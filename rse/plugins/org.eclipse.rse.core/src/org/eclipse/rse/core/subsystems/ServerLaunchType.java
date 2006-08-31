/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.core.subsystems;
import java.util.Arrays;
import java.util.List;




public final class ServerLaunchType 
{
	/**
	 * The '<em><b>Daemon</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * The server code is to be launched by calling a daemon that is listening on a port.
	 * <!-- end-user-doc -->
	 * @see #DAEMON_LITERAL
	 * @model name="Daemon"
	 * @generated
	 * @ordered
	 */
	public static final int DAEMON = 0;

	/**
	 * The '<em><b>Rexec</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * The server code is to be launched using REXEC
	 * <!-- end-user-doc -->
	 * @see #REXEC_LITERAL
	 * @model name="Rexec"
	 * @generated
	 * @ordered
	 */
	public static final int REXEC = 1;

	/**
	 * The '<em><b>Running</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * The server code is to already running, and doesn't need to be launched.
	 * <!-- end-user-doc -->
	 * @see #RUNNING_LITERAL
	 * @model name="Running"
	 * @generated
	 * @ordered
	 */
	public static final int RUNNING = 2;

	/**
	 * The '<em><b>Telnet</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * The server code is to be launched using TELNET.
	 * <!-- end-user-doc -->
	 * @see #TELNET_LITERAL
	 * @model name="Telnet"
	 * @generated
	 * @ordered
	 */
	public static final int TELNET = 3;

	/**
	 * The '<em><b>SSH</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * The server code is to be launched using SSH.
	 * <!-- end-user-doc -->
	 * @see #SSH_LITERAL
	 * @model 
	 * @generated
	 * @ordered
	 */
	public static final int SSH = 4;

	/**
	 * The '<em><b>FTP</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * The server code is to be launched using FTP
	 * <!-- end-user-doc -->
	 * @see #FTP_LITERAL
	 * @model 
	 * @generated
	 * @ordered
	 */
	public static final int FTP = 5;

	/**
	 * The '<em><b>HTTP</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * The server code is to be launched using HTTP
	 * <!-- end-user-doc -->
	 * @see #HTTP_LITERAL
	 * @model 
	 * @generated
	 * @ordered
	 */
	public static final int HTTP = 6;

	/**
	 * The '<em><b>Daemon</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <p>
	 * The server code is to be launched by calling a daemon that is listening on a port.
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #DAEMON
	 * @generated
	 * @ordered
	 */
	public static final ServerLaunchType DAEMON_LITERAL = new ServerLaunchType(DAEMON, "Daemon");

	/**
	 * The '<em><b>Rexec</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <p>
	 * The server code is to be launched using REXEC
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #REXEC
	 * @generated
	 * @ordered
	 */
	public static final ServerLaunchType REXEC_LITERAL = new ServerLaunchType(REXEC, "Rexec");

	/**
	 * The '<em><b>Running</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <p>
	 * The server code is to already running, and doesn't need to be launched.
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #RUNNING
	 * @generated
	 * @ordered
	 */
	public static final ServerLaunchType RUNNING_LITERAL = new ServerLaunchType(RUNNING, "Running");

	/**
	 * The '<em><b>Telnet</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <p>
	 * The server code is to be launched using TELNET.
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #TELNET
	 * @generated
	 * @ordered
	 */
	public static final ServerLaunchType TELNET_LITERAL = new ServerLaunchType(TELNET, "Telnet");

	/**
	 * The '<em><b>SSH</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>SSH</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #SSH
	 * @generated
	 * @ordered
	 */
	public static final ServerLaunchType SSH_LITERAL = new ServerLaunchType(SSH, "SSH");

	/**
	 * The '<em><b>FTP</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <p>
	 * The server code is to be launched using FTP
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #FTP
	 * @generated
	 * @ordered
	 */
	public static final ServerLaunchType FTP_LITERAL = new ServerLaunchType(FTP, "FTP");

	/**
	 * The '<em><b>HTTP</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <p>
	 * The server code is to be launched using HTTP
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #HTTP
	 * @generated
	 * @ordered
	 */
	public static final ServerLaunchType HTTP_LITERAL = new ServerLaunchType(HTTP, "HTTP");

	/**
	 * An array of all the '<em><b>Server Launch Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final ServerLaunchType[] VALUES_ARRAY =
		new ServerLaunchType[]
		{
			DAEMON_LITERAL,
			REXEC_LITERAL,
			RUNNING_LITERAL,
			TELNET_LITERAL,
			SSH_LITERAL,
			FTP_LITERAL,
			HTTP_LITERAL,
		};

	private String _name;
	private int _value;
	
	/**
	 * A public read-only list of all the '<em><b>Server Launch Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List VALUES = Arrays.asList(VALUES_ARRAY);

	/**
	 * Returns the '<em><b>Server Launch Type</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ServerLaunchType get(String name)
	{
		for (int i = 0; i < VALUES_ARRAY.length; ++i)
		{
			ServerLaunchType result = VALUES_ARRAY[i];
			if (result.getName().equals(name))
			{
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Server Launch Type</b></em>' literal with the specified value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ServerLaunchType get(int value)
	{
		switch (value)
		{
			case DAEMON: return DAEMON_LITERAL;
			case REXEC: return REXEC_LITERAL;
			case RUNNING: return RUNNING_LITERAL;
			case TELNET: return TELNET_LITERAL;
			case SSH: return SSH_LITERAL;
			case FTP: return FTP_LITERAL;
			case HTTP: return HTTP_LITERAL;
		}
		return null;	
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getType()
	{
		return _value;
	}

	private ServerLaunchType(int value, String name)
	{
		_name = name;
		_value = value;
	
	}

} //ServerLaunchType