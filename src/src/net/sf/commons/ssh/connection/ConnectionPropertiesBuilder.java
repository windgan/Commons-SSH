package net.sf.commons.ssh.connection;


import net.sf.commons.ssh.auth.AuthenticationMethod;
import net.sf.commons.ssh.options.*;


public class ConnectionPropertiesBuilder extends PropertiesBuilder
{
    private static ConnectionPropertiesBuilder instance = null;
    @PropertyType(Long.class)
    public static final String KEY_KEX_TIMEOUT = "net.sf.commons.ssh.options.ConnectionOptionsBuilder.kexTimeout";
    @Required
    @PropertyType(Integer.class)
    public static final String KEY_PORT = "net.sf.commons.ssh.options.ConnectionOptionsBuilder.port";
    @PropertyType(Boolean.class)
    public static final String KEY_SEND_IGNORE = "net.sf.commons.ssh.options.ConnectionOptionsBuilder.sendIgnore";
    @PropertyType(Long.class)
    public static final String KEY_SOCKET_TIMEOUT = "net.sf.commons.ssh.options.ConnectionOptionsBuilder.soTimeout";
    @PropertyType(Long.class)
    public static final String KEY_CONNECT_TIMEOUT = "net.sf.commons.ssh.options.ConnectionOptionsBuilder.connectTimeout";
    @Required
    @PropertyType(String.class)
    public static final String KEY_HOST = "net.sf.commons.ssh.options.ConnectionOptionsBuilder.host";
	@Required
	@PropertyType(AuthenticationMethod.class)
	public static final String KEY_AUTHENTICATION_METHOD = "net.sf.commons.ssh.auth.authenticationMethod";

    public ConnectionPropertiesBuilder()
    {
        defaultProperties.put(KEY_PORT, Integer.valueOf(22));
        defaultProperties.put(KEY_HOST,"127.0.0.1");
        defaultProperties.put(KEY_AUTHENTICATION_METHOD, AuthenticationMethod.NONE);
    }

    public synchronized static ConnectionPropertiesBuilder getInstance()
    {
        if (instance == null)
        {
            instance = new ConnectionPropertiesBuilder();
        }
        return instance;
    }

    public Long getKexTimeout(Properties opt)
    {
        return (Long) getProperty(opt, KEY_KEX_TIMEOUT);
    }

    public void setKexTimeout(Configurable opt, Long value)
    {
        opt.setProperty(KEY_KEX_TIMEOUT, value);
    }

    public Integer getPort(Properties opt)
    {
        return (Integer) getProperty(opt, KEY_PORT);
    }

    public void setPort(Configurable opt, Integer value)
    {
        opt.setProperty(KEY_PORT, value);
    }

    public Boolean getSendIgnore(Properties opt)
    {
        return (Boolean) getProperty(opt, KEY_SEND_IGNORE);
    }

    public void setSendIgnore(Configurable opt, Boolean value)
    {
        opt.setProperty(KEY_SEND_IGNORE, value);
    }

    public Long getSoTimeout(Properties opt)
    {
        return (Long) getProperty(opt, KEY_SOCKET_TIMEOUT);
    }

    public void setSoTimeout(Configurable opt, Long value)
    {
        opt.setProperty(KEY_SOCKET_TIMEOUT, value);
    }

    public Long getConnectTimeout(Properties opt)
    {
        return (Long) getProperty(opt, KEY_CONNECT_TIMEOUT);
    }

    public void setConnectTimeout(Configurable opt, Long value)
    {
        opt.setProperty(KEY_CONNECT_TIMEOUT, value);
    }

    public String getHost(Properties opt)
    {
        return (String) getProperty(opt, KEY_HOST);
    }

    public void setHost(Configurable opt, String value)
    {
        opt.setProperty(KEY_HOST, value);
    }
    
	public void setAuthenticationMethod(Configurable config,AuthenticationMethod value)
	{
		setProperty(config, KEY_AUTHENTICATION_METHOD, value);
	}
	
	public AuthenticationMethod getAuthenticationMethod(Properties config)
	{
		return (AuthenticationMethod) getProperty(config, KEY_AUTHENTICATION_METHOD);		
	}
    
}
