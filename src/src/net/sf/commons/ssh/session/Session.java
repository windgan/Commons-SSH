package net.sf.commons.ssh.session;

import net.sf.commons.ssh.errors.ErrorHolder;
import net.sf.commons.ssh.event.EventProcessor;
import net.sf.commons.ssh.options.Configurable;
import net.sf.commons.ssh.utils.Closable;

public interface Session extends Closable,Configurable,ErrorHolder,EventProcessor
{
    void open();
    boolean isOpened();
    boolean isOpening();
}
