/*******************************************************************************
 * Copyright (c) 2012 Sage Electronic Engineering, LLC. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jason Litton (Sage Electronic Engineering, LLC) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.dsf.gdb.IGdbErrorListener;
import org.eclipse.core.runtime.ListenerList;

/**
 * The GDBErrorHandler is a central clearing house for errors that come off the 
 * GDB command line. We can ask the error handler to listen for any possible error
 * (passed as a regex) and can notify anything that has registered for events.
 *
 */

public class GDBErrorHandler {
	
	GDBErrorHandler instance = null;
	
	public static final int SEVERITY_IGNORE = 0;
	public static final int SEVERITY_LOG = 1;
	public static final int SEVERITY_NOTIFY_WITHOUT_REPETITION= 2;
	public static final int SEVERITY_NOTIFY_ALWAYS = 3;
	public static final int SEVERITY_RESTART = 4;
	
	//number of leading characters to keep and match from the last message displayed
	private static final int LAST_MESSAGE_LENGTH = 15;
	
	private static ArrayList<GDBError> fGdbErrorsToHandle = new ArrayList<GDBError>();
	private static ArrayList<Pattern> errorsToIgnore = new ArrayList<Pattern>();
	String lastMessage = Messages.GDBErrorHandler_Default_Last_Message; //start with general message that nothing will match
	private static final String eol = System.getProperty("line.separator"); //$NON-NLS-1$
	
	private static ListenerList fListeners = new ListenerList();

	
	//This class is a singleton so that the list of messages to listen for is consistent.
	private GDBErrorHandler() {
		//default errors can be added here by calling listenForError(regex)
	}
	
	public GDBErrorHandler getInstance() {
		if (instance == null) {
			instance = new GDBErrorHandler();
		}
		return instance;
	}
	
	
	public void handleGdbError(String msg) {
		if (msg.contains(Messages.GDBErrorHandler_GDB_Error_Tag)) {
			msg = msg.substring(msg.indexOf('=' + 1));
		}
		//only analyze the error if we have not explicitly been told to ignore it
		boolean checkError = !msg.contains(lastMessage);
		for (int i = 0; i < errorsToIgnore.size() && checkError; i++) {
			Pattern pattern = errorsToIgnore.get(i);
			Matcher matcher = pattern.matcher(msg);
			checkError = !matcher.find();
		}
		
		//if we're not ignoring the error, go ahead and check whether to warn the user
		//or log the error
		if(checkError) {
			for (int i = 0; i < fGdbErrorsToHandle.size(); i++) {
				GDBError thisError = fGdbErrorsToHandle.get(i);
				if(thisError.matches(msg)) {
					String errorMessage = String.format("%s%s %s", Messages.GDBErrorHandler_GDB_Error_Prepend, eol, msg); //$NON-NLS-1$
					switch (thisError.getSeverity()) {
					case SEVERITY_RESTART:
						errorMessage = String.format("%s%s%s", errorMessage, eol, Messages.GDBErrorHandler_GDB_Restart); //$NON-NLS-1$
						//$FALL-THROUGH$
					case SEVERITY_NOTIFY_ALWAYS:
						notifyListeners(errorMessage);
						break;
					case SEVERITY_NOTIFY_WITHOUT_REPETITION:
						if(!errorMessage.contains(lastMessage)) {
							notifyListeners(errorMessage);
						}
						break;
					case SEVERITY_LOG:
							GdbDebugOptions.trace(String.format("%s %s %s", GdbPlugin.getDebugTime(), errorMessage, eol)); //$NON-NLS-1$
						break;
					}
					setLastMessage(errorMessage);
				}
			}
		}	
	}
	
	public static void listenForError(String regex, int severity) {
		fGdbErrorsToHandle.add(new GDBError(regex, severity));
	}
	
	public static void ignoreError(String regex) {
		errorsToIgnore.add(Pattern.compile(regex));
	}
	
	public static void addListener (IGdbErrorListener listener) {
		fListeners.add(listener);
	}
	
	public static void removeListener(IGdbErrorListener listener) {
		fListeners.remove(listener);
	}
	
	private void notifyListeners(final String errorMsg) {
		Object[] listeners = fListeners.getListeners();
		for (Object listener : listeners) {
			((IGdbErrorListener) listener).gdbErrorNotification(errorMsg);
		}
	}
	
	//Store the first 15 characters of the last message
	//so we can avoid repeats when multiple similar errors
	//occur simultaneously (ie. Could not disassemble memory
	//at address...)
	private void setLastMessage(String msg) {
		if(msg.length() >= LAST_MESSAGE_LENGTH) {
			lastMessage = msg.substring(0, LAST_MESSAGE_LENGTH);
		} else {
			lastMessage = msg;
		}
	}
	

}
