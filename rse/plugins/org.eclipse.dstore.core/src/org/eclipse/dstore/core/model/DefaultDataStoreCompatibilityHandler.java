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
public class DefaultDataStoreCompatibilityHandler implements
		IDataStoreCompatibilityHandler {
	private DataStore _dataStore;

	public DefaultDataStoreCompatibilityHandler(DataStore dataStore){
		_dataStore = dataStore;
	}

	public int checkCompatibility(String handshake){

		String[] clientVersionStr = DataStoreAttributes.DATASTORE_VERSION.split("\\.");	 //$NON-NLS-1$
		String[] serverVersionStr = handshake.split("\\."); //$NON-NLS-1$

		int clientVersion = Integer.parseInt(clientVersionStr[VERSION_INDEX_VERSION]);
		int serverVersion = Integer.parseInt(serverVersionStr[VERSION_INDEX_VERSION]);

		if (handshake.equals(DataStoreAttributes.DATASTORE_VERSION))
		{
			return HANDSHAKE_CORRECT;
		}
		else
		{
			if (handshake.startsWith("<DataElement")) //$NON-NLS-1$
			{
				return HANDSHAKE_SERVER_OLDER;
			}
			else if (serverVersionStr[VERSION_INDEX_PROTOCOL].equals(clientVersionStr[VERSION_INDEX_PROTOCOL]))
			{
				if (serverVersion == clientVersion)
				{
					// major versions match so should be compatible
					return HANDSHAKE_CORRECT;
				}
				else
				{
					if (serverVersion > clientVersion)
					{
						// newer server
						if (serverVersion - 1 == clientVersion)
						{
							return HANDSHAKE_SERVER_RECENT_NEWER;
						}
						else
						{
							return HANDSHAKE_SERVER_NEWER;
						}
					}
					else
					{
						// newer client
						if (serverVersion + 1 == clientVersion)
						{
							return HANDSHAKE_SERVER_RECENT_OLDER;
						}
						else if (serverVersion + 2 == clientVersion)
						{
							// TODO we shouldn't be allowing this but
							// wanting to see if old (non-open RSE server still works with open RSE)
							return HANDSHAKE_SERVER_RECENT_OLDER;
						}
						else
						{
							return HANDSHAKE_SERVER_OLDER;
						}
					}
				}
			}
			else
			{
				return HANDSHAKE_INCORRECT;
			}
		}
	}

	public void handleMissingCommand(DataElement descriptor, String keyName){
		// default does nothing in this situation
	//	System.out.println("missing command:"+keyName);
	//	System.out.println("Descriptor:"+descriptor.getName());
	}

}
