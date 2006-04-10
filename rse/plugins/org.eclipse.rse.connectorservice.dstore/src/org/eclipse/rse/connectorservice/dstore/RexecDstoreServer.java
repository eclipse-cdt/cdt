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

package org.eclipse.rse.connectorservice.dstore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.Socket;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.client.ClientConnection;
import org.eclipse.dstore.core.client.ConnectionStatus;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.IIBMServerLauncher;
import org.eclipse.rse.core.subsystems.IServerLauncher;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.model.SystemSignonInformation;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;

/**
 * Launch Datastore server on selected host using the rexec
 * protocol
 */
public class RexecDstoreServer implements IServerLauncher
{
	private SystemMessage _errorMessage;
	private SystemSignonInformation signonInfo;
	//private String host = null;
	private String cwd = null;
	private String invocation = null;
	//private String userID = null;
	private int rexecPort = 512; // the port where rexecd normally listens
	private String cmd = null;
	//private String pwd = null;
	private static String ASCII_TEST_STRING = "ASCII";
	private static String PORT_LEADING_STRING = "Server Started Successfully";
	private static final String EZYRD11E="EZYRD11E";
	private ClientConnection clientConnection;
	private IServerLauncherProperties propertyInfo;
	private boolean isModeChecked = false;
	private boolean checkPort =true;
	private boolean logInfo = false;
	private int _socketTimeoutValue = IUniversalDStoreConstants.DEFAULT_PREF_SOCKET_TIMEOUT;

	private static char[] ebcdictounicode =
		{
			0x0000,
			0x0001,
			0x0002,
			0x0003,
			0x0000,
			0x0009,
			0x0000,
			0x007F,
			0x0000,
			0x0000,
			0x0000,
			0x000B,
			0x000C,
			0x000D,
			0x000E,
			0x000F,
			0x0010,
			0x0011,
			0x0012,
			0x0013,
			0x0000,
			0x000A,
			0x0008,
			0x0000,
			0x0018,
			0x0019,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x001C,
			0x0000,
			0x0000,
			0x000A,
			0x0017,
			0x001B,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0005,
			0x0006,
			0x0007,
			0x0000,
			0x0000,
			0x0016,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0004,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0014,
			0x0015,
			0x0000,
			0x001A,
			0x0020,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x002E,
			0x003C,
			0x0028,
			0x002B,
			0x007C,
			0x0026,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0021,
			0x0024,
			0x002A,
			0x0029,
			0x003B,
			0x0000,
			0x002D,
			0x002F,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x007C,
			0x002C,
			0x0025,
			0x005F,
			0x003E,
			0x003F,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0060,
			0x003A,
			0x0023,
			0x0040,
			0x0027,
			0x003D,
			0x0022,
			0x0000,
			0x0061,
			0x0062,
			0x0063,
			0x0064,
			0x0065,
			0x0066,
			0x0067,
			0x0068,
			0x0069,
			0x0000,
			0x007B,
			0x0000,
			0x0000,
			0x0000,
			0x002B,
			0x0000,
			0x006A,
			0x006B,
			0x006C,
			0x006D,
			0x006E,
			0x006F,
			0x0070,
			0x0071,
			0x0072,
			0x0000,
			0x007D,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0073,
			0x0074,
			0x0075,
			0x0076,
			0x0077,
			0x0078,
			0x0079,
			0x007A,
			0x0000,
			0x0000,
			0x0000,
			0x005B,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x005D,
			0x0000,
			0x002D,
			0x007D,
			0x0041,
			0x0042,
			0x0043,
			0x0044,
			0x0045,
			0x0046,
			0x0047,
			0x0048,
			0x0049,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x007D,
			0x004A,
			0x004B,
			0x004C,
			0x004D,
			0x004E,
			0x004F,
			0x0050,
			0x0051,
			0x0052,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x005C,
			0x0000,
			0x0053,
			0x0054,
			0x0055,
			0x0056,
			0x0057,
			0x0058,
			0x0059,
			0x005A,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0030,
			0x0031,
			0x0032,
			0x0033,
			0x0034,
			0x0035,
			0x0036,
			0x0037,
			0x0038,
			0x0039,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000,
			0x0000 };
			
