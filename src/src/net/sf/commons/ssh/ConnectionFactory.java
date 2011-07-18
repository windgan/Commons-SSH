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
package net.sf.commons.ssh;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketOptions;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sf.commons.ssh.options.*;
import net.sf.commons.ssh.utils.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract SSH connection factory.
 *
 * @author Sergey Vidyuk (svidyuk at gmail dot com)
 * @since 1.0
 */
public abstract class ConnectionFactory implements Closeable
{

    /**
     * Creates new instance of {@link ConnectionFactory} using system properties
     * to resolve actual connection factory instance.
     *
     * @param requiredFeatures set of required features factory must support
     * @return new instance of {@link ConnectionFactory}
     * @since 1.0
     */
    public static ConnectionFactory newInstance(Set requiredFeatures)
    {
        return ConnectionFactoryResolver.resolve(requiredFeatures);
    }

    public static ConnectionFactory newInstance(String factory, Set requiredFeatures)
    {
        return ConnectionFactoryResolver.resolve(factory, requiredFeatures);
    }

    /**
     * Factory logger
     */
    protected final Log log = LogFactory.getLog(this.getClass());


    private Set supportedFeatures = null;

    private Properties properties=new Properties();

    /**
     * Returns the timeout value for the key exchange
     *
     * @return the timeout value for the key exchange
     * @since 1.0
     */
    @Deprecated
    public int getKexTimeout()
    {
        return ConnectionOptionsBuilder.getKexTimeout(getFactoryOptions());
    }

    /**
     * @return the port to connect to on the remote host
     * @since 1.0
     */
    @Deprecated
    public int getPort()
    {
        return ConnectionOptionsBuilder.getPort(getFactoryOptions());
    }

    /**
     * @return the soTimeout
     * @since 1.0
     */
    @Deprecated
    public int getSoTimeout()
    {
        return ConnectionOptionsBuilder.getSoTimeout(getFactoryOptions());
    }

    /**
     * @return the connectTimeout
     * @since 1.4
     */
    @Deprecated
    public int getConnectTimeout()
    {
        return ConnectionOptionsBuilder.getConnectTimeout(getFactoryOptions());
    }

    /**
     * Returns {@link Set} of {@link String} � the set of supported features
     * codes.
     *
     * @return {@link Set} of {@link String} � the set of supported features
     *         codes.
     * @see #isFeatureSupported(String)
     * @see Features
     */
    public final Set getSupportedFeatures()
    {
        synchronized (this)
        {
            if (supportedFeatures == null)
            {
                supportedFeatures = Collections
                        .unmodifiableSet(getSupportedFeaturesImpl());
            }

            return supportedFeatures;
        }
    }

    /**
     * Creates {@link Set} of {@link String} � the set of supported features
     * codes.
     *
     * @return {@link Set} of {@link String} � the set of supported features
     *         codes.
     * @see #isFeatureSupported(String)
     * @see Features
     */
    protected abstract Set getSupportedFeaturesImpl();

    /**
     * Returns <code>true</code> if specified feature is supported by
     * {@link ConnectionFactory}, <code>false</code> otherwise.
     *
     * @param feature feature code to check.
     * @return <code>true</code> if specified feature is supported by
     *         {@link ConnectionFactory}, <code>false</code> otherwise.
     * @see #getSupportedFeatures()
     * @see Features
     */
    public boolean isFeatureSupported(String feature)
    {
        if (log.isTraceEnabled())
        {
            log.trace("isFeatureSupported('" + feature + "')");
        }

        final boolean result = getSupportedFeatures().contains(feature);

        if (log.isTraceEnabled())
        {
            log.trace("isFeatureSupported(): result = " + result);
        }

        return result;
    }

    /**
     * @return the send ignore flag to send random data packets
     */
    @Deprecated
    public boolean isSendIgnore()
    {
        return ConnectionOptionsBuilder.isSendIgnore(getFactoryOptions());
    }

    /**
     * Creates new SSH connection using the specified connection settings
     *
     * @param host        host to connect
     * @param authOptions Authentication Options (should be supported by connection
     *                    factory)
     * @return new SSH connection
     * @throws IOException if I/O exception occurs
     * @since 1.0
     */
    @Deprecated
    public Connection openConnection(String host,
                                     AuthenticationOptions authOptions) throws IOException
    {
        return openConnection(host, getPort(), authOptions);
    }

