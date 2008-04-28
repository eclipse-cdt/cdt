/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * Martin Oberhuber (Wind River) - [184095] combined RSEModelResources and persistence.Messages into this file
 * Martin Oberhuber (Wind River) - [215820] Move SystemRegistry implementation to Core
 * David McKnight   (IBM)        - [220309] [nls] Some GenericMessages and SubSystemResources should move from UI to Core
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * David Dykstal (IBM) - [197167] adding notification and waiting for RSE model
 * David Dykstal (IBM) - [189274] provide import and export operations for profiles
 * David Dykstal (IBM) - [216858] Need the ability to Import/Export RSE connections for sharing
 ********************************************************************************/
package org.eclipse.rse.internal.core;

import org.eclipse.osgi.util.NLS;

public class RSECoreMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.rse.internal.core.messages"; //$NON-NLS-1$
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, RSECoreMessages.class);
	}

	// Model
	public static String RESID_MODELOBJECTS_MODELOBJECT_DESCRIPTION;
	public static String RESID_MODELOBJECTS_REFERENCINGOBJECT_DESCRIPTION;
	public static String RESID_MODELOBJECTS_FILTERSTRING_DESCRIPTION;
	public static String RESID_MODELOBJECTS_HOSTPOOL_DESCRIPTION;
	public static String RESID_MODELOBJECTS_PROFILE_DESCRIPTION;
	public static String RESID_MODELOBJECTS_SERVERLAUNCHER_DESCRIPTION;
	public static String RESID_MODELOBJECTS_FILTER_DESCRIPTION;
	public static String RESID_MODELOBJECTS_FILTERPOOL_DESCRIPTION;
	
	public static String RESID_PROP_SERVERLAUNCHER_MEANS_LABEL;
	public static String RESID_PROP_SERVERLAUNCHER_PATH;
	public static String RESID_PROP_SERVERLAUNCHER_INVOCATION;
	public static String RESID_CONNECTION_DAEMON_PORT_LABEL;
	public static String RESID_CONNECTION_PORT_LABEL;
	public static String RESID_SUBSYSTEM_AUTODETECT_LABEL;

	public static String RESID_PROPERTYSET_REMOTE_SERVER_LAUNCHER;
	public static String RESID_PROPERTYSET_LAUNCHER_PROPERTIES;

	// Persistence
	public static String PropertyFileProvider_LoadingTaskName;
	public static String PropertyFileProvider_SavingTaskName;
	public static String PropertyFileProvider_UnexpectedException;
	public static String RSEEnvelope_ExportNotSupported;
	public static String RSEEnvelope_IncorrectFormat;
	public static String RSEEnvelope_ModelNotExported;
	public static String RSEPersistenceManager_DeleteProfileJobName;
	public static String SaveRSEDOMJob_SavingProfileJobName;
	public static String SerializingProvider_UnexpectedException;

	// Password Persistence Manager
	public static String DefaultSystemType_Label;
	
	// Initialization
	public static String RSELocalConnectionInitializer_localConnectionName;
	public static String InitRSEJob_error_creating_mark;
	public static String InitRSEJob_initializer_ended_in_error;
	public static String InitRSEJob_initializer_failed_to_load;
	public static String InitRSEJob_initializing_rse;
	public static String InitRSEJob_listener_ended_in_error;
	
	// SystemRegistry: Loading Profile Warning Messages - See also ISystemMessages
	public static String MSG_LOADING_PROFILE_WARNING_FILTERPOOL_REFS;
	public static String MSG_LOADING_PROFILE_WARNING_FILTERPOOL_REF;
	public static String MSG_LOADING_PROFILE_SHOULDNOTBE_DEACTIVATED; //RSEG1069
	public static String MSG_LOADING_PROFILE_SHOULDBE_ACTIVATED;
	public static String MSG_CREATEHOST_EXCEPTION;

	// SystemRegistry: Progress Reporting - See also ISystemMessages
	public static String MSG_COPYCONNECTION_PROGRESS;  //RSEG1073
	public static String MSG_COPYFILTERPOOLS_PROGRESS; //RSEG1075
	public static String MSG_COPYSUBSYSTEMS_PROGRESS;  //RSEG1081
	
	
	public static String RSESubSystemOperation_message;
	public static String RSESubSystemOperation_Connect_message;
	public static String RSESubSystemOperation_Disconnect_message;
	public static String RSESubSystemOperation_Get_properties_message;
	public static String RSESubSystemOperation_Get_property_message;
	public static String RSESubSystemOperation_Resolve_filter_strings_message;
	public static String RSESubSystemOperation_Set_properties_message;
	public static String RSESubSystemOperation_Set_property_message;
	public static String RSESubSystemOperation_Notifying_registry_message;
	
	// yantzi: artemis 6.0, offline messages
	public static String MSG_OFFLINE_CANT_CONNECT;
	public static String MSG_OFFLINE_CANT_CONNECT_DETAILS;
	
	// Connection doesn't exist
	public static String MSG_CONNECTION_DELETED;
	
	private RSECoreMessages() {
	}
}
