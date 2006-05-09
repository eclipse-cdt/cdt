package org.eclipse.rse.services.ssh;

import com.jcraft.jsch.Session;

public interface ISshSessionProvider 
{
	public Session getSession();
}
