/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared                                
 *******************************************************************************/
package org.eclipse.rse.internal.useractions;

public interface IUserActionsMessageIds {

	public static final String MSG_VALIDATE_UDANAME_EMPTY    = "RSEG1180"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_UDANAME_NOTUNIQUE= "RSEG1181"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_UDANAME_NOTVALID = "RSEG1182"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_UDACMT_EMPTY    = "RSEG1183"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_UDACMT_NOTVALID = "RSEG1184"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_UDACMD_EMPTY    = "RSEG1185"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_UDACMD_NOTVALID = "RSEG1186"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_UDTNAME_EMPTY    = "RSEG1187"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_UDTNAME_NOTUNIQUE= "RSEG1188"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_UDTNAME_NOTVALID = "RSEG1189"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_UDTTYPES_EMPTY    = "RSEG1190"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_UDTTYPES_NOTVALID = "RSEG1191"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_COMPILELABEL_EMPTY    = "RSEG1195"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_COMPILELABEL_NOTUNIQUE= "RSEG1196"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_COMPILELABEL_NOTVALID = "RSEG1197"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_COMPILESTRING_EMPTY    = "RSEG1198"; //$NON-NLS-1$
	public static final String MSG_VALIDATE_COMPILESTRING_NOTVALID = "RSEG1199"; //$NON-NLS-1$
	public static final String MSG_UDA_LOAD_ERROR = "RSEG1140";	 //$NON-NLS-1$
	public static final String MSG_UDA_ROOTTAG_ERROR = "RSEG1141";	 //$NON-NLS-1$
	public static final String MSG_CONFIRM_DELETE_USERACTION = "RSEG1230";	 //$NON-NLS-1$
	public static final String MSG_CONFIRM_DELETE_USERTYPE   = "RSEG1231";	 //$NON-NLS-1$

}
