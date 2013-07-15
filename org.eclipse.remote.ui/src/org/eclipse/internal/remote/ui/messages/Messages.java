/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.internal.remote.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.internal.remote.ui.messages.messages"; //$NON-NLS-1$

	public static String AbstractRemoteUIConnectionManager_Connection_Error;

	public static String AbstractRemoteUIConnectionManager_Could_not_open_connection;

	public static String LocalUIConnectionManager_0;
	public static String LocalUIConnectionManager_1;
	public static String LocalUIConnectionManager_2;
	public static String LocalUIConnectionManager_3;
	
	public static String PTPRemoteUIPlugin_3;
	public static String PTPRemoteUIPlugin_4;
	
	public static String RemoteConnectionWidget_Connection_Type;
	public static String RemoteConnectionWidget_connectionName;
	public static String RemoteConnectionWidget_Local;
	public static String RemoteConnectionWidget_new;
	public static String RemoteConnectionWidget_Remote;
	public static String RemoteConnectionWidget_remoteServiceProvider;
	public static String RemoteConnectionWidget_selectConnection;
	public static String RemoteConnectionWidget_selectRemoteProvider;

	public static String RemoteDevelopmentPreferencePage_defaultRemoteServicesProvider;

	public static String RemoteDirectoryWidget_0;
	public static String RemoteDirectoryWidget_1;
	public static String RemoteDirectoryWidget_2;
	public static String RemoteDirectoryWidget_3;
	
	public static String RemoteResourceBrowser_resourceTitle;
	public static String RemoteResourceBrowser_fileTitle;
	public static String RemoteResourceBrowser_directoryTitle;
	public static String RemoteResourceBrowser_resourceLabel;
	public static String RemoteResourceBrowser_fileLabel;
	public static String RemoteResourceBrowser_directoryLabel;
	public static String RemoteResourceBrowser_connectonLabel;
	public static String RemoteResourceBrowser_newConnection;
	public static String RemoteResourceBrowser_NewFolder;
	public static String RemoteResourceBrowser_Show_hidden_files;
	public static String RemoteResourceBrowser_UpOneLevel;
	
	public static String RemoteUIServicesProxy_1;
	public static String RemoteUIServicesProxy_2;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
