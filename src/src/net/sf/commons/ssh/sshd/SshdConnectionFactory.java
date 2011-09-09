/*
 * Copyright 2009-2009 CommonsSSH Project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.commons.ssh.sshd;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import net.sf.commons.ssh.*;

import net.sf.commons.ssh.utils.LogUtils;
import net.sf.commons.ssh.verification.VerificationRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.ServerKeyVerifier;
import org.apache.sshd.client.SessionFactory;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSessionImpl;

/**
 * @author Sergey Vladimirov (vlsergey at gmail dot com)
 * @since 1.3
 */
public class SshdConnectionFactory extends ConnectionFactory
{
    public static final String CONNECTION_GROUP = "net.sf.commons.ssh.sshd.SshdConnectionFactory.connectionGroup";
    public static final String PROCESSOR_COUNT = "net.sf.commons.ssh.sshd.SshdConnectionFactory.processorCount";
    public static final String THREAD_SAFE_ENABLE = "net.sf.commons.ssh.sshd.SshdConnectionFactory.threadSafeEnable";
    public static final String STREAM_WAIT = "net.sf.commons.ssh.sshd.SshdConnectionFactory.streamWait";
    private static final Map<String,ClientHolder> clients = new ConcurrentHashMap<String,ClientHolder>();
    private SSHDServerKeyVerifier verifier = null;
    /**
     * Creates new instance of {@link SshdConnectionFactory}
     */
    public SshdConnectionFactory()
    {
    }

    private ClientSession connectUsingPassword(ClientHolder sshClient,
                                               String host, int port, PasswordAuthenticationOptions authOptions)
            throws IOException
    {

        try
        {
            ConnectFuture connectFuture = sshClient.getClient().connect(host, port);
            if (!connectFuture.await(getConnectTimeout() == 0?5*60000:getConnectTimeout(), TimeUnit.MILLISECONDS))
            {
                if(connectFuture.getException() == null)
                    throw new SocketTimeoutException("connection timeout");
                else
                    throw new RuntimeException("connection timeout",connectFuture.getException());
            }
            if(!connectFuture.isConnected())
                throw new RuntimeException("connection failed",connectFuture.getException());
            log.trace("connected to device "+host+" successfully");
            if(connectFuture.isCanceled())
            {
                log.trace("connection cancelled ");
                throw new IOException("connection was cancelled");
            }
            ClientSession clientSession = connectFuture.getSession();

            //int ret = ClientSession.WAIT_AUTH;
            //while ((ret & ClientSession.WAIT_AUTH) != 0)
            //{
            AuthFuture authFuture = clientSession.authPassword(authOptions.login,authOptions.password);
            if(!authFuture.await(getConnectTimeout() == 0?5*60000:getConnectTimeout(), TimeUnit.MILLISECONDS))
            {
                if(authFuture.getException() == null)
                    throw new SocketTimeoutException("authentication timeout");
                else
                    throw new RuntimeException("authentication timeout",authFuture.getException());
            }
            if(!authFuture.isSuccess())
                throw new RuntimeException("connection failed",connectFuture.getException());

            int ret = clientSession.waitFor(ClientSession.AUTHED | ClientSession.TIMEOUT,1);
                //        | ClientSession.CLOSED | ClientSession.AUTHED , 0);
            //}
            log.trace("authenticated "+((ret & ClientSession.AUTHED)!=0));
            return clientSession;
        }
        catch (Exception exc)
        {
            IOException ioException = new IOException("Unable to connect to "
                    + host + ":" + port + " using login/password: "
                    + exc.getMessage());
            ioException.initCause(exc);
            throw ioException;
        }
    }

    @Override
    public void setVerificationRepository(VerificationRepository repository)
    {
        verifier = new SSHDServerKeyVerifier(repository);
    }

    protected Set getSupportedFeaturesImpl()
    {
        final Set result = new HashSet();
        result.add(Features.AUTH_CREDENTIALS);
        result.add(Features.SESSION_EXEC);
        result.add(Features.SESSION_SHELL);
        result.add(Features.CONNECTION_TIMEOUT);
        result.add(Features.GET_PUBLIC_KEY);
        return result;
    }

    public Connection openConnection(String host, int port,
                                     AuthenticationOptions authOptions) throws IOException
    {
        ClientHolder sshClient = getClient();
        if(verifier!=null)
        {
            ServerKeyVerifier current = sshClient.getClient().getServerKeyVerifier();
            if(current == null || !(current instanceof SSHDServerKeyVerifier) || verifier.repository != ((SSHDServerKeyVerifier) current).repository)
                sshClient.getClient().setServerKeyVerifier(verifier);
        }
        ClientSession clientSession;

        if (authOptions instanceof PasswordAuthenticationOptions)
        {
            PasswordAuthenticationOptions optionsWithPassword = (PasswordAuthenticationOptions) authOptions;
            clientSession = connectUsingPassword(sshClient, host, port,
                    optionsWithPassword);
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported options type:"
                    + authOptions.getClass());
        }

