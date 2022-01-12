/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IStatus;

/**
 * A settable IStatus
 * Can be an error, warning, info or ok. For error, info and warning states,
 * a message describes the problem
 */
public class StatusInfo implements IStatus {

	public static final IStatus OK_STATUS = new StatusInfo();

	private String fStatusMessage;
	private int fSeverity;

	/**
	 * Creates a status set to OK (no message)
	 */
	public StatusInfo() {
		this(OK, null);
	}

	/**
	 * Creates a status .
	 * @param severity The status severity: ERROR, WARNING, INFO and OK.
	 * @param message The message of the status. Applies only for ERROR,
	 * WARNING and INFO.
	 */
	public StatusInfo(int severity, String message) {
		fStatusMessage = message;
		fSeverity = severity;
	}

	@Override
	public IStatus[] getChildren() {
		return new IStatus[0];
	}

	@Override
	public int getCode() {
		return fSeverity;
	}

	@Override
	public Throwable getException() {
		return null;
	}

	@Override
	public String getMessage() {
		return fStatusMessage;
	}

	@Override
	public String getPlugin() {
		return CUIPlugin.PLUGIN_ID;
	}

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

	@Override
	public boolean matches(int severityMask) {
		return (fSeverity & severityMask) != 0;
	}

	public void setError(String errorMessage) {
		fStatusMessage = errorMessage;
		fSeverity = IStatus.ERROR;
	}

	public void setInfo(String infoMessage) {
		fStatusMessage = infoMessage;
		fSeverity = IStatus.INFO;
	}

	public void setOK() {
		fStatusMessage = null;
		fSeverity = IStatus.OK;
	}

	public void setWarning(String warningMessage) {
		fStatusMessage = warningMessage;
		fSeverity = IStatus.WARNING;
	}

	/**
	 * Returns a string representation of the status, suitable
	 * for debugging purposes only.
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
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