	/**
	 * Constructor
	 */
	public RexecDstoreServer()
	{
		super();
	}
	
	/**
	 * Set the datastore client connection. This is reset for each connect()
	 */
	public void setClientConnection(ClientConnection clientConnection)
	{
		this.clientConnection = clientConnection;		
	}

	/**
	 * Set the remote system signon information
	 */
	public void setSignonInformation(SystemSignonInformation info)
	{
		this.signonInfo = info;
	}
	
	/**
	 * Get the remote system signon information, as set in
	 *  {@link #setSignonInformation(SystemSignonInformation)}
	 */
	public SystemSignonInformation getSignonInformation()
	{
		return signonInfo;
	}

	/**
	 * Set the object which contains the user-specified properties that 
	 *  are used by this launcher
	 */
	public void setServerLauncherProperties(IServerLauncherProperties propertyInfo)
	{
		this.propertyInfo = propertyInfo;
		// set path...
		this.cwd = ((IIBMServerLauncher)propertyInfo).getServerPath();
		char separatorChar = signonInfo.getSystemType().equals("Windows") ? '\\' : '/';			
		if (cwd.length() > 0 && cwd.charAt(cwd.length() - 1) != separatorChar)
			cwd += separatorChar;
	    // set script...
	    this.invocation = ((IIBMServerLauncher)propertyInfo).getServerScript();  
	}
	
	/**
	 * Get the object which contians the user-specified properties that are
	 *  used by this launcher. As set in {@link #setServerLauncherProperties(IServerLauncherProperties)}. 
	 */
	public IServerLauncherProperties getServerLauncherProperties()
	{
		return propertyInfo;
	}

	/**
	 * Determine if the remote server needs to be launched or not.
	 * Generally is always false.
	 */
	public boolean isLaunched()
	{
		return false;
	}
		
