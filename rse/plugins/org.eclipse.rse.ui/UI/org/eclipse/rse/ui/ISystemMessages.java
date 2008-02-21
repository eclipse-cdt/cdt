/********************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
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
 ********************************************************************************/

package org.eclipse.rse.ui;

/**
 * Message IDs
 */
public interface ISystemMessages 
{

	/***************************************************/
	/* Unused messages (that may still be usable
	/***************************************************/
	public static final String MSG_VALIDATE_RENAME_EMPTY    = "RSEG1012"; //MSG_VALIDATE_PREFIX + "ReName.Required"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_RENAME_NOTVALID = "RSEG1011"; //MSG_VALIDATE_PREFIX + "ReName.NotValid"; //$NON-NLS-1$
	
	public static final String MSG_VALIDATE_CONNECTIONNAME_NOTVALID = "RSEG1023"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_FILTERPOOLNAME_NOTVALID = "RSEG1039"; //$NON-NLS-1$
	
	public static final String MSG_VALIDATE_FILTERNAME_NOTVALID = "RSEG1044"; //$NON-NLS-1$

	public static final String MSG_VALIDATE_PASSWORD_EXPIRED = "RSEG1036"; //MSG_VALIDATE_PREFIX + "PasswordExpired";	 //$NON-NLS-1$
	public static final String MSG_VALIDATE_PASSWORD_INVALID = "RSEG1297"; //$NON-NLS-1$

	public static final String MSG_CONFIRM_DELETEREMOTE = "RSEG1130";	 //$NON-NLS-1$
	public static final String MSG_CONFIRM_DELETEPROFILE = "RSEG1053";		 //$NON-NLS-1$

	public static final String MSG_CONFIRM_CHANGES_CANCELABLE = "RSEG1202"; //$NON-NLS-1$

  	//public static final String MSG_CONNECT_PREFIX = MSG_PREFIX + "Connect.";
	public static final String MSG_CONNECT_PROGRESS    = "RSEG1054"; //MSG_CONNECT_PREFIX + "Connecting";  	  		 //$NON-NLS-1$
	public static final String MSG_CONNECTWITHPORT_PROGRESS    = "RSEG1055"; //MSG_CONNECT_PREFIX + "ConnectingWithPort";  	  			 //$NON-NLS-1$
	public static final String MSG_CONNECT_FAILED      = "RSEG1056"; //MSG_CONNECT_PREFIX + "Failed";			 //$NON-NLS-1$
	public static final String MSG_CONNECT_UNKNOWNHOST = "RSEG1057"; //MSG_CONNECT_PREFIX + "UnknownHost";	 //$NON-NLS-1$
	public static final String MSG_CONNECT_CANCELLED   = "RSEG1058"; //MSG_CONNECT_PREFIX + "Cancelled"; //$NON-NLS-1$
	
	public static final String MSG_CONNECT_DAEMON_FAILED = "RSEG1242"; //MSG_CONNECT_PREFIX + "Failed";			 //$NON-NLS-1$
	public static final String MSG_CONNECT_DAEMON_FAILED_EXCEPTION = "RSEG1243"; //MSG_CONNECT_PREFIX + "Failed";	 //$NON-NLS-1$
	public static final String MSG_CONNECT_SSL_EXCEPTION = "RSEC2307"; //MSG_CONNECT_PREFIX + "Failed";	 //$NON-NLS-1$
		
	public static final String MSG_STARTING_SERVER_VIA_REXEC = "RSEC2310"; //$NON-NLS-1$
	public static final String MSG_STARTING_SERVER_VIA_DAEMON = "RSEC2311"; //$NON-NLS-1$
	public static final String MSG_CONNECTING_TO_SERVER= "RSEC2312"; //$NON-NLS-1$
	public static final String MSG_INITIALIZING_SERVER= "RSEC2313"; //$NON-NLS-1$
	public static final String MSG_PORT_OUT_RANGE = "RSEC2316"; //$NON-NLS-1$
	

  	//public static final String MSG_DISCONNECT_PREFIX = MSG_PREFIX + "Disconnect.";	
	public static final String MSG_DISCONNECT_PROGRESS = "RSEG1059"; //MSG_DISCONNECT_PREFIX + "Disconnecting"; //$NON-NLS-1$
	public static final String MSG_DISCONNECTWITHPORT_PROGRESS = "RSEG1060"; //MSG_DISCONNECT_PREFIX + "DisconnectingWithPort";  	 //$NON-NLS-1$
	public static final String MSG_DISCONNECT_FAILED      = "RSEG1061"; // MSG_DISCONNECT_PREFIX + "Failed";			 //$NON-NLS-1$
	public static final String MSG_DISCONNECT_CANCELLED   = "RSEG1062"; //MSG_DISCONNECT_PREFIX + "Cancelled";		 //$NON-NLS-1$
	
