/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * David McKnight   (IBM)        - [216252] [nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 *******************************************************************************/
package org.eclipse.rse.internal.useractions;

import org.eclipse.osgi.util.NLS;

public class UserActionsResources extends NLS {
	private static String BUNDLE_NAME = "org.eclipse.rse.internal.useractions.UserActionsResources"; //$NON-NLS-1$
	// Property sheet values: Categories in Team view
	public static String RESID_PROPERTY_TEAM_USERACTION_TYPE_VALUE;
	public static String RESID_PROPERTY_TEAM_COMPILETYPE_TYPE_VALUE;
	public static String RESID_PROPERTY_TEAM_COMPILECMD_TYPE_VALUE;
	// USER ACTION PROPERTIES PAGE...
	public static String RESID_PP_USERACTION_TITLE;
	public static String RESID_PP_USERACTION_TYPE_VALUE;
	public static String RESID_PP_USERACTION_PROFILE_LABEL;
	public static String RESID_PP_USERACTION_PROFILE_TOOLTIP;
	public static String RESID_PP_USERACTION_ORIGIN_LABEL;
	public static String RESID_PP_USERACTION_ORIGIN_TOOLTIP;
	public static String RESID_PP_USERACTION_DOMAIN_LABEL;
	public static String RESID_PP_USERACTION_DOMAIN_TOOLTIP;
	// COMPILE TYPE PROPERTIES PAGE...
	public static String RESID_PP_COMPILETYPE_TITLE;
	public static String RESID_PP_COMPILETYPE_TYPE_VALUE;
	public static String RESID_PP_COMPILETYPE_TYPE_TOOLTIP;
	public static String RESID_PP_COMPILETYPE_PROFILE_LABEL;
	public static String RESID_PP_COMPILETYPE_PROFILE_TOOLTIP;
	public static String RESID_PP_COMPILETYPE_FILETYPE_LABEL;
	public static String RESID_PP_COMPILETYPE_FILETYPE_TOOLTIP;
	// COMPILE COMMAND PROPERTIES PAGE...
	public static String RESID_PP_COMPILECMD_TITLE;
	public static String RESID_PP_COMPILECMD_TYPE_VALUE;
	public static String RESID_PP_COMPILECMD_PROFILE_LABEL;
	public static String RESID_PP_COMPILECMD_PROFILE_TOOLTIP;
	public static String RESID_PP_COMPILECMD_ORIGIN_LABEL;
	public static String RESID_PP_COMPILECMD_ORIGIN_TOOLTIP;
	// USER ACTIONS
	public static String ACTION_COMPILE_NOPROMPT_LABEL;
	public static String ACTION_COMPILE_NOPROMPT_TOOLTIP;
	public static String ACTION_COMPILE_PROMPT_LABEL;
	public static String ACTION_COMPILE_PROMPT_TOOLTIP;
	// Property sheet values: User actions
	public static String RESID_PROPERTY_ORIGIN_IBM_VALUE;
	public static String RESID_PROPERTY_ORIGIN_IBMUSER_VALUE;
	public static String RESID_PROPERTY_ORIGIN_USER_VALUE;
	public static String RESID_PROPERTY_ORIGIN_ISV_VALUE;
	public static String RESID_PROPERTY_ORIGIN_ISVUSER_VALUE;
	public static String RESID_PROPERTY_USERACTION_VENDOR_LABEL;
	public static String RESID_PROPERTY_USERACTION_VENDOR_TOOLTIP;
	public static String RESID_PROPERTY_USERACTION_DOMAIN_LABEL;
	public static String RESID_PROPERTY_USERACTION_DOMAIN_TOOLTIP;
	public static String RESID_PROPERTY_USERACTION_DOMAIN_ALL_VALUE;
	// Property sheet values: Compile types
	public static String RESID_PROPERTY_COMPILETYPE_TYPES_LABEL;
	public static String RESID_PROPERTY_COMPILETYPE_TYPES_DESCRIPTION;
	// TEAM VIEW
	public static String RESID_TEAMVIEW_USERACTION_VALUE;
	
	
	public static String	RESID_PROPERTY_ORIGIN_LABEL;
	public static String	RESID_PROPERTY_ORIGIN_TOOLTIP;
	public static String	RESID_PROPERTY_COMMAND_LABEL;
	public static String	RESID_PROPERTY_COMMAND_TOOLTIP;
	public static String	RESID_PROPERTY_COMMENT_LABEL;
	public static String	RESID_PROPERTY_COMMENT_TOOLTIP;
	
	public static  String MSG_VALIDATE_UDANAME_EMPTY;
	public static  String MSG_VALIDATE_UDANAME_NOTUNIQUE;
	public static  String MSG_VALIDATE_UDANAME_NOTVALID;
	public static  String MSG_VALIDATE_UDACMT_EMPTY;
	public static  String MSG_VALIDATE_UDACMT_NOTVALID;
	public static  String MSG_VALIDATE_UDACMD_EMPTY;
	public static  String MSG_VALIDATE_UDACMD_NOTVALID;
	public static  String MSG_VALIDATE_UDTNAME_EMPTY;
	public static  String MSG_VALIDATE_UDTNAME_NOTUNIQUE;
	public static  String MSG_VALIDATE_UDTNAME_NOTVALID;
	public static  String MSG_VALIDATE_UDTTYPES_EMPTY;
	public static  String MSG_VALIDATE_UDTTYPES_NOTVALID;
	public static  String MSG_VALIDATE_COMPILELABEL_EMPTY;
	public static  String MSG_VALIDATE_COMPILELABEL_NOTUNIQUE;
	public static  String MSG_VALIDATE_COMPILELABEL_NOTVALID;
	public static  String MSG_VALIDATE_COMPILESTRING_EMPTY;
	public static  String MSG_VALIDATE_COMPILESTRING_NOTVALID;
	public static  String MSG_UDA_LOAD_ERROR;
	public static  String MSG_UDA_ROOTTAG_ERROR;
	public static  String MSG_CONFIRM_DELETE_USERACTION;
	public static  String MSG_CONFIRM_DELETE_USERTYPE;

	public static  String MSG_VALIDATE_UDANAME_EMPTY_DETAILS;
	public static  String MSG_VALIDATE_UDANAME_NOTUNIQUE_DETAILS;
	public static  String MSG_VALIDATE_UDANAME_NOTVALID_DETAILS;
	public static  String MSG_VALIDATE_UDACMT_EMPTY_DETAILS;
	public static  String MSG_VALIDATE_UDACMT_NOTVALID_DETAILS;
	public static  String MSG_VALIDATE_UDACMD_EMPTY_DETAILS;
	public static  String MSG_VALIDATE_UDACMD_NOTVALID_DETAILS;
	public static  String MSG_VALIDATE_UDTNAME_EMPTY_DETAILS;
	public static  String MSG_VALIDATE_UDTNAME_NOTUNIQUE_DETAILS;
	public static  String MSG_VALIDATE_UDTNAME_NOTVALID_DETAILS;
	public static  String MSG_VALIDATE_UDTTYPES_EMPTY_DETAILS;
	public static  String MSG_VALIDATE_UDTTYPES_NOTVALID_DETAILS;
	public static  String MSG_VALIDATE_COMPILELABEL_EMPTY_DETAILS;
	public static  String MSG_VALIDATE_COMPILELABEL_NOTUNIQUE_DETAILS;
	public static  String MSG_VALIDATE_COMPILELABEL_NOTVALID_DETAILS;
	public static  String MSG_VALIDATE_COMPILESTRING_EMPTY_DETAILS;
	public static  String MSG_VALIDATE_COMPILESTRING_NOTVALID_DETAILS;

	public static  String MSG_UDA_ROOTTAG_ERROR_DETAILS;
	public static  String MSG_CONFIRM_DELETE_USERACTION_DETAILS;
	public static  String MSG_CONFIRM_DELETE_USERTYPE_DETAILS;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, UserActionsResources.class);
	}
}
