/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 * David McKnight   (IBM)        - [216252] [nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [223103] [cleanup] fix broken externalized strings
 * Xuan Chen        (IBM)        - [222263] Need to provide a PropertySet Adapter for System Team View
 * David McKnight   (IBM)        - [334295] SystemViewForm dialogs don't display cancellable progress in the dialog
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;

import org.eclipse.osgi.util.NLS;


/**
 * Constants used throughout the SystemView plugin
 */
public class SystemViewResources extends NLS {
	private static String	BUNDLE_NAME	= "org.eclipse.rse.internal.ui.view.SystemViewResources";	//$NON-NLS-1$

	// -------------------------
	// Property names...
	// -------------------------
	// Property sheet values: Common	
	public static String	RESID_PROPERTY_NBRCHILDREN_LABEL;
	public static String	RESID_PROPERTY_NBRCHILDREN_TOOLTIP;
	
	// Property sheet values: Connections
	public static String	RESID_PROPERTY_PROFILE_TYPE_VALUE;

	public static String	RESID_PROPERTY_PROFILESTATUS_LABEL;
 	public static String	RESID_PROPERTY_PROFILESTATUS_TOOLTIP;

	public static String	RESID_PROPERTY_PROFILESTATUS_ACTIVE_LABEL;

	public static String	RESID_PROPERTY_PROFILESTATUS_NOTACTIVE_LABEL;

	public static String	RESID_PROPERTY_CONNECTION_TYPE_VALUE;

	public static String	RESID_PROPERTY_SYSTEMTYPE_LABEL;
	public static String	RESID_PROPERTY_SYSTEMTYPE_TOOLTIP;

	public static String    RESID_PROPERTY_CONNECTIONSTATUS_LABEL;
	public static String    RESID_PROPERTY_CONNECTIONSTATUS_TOOLTIP;
	public static String	RESID_PROPERTY_CONNECTIONSTATUS_CONNECTED_VALUE;

	public static String	RESID_PROPERTY_CONNECTIONSTATUS_DISCONNECTED_VALUE;


	public static String	RESID_PROPERTY_HOSTNAME_LABEL;
	public static String	RESID_PROPERTY_HOSTNAME_TOOLTIP;

	public static String	RESID_PROPERTY_DEFAULTUSERID_LABEL;
	public static String	RESID_PROPERTY_DEFAULTUSERID_TOOLTIP;

	public static String	RESID_PROPERTY_CONNDESCRIPTION_LABEL;
	public static String	RESID_PROPERTY_CONNDESCRIPTION_TOOLTIP;

	public static String	RESID_PROPERTY_PROFILE_LABEL;
	public static String	RESID_PROPERTY_PROFILE_TOOLTIP;
	

	// Property sheet values: SubSystems
	public static String	RESID_PROPERTY_SUBSYSTEM_TYPE_VALUE;

	public static String	RESID_PROPERTY_USERID_LABEL;
	public static String	RESID_PROPERTY_USERID_TOOLTIP;
	
	public static String	RESID_PROPERTY_PORT_LABEL;
	public static String	RESID_PROPERTY_PORT_TOOLTIP;

	public static String	RESID_PROPERTY_CONNECTED_TOOLTIP;
	public static String	RESID_PROPERTY_CONNECTED_LABEL;

	public static String	RESID_PROPERTY_VRM_LABEL;
	public static String	RESID_PROPERTY_VRM_TOOLTIP;

	// Property sheet values: Filter Pools
	public static String	RESID_PROPERTY_FILTERPOOL_TYPE_VALUE;

	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_TYPE_VALUE;

	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPOOL_LABEL;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPOOL_TOOLTIP;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPROFILE_LABEL;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_PARENTPROFILE_TOOLTIP;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_RELATEDCONNECTION_LABEL;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_RELATEDCONNECTION_TOOLTIP;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_IS_CONNECTIONPRIVATE_LABEL;
	public static String	RESID_PROPERTY_FILTERPOOLREFERENCE_IS_CONNECTIONPRIVATE_TOOLTIP;

	// Property sheet values: Filters
	public static String	RESID_PROPERTY_FILTERSTRING_LABEL;
	public static String	RESID_PROPERTY_FILTERSTRING_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILTERSTRINGS_COUNT_LABEL;
	public static String	RESID_PROPERTY_FILTERSTRINGS_COUNT_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILTERPARENTFILTER_LABEL;
	public static String	RESID_PROPERTY_FILTERPARENTFILTER_TOOLTIP;
	
	public static String	RESID_PROPERTY_FILTERPARENTPOOL_LABEL;
	public static String	RESID_PROPERTY_FILTERPARENTPOOL_TOOLTIP;
	
	// files - still needed outside of files.ui
	public static String	RESID_PROPERTY_FILE_TYPE_FILE_VALUE;
	public static String	RESID_PROPERTY_FILE_TYPE_FOLDER_VALUE;
	public static String	RESID_PROPERTY_FILE_TYPE_ROOT_VALUE;
	
	public static String	RESID_PROPERTY_FILE_PATH_LABEL;
	public static String	RESID_PROPERTY_FILE_PATH_TOOLTIP;
	
	
	// Property sheet values: Messages
	public static String	RESID_PROPERTY_MESSAGE_TYPE_VALUE;

	// Property sheet values: Categories in Team view
	public static String	RESID_PROPERTY_TEAM_CATEGORY_TYPE_VALUE;
	public static String	RESID_PROPERTY_TEAM_SSFACTORY_TYPE_VALUE;
	public static String    RESID_PROPERTY_TEAM_PROPERTYSET_TYPE_VALUE;
	
	// Miscellaneous / common
	public static String    RESID_SCRATCHPAD;
	public static String    RESID_REMOTE_SCRATCHPAD;
	
	public static String    RESID_FETCHING;
	public static String    RESID_FETCHING_CHILDREN_OF;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, SystemViewResources.class);
	}
}
