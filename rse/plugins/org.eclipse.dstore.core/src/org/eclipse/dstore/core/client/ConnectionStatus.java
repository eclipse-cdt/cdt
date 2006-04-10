/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.dstore.core.client;

import java.util.List;

/**
 * ConnectionStatus represents the state of a connection.  This class is
 * used for feedback, when a client attempts to connect to a server.
 */
public class ConnectionStatus
{

	private boolean _connected;
	private Throwable _exception;
	private String _message;
	private String _ticket;
	private boolean _SSLProblem = false;
	private List _untrustedCertificates;

	/**
	 * Constructor
	 * @param connected indicates whether a connection has been made
	 */
	public ConnectionStatus(boolean connected)
	{
		_connected = connected;
	}

	/**
	 * Constructor
	 * @param connected indicates whether a connection has been made
	 * @param e the exception that occurred when attempting to connect
	 */
	public ConnectionStatus(boolean connected, Throwable e)
	{
		_connected = connected;
		_exception = e;
		_message = e.toString();
	}

	/**
	 * Constructor
	 * @param connected indicates whether a connection has been made
	 * @param msg a connection error message
	 */
	public ConnectionStatus(boolean connected, String msg)
	{
		_connected = connected;
		_message = msg;
	}
	
	public ConnectionStatus(boolean connected, Throwable e, boolean sslProblem, List untrustedCerts)
	{
		_connected = connected;
		_exception = e;
		_message = e.toString();
		_SSLProblem = sslProblem;
		_untrustedCertificates = untrustedCerts;
	}

	/**
	 * Sets whether the connection is successful or not
	 * @param flag indication of whether the connection is successful
	 */
	public void setConnected(boolean flag)
	{
		_connected = flag;
	}

	/**
	 * Sets the connection error message
	 * @param message the error message
	 */
	public void setMessage(String message)
	{
		_message = message;
	}

	/**
	 * Sets the ticket to use when connecting to a server.  Typically,
	 * a ticket gets sent back from a server daemon so that the client
	 * can be granted access to the launched server DataStore
	 * @param ticket the ticket
	 */
	public void setTicket(String ticket)
	{
		_ticket = ticket;
	}

	/**
	 * Indicates whether the connection was successful or not
	 * @return whether the connection was successful or not
	 */
	public boolean isConnected()
	{
		return _connected;
	}

	/**
	 * Returns the error message for a connection attempt
	 * @return the error message
	 */
	public String getMessage()
	{
		return _message;
	}

	/**
	 * Returns the ticket required for connecting to a server
	 * @return the ticket
	 */
	public String getTicket()
	{
		return _ticket;
	}
	
	/*
	 * Returns the exception if there is one
	 * @return the exception
	 */
	public Throwable getException()
	{
		return _exception;
	}
	
	public boolean isSLLProblem()
	{
		return _SSLProblem;
	}
	
	public List getUntrustedCertificates()
	{
		return _untrustedCertificates;
	}
}