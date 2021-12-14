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

package org.eclipse.remote.internal.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.remote.internal.ui.messages.messages"; //$NON-NLS-1$

	public static String AbstractRemoteUIConnectionManager_Connection_Error;

	public static String AbstractRemoteUIConnectionManager_Could_not_open_connection;

	public static String ConnectionsPreferencePage_Add;

	public static String ConnectionsPreferencePage_closed;

	public static String ConnectionsPreferencePage_Close;

	public static String ConnectionsPreferencePage_Confirm_Actions;

	public static String ConnectionsPreferencePage_Connection_Name;

	public static String ConnectionsPreferencePage_Edit;

	public static String ConnectionsPreferencePage_Host;

	public static String ConnectionsPreferencePage_open;

	public static String ConnectionsPreferencePage_Open;

	public static String ConnectionsPreferencePage_Remote_Services;

	public static String ConnectionsPreferencePage_Remove;

	public static String ConnectionsPreferencePage_Status;

	public static String ConnectionsPreferencePage_There_are_unsaved_changes;

	public static String ConnectionsPreferencePage_This_connection_contains_unsaved_changes;

	public static String ConnectionsPreferencePage_User;

	public static String LocalUIConnectionManager_0;
	public static String LocalUIConnectionManager_1;
	public static String LocalUIConnectionManager_2;
	public static String LocalUIConnectionManager_3;

	public static String PendingUpdateAdapter_Pending;

	public static String PTPRemoteUIPlugin_3;
	public static String PTPRemoteUIPlugin_4;

	public static String RemoteConnectionWidget_Connection_Type;
	public static String RemoteConnectionWidget_Connection_Name;
	public static String RemoteConnectionWidget_Local;
	public static String RemoteConnectionWidget_New;
	public static String RemoteConnectionWidget_Remote;
	public static String RemoteConnectionWidget_selectConnection;
	public static String RemoteConnectionWidget_selectConnectionType;

	public static String RemoteDevelopmentPreferencePage_Default_connection_type;

	public static String RemoteDirectoryWidget_0;
	public static String RemoteDirectoryWidget_1;
	public static String RemoteDirectoryWidget_2;
	public static String RemoteDirectoryWidget_3;

	public static String RemoteFileWidget_Browse;

	public static String RemoteFileWidget_File;

	public static String RemoteFileWidget_Restore_Default;

	public static String RemoteFileWidget_Select_File;

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

	public static String RemoteResourceBrowserWidget_0;

	public static String RemoteResourceBrowserWidget_1;

	public static String RemoteResourceBrowserWidget_2;

	public static String RemoteResourceBrowserWidget_3;

	public static String RemoteResourceBrowserWidget_4;

	public static String RemoteResourceBrowserWidget_New_Folder;

	public static String RemoteResourceBrowserWidget_Unable_to_create_new_folder;

	public static String RemoteUIServices_Configuring_remote_services;

	public static String RemoteUIServicesProxy_1;
	public static String RemoteUIServicesProxy_2;

	public static String CloseConnectionHandler_0;
	public static String CloseConnectionHandler_1;
	public static String DeleteRemoteConnectionHandler_ConfirmDeleteMessage;
	public static String DeleteRemoteConnectionHandler_DeleteConnectionTitle;
	public static String OpenConnectionHandler_0;
	public static String OpenConnectionHandler_1;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
