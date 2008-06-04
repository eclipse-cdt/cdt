/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight   (IBM) - [220892][dstore] Backward compatibility: Server and Daemon should support old clients
 ********************************************************************************/
package org.eclipse.dstore.core.model;


/**
 * @since 3.0
 */
public interface IDataStoreCompatibilityHandler {
	public static final int HANDSHAKE_INCORRECT = 0;
	public static final int HANDSHAKE_SERVER_OLDER = 1;
	public static final int HANDSHAKE_CORRECT = 2;
	public static final int HANDSHAKE_UNEXPECTED = 3;
	public static final int HANDSHAKE_SERVER_NEWER = 4;
	public static final int HANDSHAKE_SERVER_RECENT_OLDER = 5;
	public static final int HANDSHAKE_SERVER_RECENT_NEWER = 6;
	public static final int HANDSHAKE_TIMEOUT = 7;

	public static final int VERSION_INDEX_PROTOCOL = 0;
	public static final int VERSION_INDEX_VERSION  = 1;
	public static final int VERSION_INDEX_MINOR    = 2;

	/**
	 * Checks whether a server is compatible with the current client
	 * @param handshake the server handshake string in the form <version>.<major>.<miner>
	 * @return whether this is considered compatible with the client datastore version
	 */
	public int checkCompatibility(String handshake);

	/**
	 * This method is called to notify the compatibility handler that a call
	 * to localDescriptorQuery() failed to return a result.
	 *
	 * @param descriptor the object descriptor that the command was looked for under
	 * @param keyName the value of the command descriptor to look for
	 */
	public void handleMissingCommand(DataElement descriptor, String keyName);

}
