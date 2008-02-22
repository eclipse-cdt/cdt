/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 ********************************************************************************/
package org.eclipse.rse.services.clientserver.messages;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;

public class SimpleSystemMessage extends SystemMessage {

	/**
	 * alternative to message number for ids?
	 */
	private String _pluginId;

	private int _severity;
	
	/**
	 * Constructor for messages that use explicit strings and severities rather than
	 * parsing a message file.  This is part of the work to migrate away from the message
	 * file stuff.
	 * 
	 * @param pluginId the id of the originating plugin
	 * @param severity using IStatus severities
	 * @param msg the message text
	 */
	public SimpleSystemMessage(String pluginId, int severity, String msg) {
		this(pluginId, severity, msg, (String)null);
	}
	
	/**
	 * Constructor for messages that use explicit strings and severities rather than
	 * parsing a message file.  This is part of the work to migrate away from the message
	 * file stuff.
	 * 
	 * @param pluginId the id of the originating plugin
	 * @param severity using IStatus severities
	 * @param msg the message text
	 * @param msgDetails the message details
	 */
	public SimpleSystemMessage(String pluginId, int severity, String msg, String msgDetails) {
		super("RSE", "G", "-", severityToIndicator(severity), msg, msgDetails);  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
	
		_pluginId = pluginId;
		_severity = severity;
	}
		
	/**
	 * Constructor for messages that use explicit strings and severities rather than
	 * parsing a message file.  This is part of the work to migrate away from the message
	 * file stuff.
	 * 
	 * @param pluginId the id of the originating plugin
	 * @param severity using IStatus severities
	 * @param msg the message text
	 * @param e an exception to convert into details
	 */
	public SimpleSystemMessage(String pluginId, int severity, String msg, Throwable e) {
		super("RSE", "G", "-", severityToIndicator(severity), msg, throwableToDetails(e)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		_pluginId = pluginId;
		_severity = severity;
	}
	
	private static String throwableToDetails(Throwable e){	
		// transform exception stack into a string
		StringWriter excWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(excWriter));
		String dmsg = e.toString();
		if ((dmsg == null) || (e instanceof ClassCastException)) dmsg = e.getClass().getName();
		String msgDetails = dmsg + "\n" + excWriter.toString(); //$NON-NLS-1$  
		return msgDetails;
	}
	
	private static char severityToIndicator(int severity){
		char ind = COMPLETION;
		if ((severity & IStatus.CANCEL) != 0){
			ind = UNEXPECTED;
		}
		else if ((severity & IStatus.ERROR) != 0){
			ind = ERROR;
		}
		else if ((severity & IStatus.INFO) != 0){
			ind = INFORMATION;
		}
		else if ((severity & IStatus.OK) != 0){
			ind = COMPLETION;
		}
		else if ((severity & IStatus.WARNING) != 0){
			ind = WARNING;
		}
		return ind;
	}

	public String getFullMessageID() {
		return _pluginId + ":" + getIndicator(); //$NON-NLS-1$
	}
	
}
