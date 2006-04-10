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

package org.eclipse.rse.ui.messages;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;


/**
 * This class adapts the eclipse IStatusLineManager to an ISystemMessageLine.
 * 
 * @author yantzi
 */
public class StatusLineManagerAdapter implements ISystemMessageLine {

	private IStatusLineManager statusLine;
	private String message, errorMessage;
	private SystemMessage sysErrorMessage;

	/**
	 * Constructor
	 * 
	 * @param statusLineManager the status line manager to adapt to an ISystemMessageLine
	 */
	public StatusLineManagerAdapter(IStatusLineManager statusLineManager)
	{
		this.statusLine = statusLineManager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.messages.ISystemMessageLine#clearErrorMessage()
	 */
	public void clearErrorMessage() {
		errorMessage = null;
		sysErrorMessage = null;
		if (statusLine != null)
			statusLine.setErrorMessage(errorMessage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.messages.ISystemMessageLine#clearMessage()
	 */
	public void clearMessage() {
		message = null;
		if (statusLine != null)
			statusLine.setMessage(message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.messages.ISystemMessageLine#getErrorMessage()
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.messages.ISystemMessageLine#getSystemErrorMessage()
	 */
	public SystemMessage getSystemErrorMessage() {
		return sysErrorMessage;
			}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.messages.ISystemMessageLine#getMessage()
	 */
	public String getMessage() {
		return message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.messages.ISystemMessageLine#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String message) {
		this.errorMessage = message;
		if (statusLine != null)
			statusLine.setErrorMessage(message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.messages.ISystemMessageLine#setErrorMessage(org.eclipse.rse.core.ui.messages.SystemMessage)
	 */
	public void setErrorMessage(SystemMessage message) {
		sysErrorMessage = message;
		setErrorMessage(message.getLevelOneText());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.messages.ISystemMessageLine#setErrorMessage(java.lang.Throwable)
	 */
	public void setErrorMessage(Throwable exc) {
		setErrorMessage(exc.getMessage());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.messages.ISystemMessageLine#setMessage(java.lang.String)
	 */
	public void setMessage(String message) {
		this.message = message;
		if (statusLine != null)
			statusLine.setMessage(message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.messages.ISystemMessageLine#setMessage(org.eclipse.rse.core.ui.messages.SystemMessage)
	 */
	public void setMessage(SystemMessage message) {
		setMessage(message.getLevelOneText());
	}
}