	/**
	 * Send a command to the host via rexec to launch the datastore server
	 * under the specified user ID/pwd. the datastore server will emit 
	 * messages that include the port number on which the server is listening
	 * for client connections
	 * 
	 * @return port number as String
	 */
	/*
	 * There used to be a problem in IBM Communications Server for z/OS and 
	 * the message from the REXEC daemon was in EBCDIC. see APAR PQ76782.
	 * Since this problem was fixed in Communication Server, there is no need to convert EBCDIC to ASCII
	 */
	// updated method
	public Object launch(IProgressMonitor monitor) throws Exception
	{	
		
		boolean isEBCDICTest=false;
		isModeChecked= false;
		checkPort=true;
		_errorMessage = null;
		String port = new String("0"); // default no port
		//String hostResponse = "";	//buffer to hold all the messages, so that it can be printed out later 
		String originalHostResponse="";
		String convertedHostResponse="";
		String debugOptions = System.getProperty("REXEC_DEBUG");
		if (debugOptions!= null){
			if (debugOptions.toUpperCase().indexOf("LOG") > -1)
				logInfo = true;
			if (debugOptions.toUpperCase().indexOf("EBCDIC") > -1)
				isEBCDICTest=true;
		}
		boolean isEBCDIC = false;
		try
		{

			// establish socket for rexec connection
			Socket rexecCall = new Socket(signonInfo.getHostname(), rexecPort); // rexec listens here - 512

			// set socket timeout value
			rexecCall.setSoTimeout(_socketTimeoutValue);
			
			// set up data streams on rexec socket
			DataOutputStream rxOut = new DataOutputStream(rexecCall.getOutputStream());
			DataInputStream rxIn = new DataInputStream(rexecCall.getInputStream());

			// we're not opening a socket for stderr to circumvent problems that
			// may arise if the client is behind a firewall with respect to the
			// host, in which case the rexec daemon may have trouble connecting 
			// to the client error port.
			// Not sending a port number at this point indicates to the daemon
			// that there is no error socket to establish.
			rxOut.writeByte((int) 0); // send null terminator
			rxOut.flush();

			// send userid and password on rexec socket
			rxOut.writeBytes(signonInfo.getUserid());
			rxOut.writeByte((int) 0); // send null terminator
			rxOut.writeBytes(signonInfo.getPassword());
			rxOut.writeByte((int) 0); // send null terminator
			rxOut.flush();

			// send the command on rexec socket to start datastore daemon listening
			// on any port
			// TODO - assumes a particular script and location to start the server,
			// this should be stored in some resource bundle later 
			//cmd = new String ("echo USSTEST;cd ~/dstore;start_anyport");
			//cmd = new String("echo " + ASCII_TEST_STRING + ";cd ~/rseserver;start_anyport");
			cmd = new String("echo " + ASCII_TEST_STRING + ";cd " + this.cwd + ";" + this.invocation);
			logMessage("The command is " + cmd);
			SystemBasePlugin.logInfo("RexecDstoreServer :");

			rxOut.writeBytes(cmd);
			rxOut.writeByte((int) 0); // send null terminator
			rxOut.flush();

			int inBytes = rxIn.available(); // any data available?
			
			int timeout = 600;  //  60 second to connect
			
			while (inBytes == 0 && timeout > 0)
			{
				if (monitor.isCanceled()) // Cancel button pressed?
					return "0";
				
				// try for more input
				Thread.sleep(100);
				inBytes = rxIn.available();
				timeout--;
			}
			
			if (timeout == 0) {
				SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_COMM_INVALID_LOGIN);
				msg.makeSubstitution(signonInfo.getHostname(), "");
				_errorMessage = msg;
				return port;
			}

			// get command output on socket, one byte at a time until
			// got the datastore port number or EOF is reached for this input 
			String maybePort=null;
			while (true ){  
				if (monitor.isCanceled())
					return "0";
				byte aByte = rxIn.readByte();
				if (isEBCDICTest)
					aByte = convertFromASCIIToEBCDIC(aByte); // for EBCDIC Test
				if (aByte == 0) // drop the null
					continue;
				originalHostResponse += (char)aByte;
				logMessage("Host response is " + originalHostResponse);
				if (!isModeChecked)
				{
					convertedHostResponse += convertFromEBCDICToASCII(aByte);
					logMessage("Host response is converted to " + convertedHostResponse);
					if (originalHostResponse.indexOf(ASCII_TEST_STRING) > -1) 
					{  // It's ASCII mode
						isModeChecked = true;
						logMessage("This is the ASCII mode. ");
					}
					else if (convertedHostResponse.indexOf(ASCII_TEST_STRING) > -1)
					{ // It's EBCDIC mode
						logMessage("This is the EBCDIC mode. ");
						isModeChecked = true;
						isEBCDIC = true;
					}
				} 
				else 
				{
					if (isEBCDIC)
					{
						convertedHostResponse += convertFromEBCDICToASCII(aByte);
						logMessage("Host response is converted to " + convertedHostResponse);
						if(checkPort)
						{ // It's EBCDIC mode   
							maybePort = extractPortNumber (convertedHostResponse);
						}
					} 
					else if (checkPort)
					{  // it's ASCII
						maybePort = extractPortNumber (originalHostResponse);
					}
					if (maybePort == null)
						continue ;
					port = maybePort;
					break;
				}
			}
			// -----------------------------------------------------------------
			// Close input/output streams and socket
			// -----------------------------------------------------------------
			rxIn.close();
			rxOut.close();
			rexecCall.close();
			logMessage("Going to return port " + port);
			return port;
		} 
		catch (EOFException e) 
		{
			// do nothing 
		} 
		catch (Exception e)		
		{
			e.printStackTrace();
		}
		// if no port is found, create error message
		String hostMessage = originalHostResponse;
		if (isEBCDIC) // pick up the right messages
			hostMessage = convertedHostResponse;
		int index = hostMessage.indexOf(ASCII_TEST_STRING);
		if (index > -1)  // remove the embedded ASCII_TEST_STRING
			hostMessage = hostMessage.substring(0,index) + hostMessage.substring(index+1+ASCII_TEST_STRING.length());
		if (hostMessage.indexOf(EZYRD11E) >0 ){
			SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_COMM_INVALID_LOGIN);
			msg.makeSubstitution(signonInfo.getHostname(), hostMessage);
			_errorMessage = msg;
		} else {
			SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_COMM_REXEC_NOTSTARTED);
			msg.makeSubstitution(""+rexecPort, signonInfo.getHostname(), hostMessage);
			_errorMessage = msg;
			
		}

		return port;
	} 
	/**
	 * @param newLine
	 * @return
	 */
	/*
	private String checkCodePage(String newLine) {
		// check in which mode the host is sending the message back 
		if (isModeChecked) 
		return newLine;
		if (newLine.indexOf(ASCII_TEST_STRING) > -1){   // It's ASCII mode
			isASCIIMode = true;
			isModeChecked = true;
		} else  { 				// Check whether it's EBCDIC mode
			String convertedNewLine = convertFromEBCDICToASCII(newLine.toCharArray());
			if (convertedNewLine.indexOf(ASCII_TEST_STRING) > -1){   
				isASCIIMode = false;
				isModeChecked = true;
			}
		}
		return newLine;
	}
	*/
	/* wait until host responds with some data - at least should send null byte */
