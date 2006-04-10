/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;


/**
 * A SystemMessageStatus object encapsulates a SystemMessage or a
 * SystemMessageException as a Status object. Could be used when creating a
 * CoreException from a SystemMessageException.
 */
public class SystemMessageStatus implements IStatus {
	private SystemMessage message;
	private SystemMessageException exception;
	public SystemMessageStatus(SystemMessage message) {
		this.message = message;
	}

	public SystemMessageStatus(SystemMessageException exception) {
		this.message = exception.getSystemMessage();
		this.exception = exception;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IStatus#isOK()
	 */
	public boolean isOK() {
		int severity = getSeverity();
		return severity <= IStatus.OK;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IStatus#getPlugin()
	 */
	public String getPlugin() {
		String id = SystemBasePlugin.getBaseDefault().getBundle().getSymbolicName();
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IStatus#getChildren()
	 */
	public IStatus[] getChildren() {
		return new IStatus[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IStatus#getCode()
	 */
	public int getCode() {
		String codeString = message.getMessageNumber();
		int code = 0;
		try {
			code = Integer.parseInt(codeString);
		} catch (NumberFormatException e) {
		}
		return code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IStatus#getException()
	 */
	public Throwable getException() {
		return exception;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IStatus#getMessage()
	 */
	public String getMessage() {
		return message.getLevelOneText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IStatus#getSeverity()
	 */
	public int getSeverity() {
		char ind = message.getIndicator();
		switch (ind) {
			case SystemMessage.COMPLETION:
				return IStatus.OK;
			case SystemMessage.INFORMATION:
				return IStatus.INFO;
			case SystemMessage.INQUIRY:
				return IStatus.INFO;
			case SystemMessage.WARNING:
				return IStatus.WARNING;
			case SystemMessage.UNEXPECTED:
				return IStatus.WARNING;
			case SystemMessage.ERROR:
				return IStatus.ERROR;
			default:
				return IStatus.OK;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IStatus#isMultiStatus()
	 */
	public boolean isMultiStatus() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IStatus#matches(int)
	 */
	public boolean matches(int severityMask) {
		int severity = getSeverity();
		int matching = severity & severityMask;
		return matching > 0;
	}
	
	/**
	 * @return the SystemMessage encapsulated by this status.
	 */
	public SystemMessage getSystemMessage() {
		return message;
	}
	

}