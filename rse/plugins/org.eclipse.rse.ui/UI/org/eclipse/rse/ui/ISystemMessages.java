/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 * Kevin Doyle (IBM) - [160769] Added FILEMSG_MOVE_FILTER_NOT_VALID
 * Kevin Doyle (IBM) - [199324] Added FILEMSG_MOVE_TARGET_EQUALS_PARENT_OF_SRC
 * Xuan Chen   (IBM) - [160775] Added MSG_RENAMEGENERIC_PROGRESS, FILEMSG_MOVE_INTERRUPTED
 *                                    FILEMSG_RENAME_INTERRUPTED, FILEMSG_DELETE_INTERRUPTED
 *                                    FILEMSG_COPY_INTERRUPTED
 * Xuan Chen   (IBM)        - [209828] Need to move the Create operation to a job.
 * David McKnight   (IBM)        - [216252] removing unused messages and ids
 *******************************************************************************/

package org.eclipse.rse.ui;

/**
 * Message IDs
 */
public interface ISystemMessages
{
	public static final String MSG_WIZARD_PAGE_ERROR   = "RSEG1240";	 //$NON-NLS-1$


	public static final String MSG_CONFIRM_RELOADRSE = "RSEG1002"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_NAME_EMPTY    = "RSEG1006"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_NAME_NOTUNIQUE= "RSEG1007"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_NAME_NOTVALID = "RSEG1008"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_RENAME_NOTUNIQUE= "RSEG1010"; //MSG_VALIDATE_PREFIX + "ReName.NotUnique"; //$NON-NLS-1$
 	public static final String MSG_VALIDATE_RENAME_OLDEQUALSNEW = "RSEG1009"; //MSG_VALIDATE_PREFIX+"ReName.OldEqualsNew"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_PROFILENAME_EMPTY    = "RSEG1014"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_PROFILENAME_NOTUNIQUE= "RSEG1015"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_PROFILENAME_NOTVALID = "RSEG1016"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_PROFILENAME_RESERVED = "RSEG1040"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_PATH_EMPTY    = "RSEG1032"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_PATH_NOTUNIQUE= "RSEG1033"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_PATH_NOTVALID = "RSEG1034"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_NOT_NUMERIC = "RSEG1017"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_PORT_EMPTY = "RSEG1027";	 //$NON-NLS-1$
	public static final String MSG_VALIDATE_PORT_NOTVALID = "RSEG1028";	 //$NON-NLS-1$
	public static final String MSG_VALIDATE_FOLDERNAME_NOTVALID = "RSEG1018";	 //$NON-NLS-1$
	public static final String MSG_VALIDATE_FILENAME_NOTVALID   = "RSEG1019";		 //$NON-NLS-1$

	public static final String MSG_VALIDATE_CONNECTIONNAME_EMPTY= "RSEG1021"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_CONNECTIONNAME_NOTUNIQUE = "RSEG1022"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_CONNECTIONNAME_NOTUNIQUE_OTHERPROFILE = "RSEG1041";	 //$NON-NLS-1$

	public static final String MSG_VALIDATE_HOSTNAME_EMPTY= "RSEG1024"; //MSG_VALIDATE_PREFIX + "HostNameRequired"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_USERID_EMPTY  = "RSEG1025"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_USERID_NOTVALID  = "RSEG1026"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_ENTRY_EMPTY    = "RSEG1029"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_ENTRY_NOTUNIQUE= "RSEG1030"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_ENTRY_NOTVALID = "RSEG1031"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_FILTERPOOLNAME_EMPTY    = "RSEG1037"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_FILTERPOOLNAME_NOTUNIQUE= "RSEG1038"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_FILTERNAME_EMPTY    = "RSEG1042"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_FILTERNAME_NOTUNIQUE= "RSEG1043"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_PASSWORD_EMPTY   = "RSEG1035"; //MSG_VALIDATE_PREFIX + "PasswordRequired"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_FILTERSTRING_EMPTY    = "RSEG1045"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_FILTERSTRING_NOTUNIQUE= "RSEG1046"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_FILTERSTRING_NOTVALID = "RSEG1047"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_FILTERSTRING_DUPLICATES = "RSEG1048";	 //$NON-NLS-1$
	public static final String MSG_VALIDATE_FILTERSTRING_ALREADYEXISTS = "RSEG1049";	 //$NON-NLS-1$
	public static final String MSG_VALIDATE_NUMBER_EMPTY     = "RSEG1170"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_NUMBER_NOTVALID  = "RSEG1171"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_NUMBER_OUTOFRANGE= "RSEG1172"; //$NON-NLS-1$

	public static final String MSG_CONFIRM_DELETE = "RSEG1052";	 //$NON-NLS-1$
	public static final String MSG_CONFIRM_CHANGES = "RSEG1201"; //$NON-NLS-1$




  	//public static final String MSG_SAVE_PREFIX = MSG_PREFIX + "Save.";
  	public static final String MSG_SAVE_FAILED = "RSEG1050"; //MSG_SAVE_PREFIX + "Failed";  	 //$NON-NLS-1$