  	public static final String MSG_RESTORE_FAILED = "RSEG1051"; //$NON-NLS-1$
  	public static final String MSG_SAVE_CHANGES_PENDING = "RSEG1201"; //$NON-NLS-1$
  	  	
	public static final String MSG_EXCEPTION_MOVING   = "RSEG1065"; //MSG_EXCEPTION_PREFIX + "Moving";		  	 //$NON-NLS-1$

	//public static final String MSG_RESOLVE_PREFIX = MSG_PREFIX + "Resolve.";
	public static final String MSG_RESOLVE_PROGRESS = "RSEG1070"; //$NON-NLS-1$
	
	public static final String MSG_QUERY_PROPERTIES_PROGRESS = "RSEG1096";		 //$NON-NLS-1$

	//public static final String MSG_SET_PREFIX = MSG_PREFIX + "Set.";
	public static final String MSG_SET_PROGRESS = "RSEG1093";	 //$NON-NLS-1$
	public static final String MSG_SET_PROPERTIES_PROGRESS = "RSEG1094";		 //$NON-NLS-1$

	//public static final String MSG_RUN_PREFIX = MSG_PREFIX + "Run.";
	public static final String MSG_RUN_PROGRESS = "RSEG1071";	 //$NON-NLS-1$

	public static final String MSG_COPYSUBSYSTEMS_PROGRESS  = "RSEG1081"; //$NON-NLS-1$
	

	public static final String MSG_DOWNLOAD_PROGRESS        = "RSEG1280"; //$NON-NLS-1$
	public static final String MSG_UPLOAD_PROGRESS          = "RSEG1281"; //$NON-NLS-1$
	public static final String MSG_SYNCHRONIZE_PROGRESS     = "RSEG1282"; //$NON-NLS-1$
	public static final String MSG_EXTRACT_PROGRESS         = "RSEG1285"; //$NON-NLS-1$
	public static final String MSG_PERCENT_DONE	            = "RSEG1290"; //$NON-NLS-1$
	public static final String MSG_DOWNLOADING_PROGRESS     = "RSEG1295"; //$NON-NLS-1$
	public static final String MSG_UPLOADING_PROGRESS       = "RSEG1296"; //$NON-NLS-1$
	
	public static final String MSG_MOVE_PROGRESS            = "RSEG1083"; // "moving %1 to %2" //$NON-NLS-1$

	public static final String MSG_CREATEFILEGENERIC_PROGRESS = "RSEG1143";	 //$NON-NLS-1$
	public static final String MSG_CREATEFOLDERGENERIC_PROGRESS = "RSEG1144";	 //$NON-NLS-1$


	public static final String MSG_SAVING_PROGRESS = "RSEG1119"; //$NON-NLS-1$
	

	public static final String FILEMSG_ARCHIVE_CORRUPTED = "RSEG1122"; //$NON-NLS-1$
	public static final String MSG_FOLDER_INUSE = "RSEG1150"; // defect 42138	 //$NON-NLS-1$
	public static final String MSG_FILE_INUSE = "RSEG1151"; // defect 42332 //$NON-NLS-1$


	//public static final String MSG_LOADING_PREFIX = MSG_PREFIX + "Loading.";
	public static final String MSG_LOADING_PROFILE_SHOULDBE_ACTIVATED = "RSEG1068"; //$NON-NLS-1$
	public static final String MSG_LOADING_PROFILE_SHOULDNOTBE_DEACTIVATED = "RSEG1069";	 //$NON-NLS-1$

	public static final String MSG_WIZARD_PAGE_ERROR   = "RSEG1240";	 //$NON-NLS-1$
	
	// universal find files
	public static final String MSG_UFF_PATTERN_EMPTY  		= "RSEG1250"; //$NON-NLS-1$
	public static final String MSG_UFF_PATTERN_INVALID_REGEX  = "RSEG1251"; //$NON-NLS-1$
	
	// universal commands
	public static final String MSG_UCMD_INVOCATION_EMPTY      = "RSEG1260"; //$NON-NLS-1$
				

