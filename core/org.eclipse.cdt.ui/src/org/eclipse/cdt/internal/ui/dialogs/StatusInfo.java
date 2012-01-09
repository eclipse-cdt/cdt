/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * A settable IStatus
 * Can be an error, warning, info or ok. For error, info and warning states,
 * a message describes the problem
 */
public class StatusInfo implements IStatus {
	
	public static final IStatus OK_STATUS= new StatusInfo();

	private String fStatusMessage;
	private int fSeverity;

	/**
	 * Creates a status set to OK (no message)
	 */
	public StatusInfo() {
		this(OK, null);
	}

	/**
	 * Creates a status.
	 * @param severity The status severity: ERROR, WARNING, INFO and OK.
	 * @param message The message of the status. Applies only for ERROR,
	 * WARNING and INFO.
	 */	
	public StatusInfo(int severity, String message) {
		fStatusMessage= message;
		fSeverity= severity;
	}		

	/**
	 * @see IStatus#getChildren()
	 */
	@Override
	public IStatus[] getChildren() {
		return new IStatus[0];
	}
	/**
	 * @see IStatus#getCode()
	 */
	@Override
	public int getCode() {
		return fSeverity;
	}
	/**
	 * @see IStatus#getException()
	 */
	@Override
	public Throwable getException() {
		return null;
	}
	/**
	 * @see IStatus#getMessage
	 */
	@Override
	public String getMessage() {
		return fStatusMessage;
	}
	/**
	 * @see IStatus#getPlugin()
	 */
	@Override
	public String getPlugin() {
		return CUIPlugin.PLUGIN_ID;
	}
	/**
	 * @see IStatus#getSeverity()
	 */
	@Override
	public int getSeverity() {
		return fSeverity;
	}
	public boolean isError() {
		return fSeverity == IStatus.ERROR;
	}
	public boolean isInfo() {
		return fSeverity == IStatus.INFO;
	}
	/**
	 * @see IStatus#isMultiStatus()
	 */
	@Override
	public boolean isMultiStatus() {
		return false;
	}
	@Override
	public boolean isOK() {
		return fSeverity == IStatus.OK;
	}
	public boolean isWarning() {
		return fSeverity == IStatus.WARNING;
	}
	/**
	 * @see IStatus#matches(int)
	 */
	@Override
	public boolean matches(int severityMask) {
		return (fSeverity & severityMask) != 0;
	}
	public void setError(String errorMessage) {
		fStatusMessage= errorMessage;
		fSeverity= IStatus.ERROR;
	}
	public void setInfo(String infoMessage) {
		fStatusMessage= infoMessage;
		fSeverity= IStatus.INFO;
	}
	public void setOK() {
		fStatusMessage= null;
		fSeverity= IStatus.OK;
	}
	public void setWarning(String warningMessage) {
		fStatusMessage= warningMessage;
		fSeverity= IStatus.WARNING;
	}

	/**
	 * Returns a string representation of the status, suitable 
	 * for debugging purposes only.
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("StatusInfo "); //$NON-NLS-1$
		if (fSeverity == OK) {
			buf.append("OK"); //$NON-NLS-1$
		} else if (fSeverity == ERROR) {
			buf.append("ERROR"); //$NON-NLS-1$
		} else if (fSeverity == WARNING) {
			buf.append("WARNING"); //$NON-NLS-1$
		} else if (fSeverity == INFO) {
			buf.append("INFO"); //$NON-NLS-1$
		} else {
			buf.append("severity="); //$NON-NLS-1$
			buf.append(fSeverity);
		}
		buf.append(": "); //$NON-NLS-1$
		buf.append(fStatusMessage);
		return buf.toString();
	}
}