  	//public static final String MSG_EXCEPTION_PREFIX = MSG_PREFIX + "Exception.";
	public static final String MSG_EXCEPTION_OCCURRED = "RSEG1003"; //$NON-NLS-1$
	public static final String MSG_EXCEPTION_DELETING = "RSEG1063"; //""RSEG1004";	 //$NON-NLS-1$
	public static final String MSG_EXCEPTION_RENAMING = "RSEG1064"; //"RSEG1005"; //MSG_EXCEPTION_PREFIX + "Renaming";		 //$NON-NLS-1$


	//public static final String MSG_QUERY_PREFIX = MSG_PREFIX + "Query.";
	public static final String MSG_QUERY_PROGRESS = "RSEG1095";	 //$NON-NLS-1$

	//public static final String MSG_COPY_PREFIX = MSG_PREFIX + "Copy.";
	public static final String MSG_COPY_PROGRESS = "RSEG1072";	 //$NON-NLS-1$
	public static final String MSG_COPYCONNECTION_PROGRESS = "RSEG1073"; //$NON-NLS-1$
	public static final String MSG_COPYCONNECTIONS_PROGRESS = "RSEG1074"; //$NON-NLS-1$
	public static final String MSG_COPYFILTERPOOLS_PROGRESS = "RSEG1075"; //$NON-NLS-1$
	public static final String MSG_COPYFILTERPOOL_PROGRESS  = "RSEG1076"; //$NON-NLS-1$
	public static final String MSG_COPYFILTERS_PROGRESS     = "RSEG1077"; //$NON-NLS-1$
	public static final String MSG_COPYFILTER_PROGRESS      = "RSEG1078"; //$NON-NLS-1$
	public static final String MSG_COPYFILTERSTRINGS_PROGRESS="RSEG1079"; //$NON-NLS-1$
	public static final String MSG_COPYFILTERSTRING_PROGRESS ="RSEG1080"; //$NON-NLS-1$

	public static final String MSG_COPYFILTERPOOL_COMPLETE  = "RSEG1082"; //$NON-NLS-1$

	//public static final String MSG_MOVE_PREFIX = MSG_PREFIX + "Move.";
	public static final String MSG_MOVECONNECTION_PROGRESS  = "RSEG1084"; //$NON-NLS-1$
	public static final String MSG_MOVECONNECTIONS_PROGRESS = "RSEG1085"; //$NON-NLS-1$
	public static final String MSG_MOVEFILTERPOOLS_PROGRESS = "RSEG1086"; //$NON-NLS-1$
	public static final String MSG_MOVEFILTERPOOL_PROGRESS  = "RSEG1087"; //$NON-NLS-1$
	public static final String MSG_MOVEFILTERS_PROGRESS     = "RSEG1088"; //$NON-NLS-1$
	public static final String MSG_MOVEFILTER_PROGRESS      = "RSEG1089"; //$NON-NLS-1$
	public static final String MSG_MOVEFILTERSTRINGS_PROGRESS="RSEG1090"; //$NON-NLS-1$
	public static final String MSG_MOVEFILTERSTRING_PROGRESS ="RSEG1091"; //$NON-NLS-1$
	public static final String MSG_MOVEFILTERPOOL_COMPLETE  = "RSEG1092"; //$NON-NLS-1$


	public static final String MSG_COPYGENERIC_PROGRESS = "RSEG1115";	 //$NON-NLS-1$
	public static final String MSG_MOVEGENERIC_PROGRESS = "RSEG1116"; //$NON-NLS-1$
	public static final String MSG_COPYTHINGGENERIC_PROGRESS = "RSEG1117";	 //$NON-NLS-1$
	public static final String MSG_MOVETHINGGENERIC_PROGRESS = "RSEG1118"; //$NON-NLS-1$
	/**
	 * @since 3.0
	 */
	public static final String MSG_RENAMEGENERIC_PROGRESS = "RSEG1142";	 //$NON-NLS-1$

	public static final String MSG_VALIDATE_SRCTYPE_EMPTY    = "RSEG1192"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_SRCTYPE_NOTVALID = "RSEG1193"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_SRCTYPE_NOTUNIQUE= "RSEG1194"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_ARCHIVE_NAME = "RSEG1120"; //$NON-NLS-1$


	public static final String MSG_FILTERPOOL_CREATED = "RSEG1160"; // defect 42503 //$NON-NLS-1$
	public static final String MSG_UPDATEFILTER_FAILED = "RSEG1161";  //$NON-NLS-1$
	public static final String MSG_RENAMEFILTER_FAILED = "RSEG1162";  //$NON-NLS-1$

	//public static final String MSG_OPERATION_PREFIX = MSG_PREFIX + "Operation.";
	public static final String MSG_OPERATION_FAILED      = "RSEG1066"; 		 //$NON-NLS-1$
	public static final String MSG_OPERATION_CANCELLED   = "RSEG1067";		 //$NON-NLS-1$