	// operation status
	public static final String MSG_OPERATION_RUNNING		= "RSEG1255"; //$NON-NLS-1$
	public static final String MSG_OPERATION_FINISHED		= "RSEG1256"; //$NON-NLS-1$
	public static final String MSG_OPERTION_STOPPED			= "RSEG1257"; //$NON-NLS-1$
	public static final String MSG_OPERATION_DISCONNECTED	= "RSEG1258";			 //$NON-NLS-1$
					
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_EMPTY    = "RSEF1011"; //$NON-NLS-1$
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE= "RSEF1007"; //$NON-NLS-1$
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_NOTVALID = "RSEF1008"; //$NON-NLS-1$
	public static final String FILEMSG_VALIDATE_FILEFILTERSTRING_NOINCLUDES = "RSEF1009";	 //$NON-NLS-1$
    public static final String FILEMSG_DELETE_FILE_FAILED  = "RSEF1300"; //$NON-NLS-1$
    public static final String FILEMSG_RENAME_FILE_FAILED  = "RSEF1301"; //$NON-NLS-1$
    public static final String FILEMSG_CREATE_FILE_FAILED  = "RSEF1302"; //$NON-NLS-1$
    public static final String FILEMSG_CREATE_FILE_FAILED_EXIST  = "RSEF1303"; //$NON-NLS-1$
    public static final String FILEMSG_CREATE_FOLDER_FAILED  = "RSEF1304"; //$NON-NLS-1$
    public static final String FILEMSG_CREATE_FOLDER_FAILED_EXIST  = "RSEF1309"; //$NON-NLS-1$
    public static final String FILEMSG_CREATE_RESOURCE_NOTVISIBLE  = "RSEF1310"; //$NON-NLS-1$
    public static final String FILEMSG_RENAME_RESOURCE_NOTVISIBLE  = "RSEF1311"; //$NON-NLS-1$
	public static final String FILEMSG_ERROR_NOFILETYPES = "RSEF1010"; //$NON-NLS-1$
    public static final String FILEMSG_COPY_FILE_FAILED  = "RSEF1306"; //$NON-NLS-1$
    public static final String FILEMSG_MOVE_FILE_FAILED  = "RSEF1307"; //$NON-NLS-1$
    public static final String FILEMSG_MOVE_TARGET_EQUALS_SOURCE  = "RSEF1308"; //$NON-NLS-1$
    public static final String FILEMSG_MOVE_TARGET_EQUALS_PARENT_OF_SOURCE = "RSEF1314"; //$NON-NLS-1$
	public static final String FILEMSG_MOVE_TARGET_DESCENDS_FROM_SOURCE = "RSEF1312"; //$NON-NLS-1$
	public static final String FILEMSG_MOVE_FILTER_NOT_VALID = "RSEF1313"; //$NON-NLS-1$
	public static final String FILEMSG_DELETING = "RSEF1315"; //$NON-NLS-1$
	public static final String FILEMSG_MOVE_INTERRUPTED = "RSEG1245"; //$NON-NLS-1$

	public static final String FILEMSG_COPY_INTERRUPTED = "RSEG1248"; //$NON-NLS-1$

	
	/***************************************************/
	/* End of Unused messages (that may still be usable
	/***************************************************/
	
	
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
	public static final String FILEMSG_RENAME_INTERRUPTED = "RSEG1246"; //$NON-NLS-1$
	public static final String FILEMSG_DELETE_INTERRUPTED = "RSEG1247"; //$NON-NLS-1$

	
	
	/******/
	
	
	// -------------------------	
	// IMPORT/EXPORT MESSAGES...
	// -------------------------
	public static final String FILEMSG_COPY_ROOT = "RSEF8050"; //$NON-NLS-1$
	public static final String FILEMSG_IMPORT_ERROR = "RSEF8052"; //$NON-NLS-1$
	public static final String FILEMSG_IMPORT_PROBLEMS = "RSEF8054"; //$NON-NLS-1$
	public static final String FILEMSG_IMPORT_SELF = "RSEF8056";	 //$NON-NLS-1$
	public static final String FILEMSG_EXPORT_ERROR = "RSEF8057"; //$NON-NLS-1$
	public static final String FILEMSG_EXPORT_PROBLEMS = "RSEF8058";	 //$NON-NLS-1$
	public static final String FILEMSG_NOT_WRITABLE = "RSEF8059"; //$NON-NLS-1$
		
