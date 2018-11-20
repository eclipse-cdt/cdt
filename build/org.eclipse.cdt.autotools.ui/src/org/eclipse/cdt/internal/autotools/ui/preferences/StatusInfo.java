/*******************************************************************************
 * Copyright (c) 2002, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.preferences;

import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

/**
 * A settable IStatus.
 * Can be an error, warning, info or ok. For error, info and warning states,
 * a message describes the problem.
 */
public class StatusInfo implements IStatus {

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

	/**
	 *  Returns if the status' severity is OK.
	 */
	@Override
	public boolean isOK() {
		return fSeverity == IStatus.OK;
	}

	/**
	 *  Returns if the status' severity is WARNING.
	 */
	public boolean isWarning() {
		return fSeverity == IStatus.WARNING;
	}

	/**
	 *  Returns if the status' severity is INFO.
	 */
	public boolean isInfo() {
		return fSeverity == IStatus.INFO;
	}

	/**
	 *  Returns if the status' severity is ERROR.
	 */
	public boolean isError() {
		return fSeverity == IStatus.ERROR;
	}

	@Override
	public String getMessage() {
		return fStatusMessage;
	}

	/**
	 * Sets the status to ERROR.
	 * @param errorMessage The error message (can be empty, but not null)
	 */
	public void setError(String errorMessage) {
		Assert.isNotNull(errorMessage);
		fStatusMessage = errorMessage;
		fSeverity = IStatus.ERROR;
	}

	/**
	 * Sets the status to WARNING.
	 * @param warningMessage The warning message (can be empty, but not null)
	 */
	public void setWarning(String warningMessage) {
		Assert.isNotNull(warningMessage);
		fStatusMessage = warningMessage;
		fSeverity = IStatus.WARNING;
	}

	/**
	 * Sets the status to INFO.
	 * @param infoMessage The info message (can be empty, but not null)
	 */
	public void setInfo(String infoMessage) {
		Assert.isNotNull(infoMessage);
		fStatusMessage = infoMessage;
		fSeverity = IStatus.INFO;
	}

	/**
	 * Sets the status to OK.
	 */
	public void setOK() {
		fStatusMessage = null;
		fSeverity = IStatus.OK;
	}

	/*
	 * @see IStatus#matches(int)
	 */
	@Override
	public boolean matches(int severityMask) {
		return (fSeverity & severityMask) != 0;
	}

	/**
	 * Returns always <code>false</code>.
	 * @see IStatus#isMultiStatus()
	 */
	@Override
	public boolean isMultiStatus() {
		return false;
	}

	/*
	 * @see IStatus#getSeverity()
	 */
	@Override
	public int getSeverity() {
		return fSeverity;
	}

	/*
	 * @see IStatus#getPlugin()
	 */
	@Override
	public String getPlugin() {
		return AutotoolsUIPlugin.getPluginId();
	}

	/**
	 * Returns always <code>null</code>.
	 * @see IStatus#getException()
	 */
	@Override
	public Throwable getException() {
		return null;
	}

	/**
	 * Returns always the error severity.
	 * @see IStatus#getCode()
	 */
	@Override
	public int getCode() {
		return fSeverity;
	}

	/**
	 * Returns always <code>null</code>.
	 * @see IStatus#getChildren()
	 */
	@Override
	public IStatus[] getChildren() {
		return new IStatus[0];
	}

}
