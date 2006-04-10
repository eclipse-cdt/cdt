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

package org.eclipse.rse.core.comm;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.actions.DisplaySystemMessageAction;
import org.eclipse.rse.ui.actions.SystemStartCommunicationsDaemonAction;
import org.eclipse.swt.widgets.Display;

 
/**
 * Communications daemon that can be used by other functions for receiving incoming socket 
 * requests.  Functions need to implement the ICommunicationsDaemonHandler interface and register
 * a unique 4 byte id (as an integer) which the server sends across the socket to let the
 * daemon know which local function to pass the socket off to.
 */ 
public class SystemCommunicationsDaemon extends Thread {

	
	private static final String THREAD_NAME = "RSE daemon";	

	// Communication Daemon Request Handlers
	private static Map handlers = new Hashtable(50);
	private static List _listeners = new Vector(10);
	
	// Starting point for dynamically generated keys
	private static int _nextKey = 0xD0000000;
	
	// Instance fields
	private static SystemCommunicationsDaemon inst = null;
	private boolean halt = false;
	private ServerSocket serverSocket;
	private boolean displayErrors = true;
	
	// We need to hold a reference to the _startAction in order to change the "Stop Daemon" label
	// to "Start Daemon" if the daemon fails.
	private static SystemStartCommunicationsDaemonAction _startAction;
	
	/**
	 * Inner class for running ICommunicationHandlers on a new thread.  This is done 
	 * for two main reasons:
	 * 		1.  Allow the communication daemon to continue handling requests while 
	 * 	 	    processing other requests (this is important because some requests
	 * 			like the program verifiers run long.
	 * 		2.  Keep the communications daemon safe from crashes (unhandled exceptions)
	 * 			in the communication handlers.
	 */
	protected class CommunicationHandlerThread extends Thread
	{
		private ISystemCommunicationsDaemonHandler _handler;
		private Socket _socket;
		private int _requestKey;
		
		protected CommunicationHandlerThread(ISystemCommunicationsDaemonHandler handler, Socket socket, int requestKey)
		{
			_handler = handler;
			_socket = socket;
			_requestKey = requestKey;			
		}
		
		public void run() 
		{
			_handler.handleRequest(_socket, _requestKey);
		}	
	}
	
	/**
	 * Singleton, so constructor is private.  Use the getInstance method 
	 * to retrieve and instance of this class.
	 */
	private SystemCommunicationsDaemon() {
	}

	/**
	 * Returns the singleton instance of this class.  If the an instance 
	 * has already been created then it is returned, otherwise a new 
	 * instance is created and returned.
	 */
	public static synchronized SystemCommunicationsDaemon getInstance()
	{
		if (inst == null) 
		{
			inst = new SystemCommunicationsDaemon();
		}
		return inst;
	}
	
	/*
	 * Stop the communications daemon
	 */
	private void halt() {
		halt = true;
		if (serverSocket != null) {
			try {
				serverSocket.close();

				// yantzi:2.1.2 (defect 49812) wait for RSE daemon thread to finish (wait at most 5 seconds)
				if (inst != null)
				{
					inst.join(5000);
				}
				
			} 
			catch (IOException e) 
			{
				SystemBasePlugin.logError("RSE Communications daemon: Unable to close socket", e);
			} 
			catch (InterruptedException e)
			{
				// some other thread interrupted this one (which should not happen
				SystemBasePlugin.logError("SystemCommunicationsDaemon.halt", e);
			}
		}
	}	
	