	public static final String MSG_HOSTNAME_NOTFOUND = "RSEG1220";	 //$NON-NLS-1$
	public static final String MSG_HOSTNAME_VERIFYING = "RSEG1221";	 //$NON-NLS-1$


	public static final String MSG_ENCODING_NOT_SUPPORTED = "RSEG1244";	 //$NON-NLS-1$


    // --------------------------
    // UNIVERSAL FILE MESSAGES...
    // --------------------------
	/** @since 3.0 */
	public static final String FILEMSG_RENAME_INTERRUPTED = "RSEG1246"; //$NON-NLS-1$
	/** @since 3.0 */
	public static final String FILEMSG_DELETE_INTERRUPTED = "RSEG1247"; //$NON-NLS-1$


    // --------------------------
    // SYSTEM VIEW MESSAGES...
    // --------------------------
	//public static final String MSG_EXPAND_PREFIX = MSG_PREFIX + "Expand.";
	public static final String MSG_EXPAND_FAILED    = "RSEG1098"; //MSG_EXPAND_PREFIX + "Failed"; //$NON-NLS-1$
	public static final String MSG_EXPAND_CANCELLED = "RSEG1067"; //MSG_EXPAND_PREFIX + "Cancelled"; //$NON-NLS-1$
	// Message vetoed by UCD
	//public static final String MSG_EXPAND_CANCELLED = "RSEG1099"; //MSG_EXPAND_PREFIX + "Cancelled";
	public static final String MSG_EXPAND_EMPTY     = "RSEG1100"; //MSG_EXPAND_PREFIX + "Empty"; //$NON-NLS-1$
	public static final String MSG_EXPAND_FILTERCREATED = "RSEG1102"; //MSG_EXPAND_PREFIX + "FilterCreated"; //$NON-NLS-1$
	public static final String MSG_EXPAND_CONNECTIONCREATED = "RSEG1108"; //MSG_EXPAND_PREFIX + "ConnectionCreated"; //$NON-NLS-1$

	//public static final String MSG_LIST_PREFIX = MSG_PREFIX + "List.";
	public static final String MSG_LIST_CANCELLED = "RSEG1101"; //MSG_LIST_PREFIX + "Cancelled"; //$NON-NLS-1$

    // ----------------------------------
    // GENERIC ERROR CHECKING MESSAGES...
    // ----------------------------------

	public static final String MSG_ERROR_CONNECTION_NOTFOUND = "RSEG1103"; //$NON-NLS-1$
	public static final String MSG_ERROR_PROFILE_NOTFOUND = "RSEG1104"; //$NON-NLS-1$
	public static final String MSG_ERROR_FILE_NOTFOUND = "RSEG1106"; 		 //$NON-NLS-1$


    // --------------------------
    // Generic messages, must substitute in values...
    // --------------------------
    public static final String MSG_GENERIC_I               = "RSEO1010"; //$NON-NLS-1$
    public static final String MSG_GENERIC_W               = "RSEO1011"; //$NON-NLS-1$
    public static final String MSG_GENERIC_E               = "RSEO1012"; //$NON-NLS-1$
    public static final String MSG_GENERIC_U               = "RSEO1013"; //$NON-NLS-1$
    public static final String MSG_GENERIC_Q               = "RSEO1014"; //$NON-NLS-1$
    public static final String MSG_GENERIC_I_HELP          = "RSEO1000"; //$NON-NLS-1$
    public static final String MSG_GENERIC_W_HELP          = "RSEO1001"; //$NON-NLS-1$
    public static final String MSG_GENERIC_E_HELP          = "RSEO1002"; //$NON-NLS-1$
    public static final String MSG_GENERIC_U_HELP          = "RSEO1003"; //$NON-NLS-1$
    public static final String MSG_GENERIC_Q_HELP          = "RSEO1004"; //$NON-NLS-1$


    // ----------------------------------
    // COMMUNICATIONS ERROR CHECKING MESSAGES...
    // ----------------------------------

	public static final String MSG_COMM_PWD_INVALID			= "RSEC1004"; //$NON-NLS-1$

	public static final String MSG_COMM_PWD_EXISTS			= "RSEC2101"; //$NON-NLS-1$
	public static final String MSG_COMM_PWD_MISMATCH		= "RSEC2102"; //$NON-NLS-1$
	public static final String MSG_COMM_PWD_BLANKFIELD		= "RSEC2103"; //$NON-NLS-1$

	public static final String MSG_COMM_ENVVAR_DUPLICATE	= "RSEC2001"; //$NON-NLS-1$
	public static final String MSG_COMM_ENVVAR_NONAME		= "RSEC2002"; //$NON-NLS-1$
	public static final String MSG_COMM_ENVVAR_INVALIDCHAR	= "RSEC2004"; //$NON-NLS-1$

	public static final String MSG_COMM_PORT_WARNING          = "RSEC2306"; //$NON-NLS-1$


	// Unexpected error message
	public static final String MSG_ERROR_UNEXPECTED = "RSEF8002"; //$NON-NLS-1$

	// file transfer message
	public static final String MSG_TRANSFER_INVALID = "RSEG1270"; //$NON-NLS-1$

}