    /**
     * Creates new SSH connection using the specified connection settings
     *
     * @param host        host to connect
     * @param port        port to connect
     * @param authOptions Authentication Options (should be supported by connection
     *                    factory)
     * @return new SSH connection
     * @throws IOException if I/O exception occurs
     * @since 1.0
     */
    @Deprecated
    public abstract Connection openConnection(String host, int port,
                                              AuthenticationOptions authOptions) throws IOException;

    /**
     * @param kexTimeout the kexTimeout to set
     * @since 1.0
     */
    @Deprecated
    public void setKexTimeout(int kexTimeout)
    {
        ConnectionOptionsBuilder.setKexTimeout(getFactoryOptions(), kexTimeout);
    }

    /**
     * @param port the port to connect to on the remote host
     * @since 1.0
     */
    @Deprecated
    public void setPort(int port)
    {
        ConnectionOptionsBuilder.setPort(getFactoryOptions(), port);
    }

    /**
     * @param sendIgnore the send ignore flag to send random data packets
     * @since 1.0
     */
    @Deprecated
    public void setSendIgnore(boolean sendIgnore)
    {
        ConnectionOptionsBuilder.setSendIgnore(getFactoryOptions(), sendIgnore);
    }

    /**
     * Enable/disable {@link SocketOptions#SO_TIMEOUT} with the specified
     * timeout, in milliseconds. With this option set to a non-zero timeout, a
     * {@link InputStream#read()} will block for only this amount of time.
     * <p/>
     * If the timeout expires, a {@link SocketTimeoutException} is raised,
     * though the Socket is still valid.
     * <p/>
     * The timeout must be &gt; 0. A timeout of zero is interpreted as an
     * infinite timeout.
     *
     * @param soTimeout the soTimeout to set
     * @since 1.0
     */
    @Deprecated
    public void setSoTimeout(int soTimeout)
    {
        ConnectionOptionsBuilder.setSoTimeout(getFactoryOptions(), soTimeout);
    }

    /**
     * Enable/disable connectTimeout with a specified timeout value, in millisecond.
     * With this options set to a non-zero timeout, socket will connect to the server
     * for only this amount of time. A timeout of zero is interpreted as an infinite
     * timeout. The connection will then block until established or an error occurs.
     * <p/>
     * If the timeout expires, a {@link SocketTimeoutException} is raised.
     *
     * @param connectTimeout the connectTimeout to set
     * @since 1.4
     */
    @Deprecated
    public void setConnectTimeout(int connectTimeout)
    {
        ConnectionOptionsBuilder.setConnectTimeout(getFactoryOptions(), connectTimeout);
    }

    public String getProperty(String key)
    {
        return properties.getProperty(key);
    }

    public String getProperty(String key,String defaultValue)
    {
        String result=properties.getProperty(key);
        if(result==null)
            return defaultValue;
        else
            return result;
    }

    public void setProperty(String key,String value)
    {
        properties.setProperty(key,value);
    }



    protected List<Connection> connections = new ArrayList<Connection>();
    protected Options options;

    protected AtomicBoolean isClosed = new AtomicBoolean(false);

    protected ConnectionFactory()
    {
        options = new Options();
        ConnectionOptionsBuilder.initDefault(options);
        FactoryOptionsBuilder.initDefault(options);
        ShellSessionOptionsBuilder.initDefault(options);
        SftpSessionOptionsBuilder.initDefault(options);
    }

    public  boolean isClosed()
    {
        return isClosed.get();
    }

    public void close() throws IOException
    {
        for(Connection conection: connections)
        {
            if(!conection.isClosed())
                conection.close();
        }
        isClosed.set(true);
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        IOUtils.close(this);
    }

    protected abstract Connection initConnection(Options options);

    public Connection createConnection()
    {
        return initConnection(options);
    }


    public void clearClosedConnections()
    {
        for(Connection conection: connections)
        {
            if(conection.isClosed())
                connections.remove(conection);
        }
    }

    public List<Connection> getManagedConnections()
    {
        return connections;
    }

    public Options getFactoryOptions()
    {
        return options;
    }

    public void setFactoryOptions(Options factoryOptions)
    {
        this.options = factoryOptions;
    }

    public FactoryOptionsBuilder getFactoryOptionsBuilder()
    {
        return new FactoryOptionsBuilder(options);
    }

    public ConnectionOptionsBuilder getConnectionOptionsBuilder()
    {
        return new ConnectionOptionsBuilder(options);
    }
}
