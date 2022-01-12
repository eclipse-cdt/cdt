/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.proxy.protocol.core;

public class Protocol {
	public static final int MAGIC = 0xAFFA;
	
	public final static byte PROTO_COMMAND = 	1;
	public final static byte PROTO_SHUTDOWN = 	2;
	public final static byte PROTO_ERROR = 		126;
	public final static byte PROTO_OK = 		0;
	
	public final static short CmdBase = 		100;
	
	public final static short CMD_EXEC = 			CmdBase + 1;
	public final static short CMD_SHELL = 			CmdBase + 2;
	public final static short CMD_GETCWD = 			CmdBase + 3;
	public final static short CMD_GETENV = 			CmdBase + 4;
	public final static short CMD_CHILDINFOS = 		CmdBase + 5;
	public final static short CMD_DELETE = 			CmdBase + 6;
	public final static short CMD_FETCHINFO = 		CmdBase + 7;
	public final static short CMD_GETINPUTSTREAM = 	CmdBase + 8;
	public final static short CMD_GETOUTPUTSTREAM = 	CmdBase + 9;
	public final static short CMD_MKDIR = 			CmdBase + 10;
	public final static short CMD_PUTINFO = 			CmdBase + 11;
	public final static short CMD_GETPROPERTIES = 	CmdBase + 12;
	
	/**
	 * @since 2.0
	 */
	public final static byte CONTROL_KILL = 0;
	/**
	 * @since 2.0
	 */
	public final static byte CONTROL_SETTERMINALSIZE = 1;
}
