/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * Martin Oberhuber (Wind River) - [184095] combined RSEModelResources and persistence.Messages into this file
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
	public static String RSEPersistenceManager_DeleteProfileJobName;
	public static String SaveRSEDOMJob_SavingProfileJobName;
	public static String SerializingProvider_UnexpectedException;

	// Password Persistence Manager
	public static String DefaultSystemType_Label;
	
	private RSECoreMessages() {
	}
}