        return new SshdConnection(sshClient, clientSession);
    }

    @Override
    public PublicKey getPublicKey(String host, int port) throws Exception
    {
        ClientHolder sshClient = getClient();
        ClientSession session = null;
        try
        {
            ConnectFuture connectFuture = sshClient.getClient().connect(host, port);

            if (!connectFuture.await(getConnectTimeout() == 0 ? 5 * 60000 : getConnectTimeout(), TimeUnit.MILLISECONDS))
            {
                if (connectFuture.getException() == null)
                {
                    throw new SocketTimeoutException("connection timeout");
                }
                else
                {
                    throw new RuntimeException("connection timeout", connectFuture.getException());
                }
            }
            if (!connectFuture.isConnected())
            {
                throw new RuntimeException("connection failed", connectFuture.getException());
            }
            log.trace("connected to device " + host + " successfully");
            if (connectFuture.isCanceled())
            {
                log.trace("connection cancelled ");
                throw new IOException("connection was cancelled");
            }

            session = connectFuture.getSession();
            if(!(session instanceof ClientSessionImpl))
                throw new RuntimeException("Unknown session type");
            session.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.TIMEOUT,getConnectTimeout() == 0 ? 5 * 60000 : getConnectTimeout());
            if(((ClientSessionImpl) session).getKex() == null)
                throw new RuntimeException("Unable to get server key");
            return ((ClientSessionImpl) session).getKex().getServerKey();
        }
        finally
        {
            log.trace("close connection");
            try
            {
                if(session!= null)
                    session.close(true);
            }
            catch (Exception e)
            {
                log.warn(e);
            }
            try
            {
                sshClient.close();
            }
            catch (Exception e)
            {
                log.warn(e);
            }
        }
    }

    protected ClientHolder getClient()
    {
        String connectionGroup = getProperty(CONNECTION_GROUP);
        log.trace("connection group "+connectionGroup);

        if(connectionGroup == null)
        {
            SshClient client = SshClient.setUpDefaultClient();
            setupHackedProperties(client);
            client.start();
            return new ClientHolder(null,client);
        }
        LogUtils.trace(log,"getClient from {0}",clients);
        ClientHolder holder = clients.get(connectionGroup);
        if(holder == null)
        {
            SshClient client = SshClient.setUpDefaultClient();
            setupHackedProperties(client);
            client.start();
            holder = new ClientHolder(connectionGroup,client);
        }
        holder.borrowClient();
        return holder;
    }

    public static class ClientHolder
    {
        private static final Log  log = LogFactory.getLog(ClientHolder.class);
        String group;
        SshClient client;
        int count;

        public ClientHolder(String group, SshClient client)
        {
            this.group = group;
            this.client = client;
            count = 0;
        }

        public String getGroup()
        {
            return group;
        }

        public SshClient getClient()
        {
            return client;
        }

        public ClientHolder borrowClient()
        {
            log.trace("borrow client");
            count++;
            if(count == 1)
            {
                clients.put(group,this);
            }
            LogUtils.trace(log,"client {0}: all clients {1}",this,clients);
            return this;
        }

        public void close()
        {
            log.trace("close client");
            count--;
            if(count<=0)
            {
                getClient().stop();
                if(group!=null)
                    clients.remove(group);
            }
            LogUtils.trace(log,"client {0}: all clients {1}",this,clients);
        }

        @Override
        public String toString()
        {
            return "group " + getGroup()+" count "+count;
        }
    }

    private void setupHackedProperties(SshClient client)
    {
        String processorCount = getProperty(PROCESSOR_COUNT);
        String threadSafeEnable = getProperty(THREAD_SAFE_ENABLE);
        String waitTime = getProperty(STREAM_WAIT);
        try
        {
            if(processorCount != null)
                client.setNioProcessorCount(Integer.valueOf(processorCount));
            if(threadSafeEnable != null && Boolean.valueOf(threadSafeEnable))
                client.setPumpingMethod(org.apache.sshd.client.PumpingMethod.SELF);
            if(!StringUtils.isBlank(waitTime))
                client.setStreamWaitTime(Long.valueOf(waitTime));
        }
        catch (NumberFormatException e)
        {
            log.warn("hacked library not found");
        }

    }
}