	/**
	 * @see Thread.run()
	 */
	public void run() {
		byte[] buffer;
		Socket socket = null;
		InputStream in = null;
		
		setName(THREAD_NAME);

		// Create server socket and start listening
		int port = SystemPlugin.getDefault().getPreferenceStore().getInt(ISystemPreferencesConstants.DAEMON_PORT);
		
		try {
			serverSocket = new ServerSocket(port);

			fireStateChangeEvent(SystemCommunicationsDaemonEvent.STARTED);			
			
			// Process incoming socket connections
			while (!halt) {
				socket = serverSocket.accept();
				
				// Pass incoming socket off to the appropriate handler,
				// the first four bytes (integer) determines the handler.
				in = socket.getInputStream();
				buffer = new byte[4];
				in.read(buffer);
				
				// RequestKey (integer) must be sent as Big Endian (high order bits first)
				// convert first four bytes to Java integer
				int requestKey = 0 | (buffer[0] << 24);

				requestKey = requestKey | ((buffer[1] << 24) >>> 8);
				requestKey = requestKey | ((buffer[2] << 24) >>> 16);
				requestKey = requestKey | ((buffer[3] << 24) >>> 24);
				

				// yantzi: 5.0.1: changed from asking hanlder for request key to requiring
				// handler to provide key when they are registered.
				ISystemCommunicationsDaemonHandler handler = (ISystemCommunicationsDaemonHandler) handlers.get(new Integer(requestKey));
				
				if (handler != null)
				{				
					new CommunicationHandlerThread(handler, socket, requestKey).start();
				}
				else
				{
					// handler not found
					SystemBasePlugin.logWarning("SystemCommunicationsDaemon: Handler not found for key " + requestKey);
					socket.close();
				}
			}
		} catch (IOException e) {
			if (!halt) 
			{
				SystemBasePlugin.logError("CommunicationsDaemon, IOException occured during communications daemon request",e);

				fireStateChangeEvent(SystemCommunicationsDaemonEvent.STOPPED_IN_ERROR);			
				
				// yantzi:2.1.2 (defect 51016) Suppress error messages when workbench is first starting up
				if (displayErrors)
				{
					SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_COMM_DAEMON_NOTSTARTED);
					msg.makeSubstitution(new Integer(port));
					Display.getDefault().asyncExec(new DisplaySystemMessageAction(msg));
				}
				
				// Reset label of action
				if (_startAction != null)
				{
					_startAction.setActionLabelToStart();
				}
			}
			else
			{
				fireStateChangeEvent(SystemCommunicationsDaemonEvent.STOPPED);
			}
		} finally {
			serverSocket = null;
			//dy need to get a new thread next time in order to re-run
			inst = null; 	
		}
	}

	/**
	 * Start the communications daemon if it is not already running
	 */	
	public synchronized void startDaemon() 
	{
		startDaemon(true);	
	}
	
	/**
	 * Start the communications daemon if it is not already running
	 * 
	 * @param displayErrors true if error messages should be displayed, false
	 * if they should be not be displayed.
	 */
	public synchronized void startDaemon(boolean displayErrors) {
		if (!isRunning())
		{
			SystemBasePlugin.logDebugMessage("CommunicationsDaemon.startDaemon()", "Starting iSeries Communications Daemon");

			//yantzi:2.1.2 added boolean to suppress error messages when workbench is first being started
			
			if (_startAction != null)
			{
				_startAction.setActionLabelToStop();
			}
				
			// Just in case someone cached an old copy of the daemon ...
			SystemCommunicationsDaemon daemon = getInstance();
			daemon.displayErrors = displayErrors;
			daemon.start();
		}
		else 
		{
			SystemBasePlugin.logDebugMessage("CommunicationsDaemon.startDaemon()",  "Daemon already started");
		}
	}

	/**
	 * Stops the communications daemon if it is running
	 */
	public synchronized void stopDaemon() {
		if (isRunning()) 
		{

			if (_startAction != null)
			{
				_startAction.setActionLabelToStart();
			}
			
			SystemBasePlugin.logDebugMessage("RSE CommunicationsDaemon.stopDaemon()", "Stopping iSeries Communications Daemon");
			getInstance().halt();
			
			// Need to get rid of the old Thread object and create a new
			// one next time (calling start on the same thread object twice
			// does not seem to work.  DY
			inst = null;
			
		}
		else 
		{
			SystemBasePlugin.logDebugMessage("RSE CommunicationsDaemon.stopDaemon()",  "Daemon already stopped");
		}
	}
	
	/**
	 * Returns the port the daemon is currently running on, or zero if the
	 * daemon is not running.
	 */
	public synchronized int getPort() {
		if (isRunning())
		{
			return serverSocket.getLocalPort();
		}
			
		return 0;
	}	
	
	/**
	 * Check to see if the iSeries communications daemon is running
	 * 
	 * @return true if the communications daemon is running, false if it 
	 * is not running
	 */
	public synchronized boolean isRunning() {
		return serverSocket != null;
	}

	//
	// Helper methods
	//
	
	/**
	 * Returns the user preference for auto-starting the communications daemon 
	 * when the Workbench starts.
	 */
	public static boolean isAutoStart() {
		return SystemPlugin.getDefault().getPreferenceStore().getBoolean(ISystemPreferencesConstants.DAEMON_AUTOSTART);
	}

	/**
	 * Add a new CommunicationsDaemonHandler
	 * 
	 * @see ISystemCommunicationsDaemonHandler
	 * 
	 * @param requestKey
	 * 
	 * The request key must be four byte integer key used by this handler.  This
	 * is the first thing that much be sent over the socket from the corresponding
	 * client / server when connecting to the RSE communications daemon.  The daemon
	 * when then compare this integer against all registered handlers and pass off the
	 * socket connection (via the handleRequest method) to the first match.  
	 * 
	 * Use the SystemCommunicationsDaemon.getInstance().addCommunicationsDaemonHandler(...)
	 * method to register with the daemon.  There is also a corresponding remove method.
	 * 
	 * Known handlers:
	 * 	RSEInteractiveJobHandler	0x67DF7A14
	 *  CODECommIntegrationHandler	0xA387E2CD
	 *  iSeries DebugEngine	
	 * 
	 * <b>The range of integers from 0xD0000000 to 0xFFFFFFFF is reserved for dyanmically
	 * generated kes. 
	 * 
	 * @return true if the handler was registered, false it the handler was not registered because
	 * the requestKey is already being used 
	 */
	public boolean addCommunicationsDaemonHandler(ISystemCommunicationsDaemonHandler handler, int requestKey) 
	{
		Integer key = new Integer(requestKey);
		
		if (!handlers.containsKey(key))
		{
			handlers.put(key, handler);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Remove a CommunicationsDaemonHandler
	 * 
	 * @see ISystemCommunicationsDaemonHandler
	 */
	public void removeCommunicationsDaemonHandler(int requestKey)
	{
		handlers.remove(new Integer(requestKey));
	}
	
	/**
	 * Sets the SystemStartCommunicationsDaemon action.  This is used to change the 
	 * label of the action if the daemon fails to start or fails during regular
	 * operation.
	 */
	public static void setAction(SystemStartCommunicationsDaemonAction action)
	{
		_startAction = action; 
	}

	/**
	 * getNextKey() can be called to generated a dynamic key used by any communications
	 * handler.  They generated key is guaranteed to be unique.  Callers still need to
	 * call the addCommunicationsDaemonHandler() method to register their handler.
	 */
	public static int getNextKey()
	{
		int key;
		
		synchronized (handlers)
		{
			key = _nextKey;
			
			if (_nextKey == 0xFFFFFFFF)
			{
				_nextKey = 0xD0000000;
			}
			else
			{
				_nextKey++;
			}
		}
				
		return key;
	}

	/**
	 * Add an ISystemCommunicationsDaemonListener listener to receive state change events from the SystemCommunicationsDaemon.
	 */
	public static void addDaemonListener(ISystemCommunicationsDaemonListener listener)
	{
		if (!_listeners.contains(listener))
		{
			_listeners.add(listener);
		}
	}

	/**
	 * Remove the ISystemCommunicationsDaemonListener listener.
	 */
	public static void removeDaemonListener(ISystemCommunicationsDaemonListener listener)
	{
		_listeners.remove(listener);
	}
	
	/**
	 * Fire a state change event for the daemon
	 */
	private static void fireStateChangeEvent(int newstate)
	{
		if (_listeners.size() > 0)
		{
			SystemCommunicationsDaemonEvent event = new SystemCommunicationsDaemonEvent(newstate);
			for (int i = 0; i < _listeners.size(); i++)
			{
				((ISystemCommunicationsDaemonListener) _listeners.get(i)).daemonStateChanged(event);
			}
		}
	}
	
}