/*			char cChar;
			int inBytes = rxIn.available(); // any data available? 
			StringBuffer buf = new StringBuffer();
			String chunk = null;

			while (inBytes == 0)
			{
				if (monitor.isCanceled())
					return "0";
					
				// try for more input
				Thread.sleep(100);

				inBytes = rxIn.available();
			}

			// get command output on socket, one byte at a time until
			// got the datastore port number or EOF is reached for this input 
			byte[] bDSPort = new byte[4];
			byte[] byteBuf = new byte[8];
			int index;
			if (inBytes > 0)
			{
				byte aByte = rxIn.readByte();
				cChar = (char)aByte; // discard first null byte
				
//				port = getPortASCII(rxIn);
				// should have at least 8 bytes plus newline from the "echo USSTEST" command. This is
				// used to test if the data being returned is ASCII or EBCDIC
				for (index = 0; index < ASCII_TEST_STRING.length(); index++)
				{ // include newline too
					aByte = rxIn.readByte();
					cChar = (char)aByte;
					buf.append(cChar);
					byteBuf[index] = aByte;
				}
				chunk = new String(buf);

				if (chunk.indexOf(ASCII_TEST_STRING) >= 0)
				{
					// returned data is ASCII
					port = getPortASCII(rxIn);
				}
				else
				{

					chunk = convertFromEBCDICToASCII(byteBuf);
					if (chunk.indexOf(ASCII_TEST_STRING) >= 0)
					{

						// returned data is EBCDIC
						port = getPortEBCDIC(rxIn);
					}
					else
					{
						// rexec error!
						// there's an error
						while (cChar != '\n')
						{
							cChar = (char) rxIn.readByte();
							buf.append(cChar);
						}
						SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_COMM_INVALID_LOGIN);
						msg.makeSubstitution(signonInfo.getHostname(), buf.toString());
						_errorMessage = msg;
					}
				}
			}
			rxIn.close();
			rxOut.close();
			rexecCall.close();

		}
		catch (Exception e)
		{
			System.out.println(e);
			SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_COMM_REXEC_NOTSTARTED);
			msg.makeSubstitution(""+rexecPort, signonInfo.getHostname(), e.getMessage());
			_errorMessage = msg;
		}

		return port;
	} // end of launch method
	*/
	/* original method
	public Object launch(IProgressMonitor monitor) throws Exception
	{
		_errorMessage = null;
		String port = new String("0"); // default no port
		try
		{

			// establish socket for rexec connection
			Socket rexecCall = new Socket(signonInfo.getHostname(), rexecPort); // rexec listens here - 512

			// set up data streams on rexec socket
			DataOutputStream rxOut = new DataOutputStream(rexecCall.getOutputStream());
			DataInputStream rxIn = new DataInputStream(rexecCall.getInputStream());

			// we're not opening a socket for stderr to circumvent problems that
			// may arise if the client is behind a firewall with respect to the
			// host, in which case the rexec daemon may have trouble connecting 
			// to the client error port.
			// Not sending a port number at this point indicates to the daemon
			// that there is no error socket to establish.
			rxOut.writeByte((int) 0); // send null terminator
			rxOut.flush();

			// send userid and password on rexec socket
			rxOut.writeBytes(signonInfo.getUserid());
			rxOut.writeByte((int) 0); // send null terminator
			rxOut.writeBytes(signonInfo.getPassword());
			rxOut.writeByte((int) 0); // send null terminator
			rxOut.flush();

			// send the command on rexec socket to start datastore daemon listening
			// on any port
			// TODO - assumes a particular script and location to start the server,
			// this should be stored in some resource bundle later 
			//cmd = new String ("echo USSTEST;cd ~/dstore;start_anyport");
			//cmd = new String("echo " + ASCII_TEST_STRING + ";cd ~/rseserver;start_anyport");
			cmd = new String("echo " + ASCII_TEST_STRING + ";cd " + this.cwd + ";" + this.invocation);

			rxOut.writeBytes(cmd);
			rxOut.writeByte((int) 0); // send null terminator
			rxOut.flush();

			// wait until host responds with some data - at least should send null byte 

			char cChar;
			int inBytes = rxIn.available(); // any data available? 
			StringBuffer buf = new StringBuffer();
			String chunk = null;

			while (inBytes == 0)
			{
				if (monitor.isCanceled())
					return "0";
					
				// try for more input
				Thread.sleep(100);

				inBytes = rxIn.available();
			}

			// get command output on socket, one byte at a time until
			// got the datastore port number or EOF is reached for this input 
			byte[] bDSPort = new byte[4];
			byte[] byteBuf = new byte[8];
			int index;
			if (inBytes > 0)
			{
				byte aByte = rxIn.readByte();
				cChar = (char)aByte; // discard first null byte

				// should have at least 8 bytes plus newline from the "echo USSTEST" command. This is
				// used to test if the data being returned is ASCII or EBCDIC
				for (index = 0; index < ASCII_TEST_STRING.length(); index++)
				{ // include newline too
					aByte = rxIn.readByte();
					cChar = (char)aByte;
					buf.append(cChar);
					byteBuf[index] = aByte;
				}
				chunk = new String(buf);

				if (chunk.indexOf(ASCII_TEST_STRING) >= 0)
				{
					// returned data is ASCII
					port = getPortASCII(rxIn);
				}
				else
				{

					chunk = convertFromEBCDICToASCII(byteBuf);
					if (chunk.indexOf(ASCII_TEST_STRING) >= 0)
					{

						// returned data is EBCDIC
						port = getPortEBCDIC(rxIn);
					}
					else
					{
						// rexec error!
						// there's an error
						while (cChar != '\n')
						{
							cChar = (char) rxIn.readByte();
							buf.append(cChar);
						}
						SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_COMM_INVALID_LOGIN);
						msg.makeSubstitution(signonInfo.getHostname(), buf.toString());
						_errorMessage = msg;
					}
				}

			}
			rxIn.close();
			rxOut.close();
			rexecCall.close();

		}
		catch (Exception e)
		{
			System.out.println(e);
			SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_COMM_REXEC_NOTSTARTED);
			msg.makeSubstitution(""+rexecPort, signonInfo.getHostname(), e.getMessage());
			_errorMessage = msg;
		}

		return port;
	} // end of launch method
*/
	/**
	 * Determine if we are connected to the remote server or not.  
	 * @return true if we are connected, false otherwise.
	 */
	public boolean isConnected()
	{
		if (clientConnection != null)
		{
			return clientConnection.isConnected();
		}
		return false;		
	}
	
	/**
	 * Connect to the remote server. 
	 * @see #getErrorMessage()
	 * @param monitor a monitor for showing progress
	 * @param connectPort the port to use for launching the server
	 * @return Anything you want.
	 */
	public Object connect(IProgressMonitor monitor, int connectPort) throws Exception
	{
		clientConnection.setPort(Integer.toString(connectPort));
				
		if (monitor != null)
		{
			SystemMessage cmsg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_CONNECTING_TO_SERVER);
			cmsg.makeSubstitution(clientConnection.getPort());
			monitor.subTask(cmsg.getLevelOneText());
		}
				
		// connect to launched server
		ConnectionStatus connectStatus = clientConnection.connect(null);
		
		return connectStatus;
	}

	/**
	 * Disconnect from the remote server
	 * @see #getErrorMessage()
	 */
	public void disconnect() throws Exception
	{
		if (clientConnection != null)
		{ 
			/* TODO!
			// Is disconnect being called because the network (connection) went down?
			if (_connectionStatusListener != null && _connectionStatusListener.isConnectionDown())
			{
				fireCommunicationsEvent(CommunicationsEvent.CONNECTION_ERROR);
			}
			else
			{
				// Fire comm event to signal state about to change
				fireCommunicationsEvent(CommunicationsEvent.BEFORE_DISCONNECT);
			}

			DataStore dataStore = getDataStore();
			if (dataStore != null && _connectionStatusListener != null)
			{
				dataStore.getDomainNotifier().removeDomainListener(_connectionStatusListener);
			}

			clientConnection.disconnect();
			clientConnection = null;
			getUserId(); // Clear any cached local user IDs
			sysInfo = null;

			// Fire comm event to signal state changed
			fireCommunicationsEvent(CommunicationsEvent.AFTER_DISCONNECT);
			*/
			clientConnection.disconnect();
			clientConnection = null;
		}
	}
	
	/**
	 * Return the last error message issued
	 */
	public SystemMessage getErrorMessage()
	{
		return _errorMessage;
	}
	
	// ------------------
	// PRIVATE METHODS...
	// ------------------