	public static final String FILEMSG_TARGET_EXISTS = "RSEF8060"; //$NON-NLS-1$
	public static final String FILEMSG_FOLDER_IS_FILE = "RSEF8061";	 //$NON-NLS-1$
	public static final String FILEMSG_DESTINATION_CONFLICTING = "RSEF8062";	 //$NON-NLS-1$
	public static final String FILEMSG_SOURCE_IS_FILE = "RSEF8063";	 //$NON-NLS-1$
	public static final String FILEMSG_SOURCE_EMPTY = "RSEF8066";	 //$NON-NLS-1$
	public static final String FILEMSG_EXPORT_FAILED = "RSEF8067";	 //$NON-NLS-1$
	public static final String FILEMSG_EXPORT_NONE_SELECTED = "RSEF8068";		 //$NON-NLS-1$
	public static final String FILEMSG_DESTINATION_EMPTY = "RSEF8069";	 //$NON-NLS-1$
	public static final String FILEMSG_IMPORT_FAILED = "RSEF8070";		 //$NON-NLS-1$
	public static final String FILEMSG_IMPORT_NONE_SELECTED = "RSEF8071";	 //$NON-NLS-1$
	public static final String FILEMSG_IMPORT_FILTERING = "RSEF8072";	 //$NON-NLS-1$
	
    // --------------------------------
	// INFO-POPS FOR UNIVERSAL FILE
	// -------------------------------
	
	public static final String NEW_FILE_WIZARD     = "ufwf0000"; //$NON-NLS-1$
	public static final String NEW_FOLDER_WIZARD   = "ufwr0000"; //$NON-NLS-1$
	public static final String NEW_FILE_ACTION     = "ufaf0000"; //$NON-NLS-1$
	public static final String NEW_FOLDER_ACTION   = "ufar0000"; //$NON-NLS-1$
	
	
    // Remote File Exception Messages
  	public static final String FILEMSG_SECURITY_ERROR = "RSEF1001"; //$NON-NLS-1$
  	public static final String FILEMSG_IO_ERROR = "RSEF1002"; //$NON-NLS-1$
  	
  	public static final String FILEMSG_FOLDER_NOTEMPTY = "RSEF1003"; //$NON-NLS-1$
  	public static final String FILEMSG_FOLDER_NOTFOUND = "RSEF1004"; //$NON-NLS-1$
  	public static final String FILEMSG_FOLDER_NOTFOUND_WANTTOCREATE = "RSEF1005";  	 //$NON-NLS-1$
  	public static final String FILEMSG_FILE_NOTFOUND   = "RSEF1006";  	 //$NON-NLS-1$

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
	public static final String MSG_ERROR_FOLDER_NOTFOUND = "RSEG1105"; //$NON-NLS-1$
	public static final String MSG_ERROR_FILE_NOTFOUND = "RSEG1106"; 		 //$NON-NLS-1$
	public static final String MSG_ERROR_FOLDERORFILE_NOTFOUND = "RSEG1107"; //$NON-NLS-1$
	public static final String MSG_ERROR_ARCHIVEMANAGEMENT_NOTSUPPORTED = "RSEG1304"; 		 //$NON-NLS-1$
		
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
    public static final String MSG_GENERIC_I_TWOPARMS_HELP = "RSEO1005"; //$NON-NLS-1$
    public static final String MSG_GENERIC_W_TWOPARMS_HELP = "RSEO1006"; //$NON-NLS-1$
    public static final String MSG_GENERIC_E_TWOPARMS_HELP = "RSEO1007"; //$NON-NLS-1$
    public static final String MSG_GENERIC_U_TWOPARMS_HELP = "RSEO1008"; //$NON-NLS-1$
    public static final String MSG_GENERIC_Q_TWOPARMS_HELP = "RSEO1009"; //$NON-NLS-1$

    // ----------------------------------
    // COMMUNICATIONS ERROR CHECKING MESSAGES...
    // ----------------------------------
	public static final String MSG_COMM_CONNECT_FAILED 		= "RSEC1001"; //$NON-NLS-1$
	public static final String MSG_COMM_AUTH_FAILED 		= "RSEC1002"; //$NON-NLS-1$
	public static final String MSG_COMM_PWD_INVALID			= "RSEC1004"; //$NON-NLS-1$
	
