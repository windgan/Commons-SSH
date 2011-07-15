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

import net.sf.commons.ssh.*;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ChannelSession;

/**
 * @author Sergey Vladimirov (vlsergey at gmail dot com)
 * @since 1.3
 */
class SshdConnection extends Connection {
    private final ClientSession clientSession;

    private final SshClient sshClient;

    SshdConnection(SshClient sshClient, ClientSession clientSession) {
	this.sshClient = sshClient;
	this.clientSession = clientSession;
    }

    public void close() throws IOException {
	clientSession.close(false);

	if ((clientSession.waitFor(ClientSession.CLOSED, 1000) & ClientSession.CLOSED) == 0)
	    clientSession.close(true);

	sshClient.stop();
    }

    public boolean isClosed() {
	int ret = clientSession.waitFor(ClientSession.CLOSED, 0);
	return (ret & ClientSession.CLOSED) != 0;
    }

    public ExecSession openExecSession(ExecSessionOptions execSessionOptions)
	    throws IOException {
	try {
	    ChannelExec channelExec = (ChannelExec) clientSession
		    .createChannel(ClientChannel.CHANNEL_EXEC);

	    return new SshdExecSession(channelExec, execSessionOptions
		    .getCommand().getBytes("utf-8"));
	} catch (Exception exc) {
	    IOException ioException = new IOException(
		    "Unable to open EXEC session: " + exc.getMessage());
	    ioException.initCause(exc);
	    throw ioException;
	}
    }

    public ShellSession openShellSession(ShellSessionOptions shellSessionOptions)
	    throws IOException {
	try {
	    ChannelSession channelSession = (ChannelSession) clientSession
		    .createChannel(ClientChannel.CHANNEL_SHELL);

	    return new SshdShellSession(channelSession);
	} catch (Exception exc) {
	    IOException ioException = new IOException(
		    "Unable to open SHELL session: " + exc.getMessage());
	    ioException.initCause(exc);
	    throw ioException;
	}
    }

}