/*
	// sam private String convertFromEBCDICToASCII(byte[] eBytes)
	private String convertFromEBCDICToASCII(char[] eBytes)
	{
	
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < eBytes.length; i++)
		{
			byte bByte = (byte)eBytes[i];
			int index = bByte;
			if (index < 0)
			{
				index = 256 + index;
			}
			char cChar = ebcdictounicode[index];
			buf.append(cChar);
		}
		return buf.toString();
	}	
*/
	/**
	 * Read the data returned on the rexec port as ASCII characters and
	 * detect the port number in the datastore server messages
	 * @param rxIn
	 * @return String port number
	 */
	/*
	private String getPortASCII(DataInputStream rxIn)
	{
		String port = new String("0"); // default to no port
		char cChar;
		byte bByte;
		int numPortChars = 5;
		byte[] bDSPort = new byte[numPortChars];
		StringBuffer buf = new StringBuffer();
		StringBuffer diagnosticString = new StringBuffer();
		String chunk = null;
		boolean serverStartedMsg = false;
		while (true)
		{
			try
			{
				cChar = (char) rxIn.readByte();
				if (cChar == '\n')
				{ // hit a newline
					chunk = new String(buf);
				
					// DKM: need to handle mixed order cases

					if (chunk.indexOf("Server Started Successfully") >= 0)
					{
						serverStartedMsg = true;
						// this server output precedes the datastore server port number
						buf.delete(0, buf.length()); // clear buffer

						// might have already got port
						// check first
						if (port.equals("0"))
						{
							rxIn.read(bDSPort, 0, numPortChars); // get next 4 bytes - datastore port #
							for (int i = 0; i < numPortChars; i++)
							{
								char c = (char) bDSPort[i];
								if (Character.isDigit(c))
									buf.append(c);
							}
							port = new String(buf); // got port where datastore server is listening

							// check for valid port
							try
							{
								int possiblePort = Integer.parseInt(port);
								break;
								
							}
							catch (Exception e)
							{
								// not valid												
							}
						}
					
					}
					else
					{
						// might be the port
						try
						{
							int possiblePort = Integer.parseInt(chunk);
							port = chunk;
							if (serverStartedMsg)
								break;
						}
						catch (Exception e)
						{
						}
					}

					buf.delete(0, buf.length()); // clear buffer
				}
				else
				{
					diagnosticString.append(cChar);
					buf.append(cChar);
				}
			}
			// EOF indicates no more lines to come through on error socket
			catch (EOFException e)
			{
				break;
			}
			catch (IOException e)
			{
				break;
			}
		} // end of while true

		// if port is somethign wierd...log this
		if (port.equals("0"))
		{
			// port is weird!

			try
			{

				int available = rxIn.available();
				while (available > 0)
				{
					rxIn.read(bDSPort, 0, numPortChars);
					for (int i = 0; i < numPortChars; i++)
					{
						char c = (char) bDSPort[i];
						diagnosticString.append(c);
					}
					available = rxIn.available();
				}

			}
			catch (Exception ex)
			{
			}

			SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_COMM_SERVER_NOTSTARTED);
			diagnosticString.insert(0, "\n\n");
			msg.makeSubstitution(signonInfo.getHostname(), diagnosticString.toString());

			_errorMessage = msg;
		}

		return port;
	}
*/
	/**
	 * Read the data returned on the rexec port as EBCDIC characters and
	 * convert to unicode. Detect the port number in the datastore server messages
	 * @param rxIn
	 * @return String port number
	 */
	/*
	private String getPortEBCDIC(DataInputStream rxIn)
	{
		String port = new String("0"); // default to no port
		int index;
		char cChar;
		byte bByte;
		byte[] bDSPort = new byte[4];
		StringBuffer buf = new StringBuffer();
		String chunk = null;
		while (true)
		{
			try
			{
				bByte = rxIn.readByte();
				index = bByte;
				if (index < 0)
				{
					index = 256 + index;
				}
				cChar = ebcdictounicode[index];
				if (cChar == '\n')
				{ // hit a newline
					chunk = new String(buf);

					if (chunk.indexOf("Server Started Successfully") >= 0)
					{
						// this server output precedes the datastore server port number
						buf.delete(0, buf.length()); // clear buffer
						rxIn.read(bDSPort, 0, 4); // get next 4 bytes - datastore port #
						for (int i = 0; i < 4; i++)
						{
							index = bDSPort[i];
							if (index < 0)
							{
								index = 256 + index;
							}
							buf.append(ebcdictounicode[index]);
						}
						port = new String(buf); // got port where datastore server is listening
						break;
					}
					buf.delete(0, buf.length()); // clear buffer
				}
				else
				{
					buf.append(cChar);
				}
			}
			// EOF indicates no more lines to come through on error socket
			catch (EOFException e)
			{
				break;
			}
			catch (IOException e)
			{
				break;
			}
		} // end of while true
		return port;
	}
	*/
	private String extractPortNumber (String hostResponse) {
		String port ="0";
		logMessage("Going to find port number. ");
		int index = hostResponse.indexOf(PORT_LEADING_STRING);
		if ( index < 0 ) 
			return null;
		logMessage("Found the leading string. ");
		String portString = hostResponse.substring(index + PORT_LEADING_STRING.length());
		logMessage("Removed the leading string as " +  portString);
		if (portString != null && portString.startsWith("\n"))
			portString = portString.substring(1);
		//if (portString.length() < 4) 
		//	return null;
		
		// change to support 5 digit ports
		StringBuffer portBuffer = new StringBuffer();
		for (int i = 0; i < portString.length(); i++)
		{
		    char c = portString.charAt(i);
		    if (Character.isDigit(c))
		    {
		        portBuffer.append(c);
		    }		    
		}
		
		if (portString.length() != portBuffer.length())
		{
			port = portBuffer.toString();
			// old code - didn't support 5 digits
			//port = portString.substring(0,4);
			
			logMessage("Got the port " +  port);
			try 
			{
				int possiblePort = Integer.parseInt(port);
				logMessage("Going to return port " +  port);
				return port;
				//logMessage("Return the port " + port);
			} 
			catch (RuntimeException e) 
			{
				e.printStackTrace();
				logMessage("Got the wrong port " +  port);
				checkPort=false;
				return null;
			}
		}
		else
		{
		    return null;
		}
	}
	private char convertFromEBCDICToASCII(byte eByte)
	{

		StringBuffer buf = new StringBuffer();
			int index = eByte;
			if (index < 0)
			{
				index = 256 + index;
			}
			return ebcdictounicode[index];
	}
	private byte convertFromASCIIToEBCDIC(byte eByte)
	{
			int index = eByte;
			for (int i = 0; i <ebcdictounicode.length; i++) {
				if (index ==(int)ebcdictounicode[i]){
					if (i > 128)
						return (byte )(i - 256);
					return (byte)i;
				}
				
			}
			return (byte)0;
	}
	private void logMessage (String message) {
		if (logInfo)
			SystemBasePlugin.logError("RexecDstoreServer :" + message);
	}
	
	public void setSocketTimeoutValue(int value)
	{
		_socketTimeoutValue = value;
	}
}