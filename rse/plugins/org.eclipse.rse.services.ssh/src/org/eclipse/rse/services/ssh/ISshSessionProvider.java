package org.eclipse.rse.services.ssh;

import com.jcraft.jsch.Session;

public interface ISshSessionProvider 
{
	/* Return an active SSH session from a ConnectorService. */
	public Session getSession();
	
	/* Inform the connectorService that a session has been lost. */
	public void handleSessionLost();
	
}