	public static final String MSG_COMM_PWD_EXISTS			= "RSEC2101"; //$NON-NLS-1$
	public static final String MSG_COMM_PWD_MISMATCH		= "RSEC2102"; //$NON-NLS-1$
	public static final String MSG_COMM_PWD_BLANKFIELD		= "RSEC2103"; //$NON-NLS-1$

	public static final String MSG_COMM_ENVVAR_DUPLICATE	= "RSEC2001"; //$NON-NLS-1$
	public static final String MSG_COMM_ENVVAR_NONAME		= "RSEC2002"; //$NON-NLS-1$
	public static final String MSG_COMM_ENVVAR_INVALIDCHAR	= "RSEC2004"; //$NON-NLS-1$
	
	public static final String MSG_COMM_SERVER_NOTSTARTED	= "RSEC2301"; //$NON-NLS-1$
	public static final String MSG_COMM_INVALID_LOGIN		= "RSEC2302"; //$NON-NLS-1$
	
	public static final String MSG_COMM_INCOMPATIBLE_PROTOCOL = "RSEC2303"; //$NON-NLS-1$
	public static final String MSG_COMM_INCOMPATIBLE_UPDATE   = "RSEC2304"; //$NON-NLS-1$

	
	public static final String MSG_COMM_REXEC_NOTSTARTED      = "RSEC2305"; //$NON-NLS-1$
	
	public static final String MSG_COMM_PORT_WARNING          = "RSEC2306"; //$NON-NLS-1$
	
	public static final String MSG_COMM_SERVER_OLDER_WARNING  = "RSEC2308"; //$NON-NLS-1$
	public static final String MSG_COMM_CLIENT_OLDER_WARNING  = "RSEC2309"; //$NON-NLS-1$
	
	public static final String MSG_COMM_USING_SSL  = "RSEC2314"; //$NON-NLS-1$
	public static final String MSG_COMM_NOT_USING_SSL  = "RSEC2315"; //$NON-NLS-1$
	
	// Unexpected error message
	public static final String MSG_ERROR_UNEXPECTED = "RSEF8002"; //$NON-NLS-1$
	
	// Connection doesn't exist
	public static final String MSG_CONNECTION_DELETED = "RSEF5011"; //$NON-NLS-1$
	
	// Remote editing messages
	public static final String MSG_DOWNLOAD_NO_WRITE = "RSEF5002"; //$NON-NLS-1$
	public static final String MSG_DOWNLOAD_ALREADY_OPEN_IN_EDITOR = "RSEF5009"; //$NON-NLS-1$
	public static final String MSG_UPLOAD_FILE_EXISTS = "RSEF5012"; //$NON-NLS-1$
	
	public static final String MSG_FOLDER_UNREADABLE = "RSEF5020"; //$NON-NLS-1$
	
	// General error message
	public static final String MSG_ERROR_GENERAL = "RSEO1002"; //$NON-NLS-1$
	
	// file transfer message	
	public static final String MSG_TRANSFER_INVALID = "RSEG1270"; //$NON-NLS-1$
	
	
	// remote error list title message
	public static final String MSG_ERROR_LIST_TITLE = "RSEG1500"; //$NON-NLS-1$
	
	// name validation
	public static final String MSG_ERROR_EXTENSION_EMPTY = "RSEF6001"; //$NON-NLS-1$
	public static final String MSG_ERROR_FILENAME_INVALID = "RSEF6002"; //$NON-NLS-1$
	
	// cache preferences
	public static final String MSG_CACHE_UPLOAD_BEFORE_DELETE = "RSEF6101"; //$NON-NLS-1$
	public static final String MSG_CACHE_UNABLE_TO_SYNCH = "RSEF6102"; //$NON-NLS-1$
	
	// remote search messages
	public static final String MSG_REMOTE_SEARCH_INVALID_REGEX = "RSEG1601"; //$NON-NLS-1$
	
	// yantzi: artemis 6.0, offline messages
	public static final String MSG_OFFLINE_CANT_CONNECT			= "RSEC3001"; //$NON-NLS-1$
	
	// file import/export messages
	public static final String MSG_IMPORT_EXPORT_UNABLE_TO_USE_CONNECTION = "RSEF5101";  //$NON-NLS-1$
	public static final String MSG_IMPORT_EXPORT_UNEXPECTED_EXCEPTION = "RSEF5102"; //$NON-NLS-1$
	
	// jar export messages
	public static final String MSG_REMOTE_JAR_EXPORT_OVERWRITE_FILE = "RSEF5103"; //$NON-NLS-1$
}