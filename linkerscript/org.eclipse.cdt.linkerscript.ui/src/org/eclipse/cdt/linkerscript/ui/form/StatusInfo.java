package org.eclipse.cdt.linkerscript.ui.form;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

/**
 * A settable IStatus.
 * Can be an error, warning, info or ok. For error, info and warning states,
 * a message describes the problem.
 */
// copied from org.eclipse.jdt.internal.ui.dialogs.StatusInfo
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
	 * Creates a status .
	 * @param severity The status severity: ERROR, WARNING, INFO and OK.
	 * @param message The message of the status. Applies only for ERROR,
	 * WARNING and INFO.
	 */
	public StatusInfo(int severity, String message) {
		fStatusMessage= message;
		fSeverity= severity;
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

	/**
	 * @see IStatus#getMessage
	 */
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
		fStatusMessage= errorMessage;
		fSeverity= IStatus.ERROR;
	}

	/**
	 * Sets the status to WARNING.
	 * @param warningMessage The warning message (can be empty, but not null)
	 */
	public void setWarning(String warningMessage) {
		Assert.isNotNull(warningMessage);
		fStatusMessage= warningMessage;
		fSeverity= IStatus.WARNING;
	}

	/**
	 * Sets the status to INFO.
	 * @param infoMessage The info message (can be empty, but not null)
	 */
	public void setInfo(String infoMessage) {
		Assert.isNotNull(infoMessage);
		fStatusMessage= infoMessage;
		fSeverity= IStatus.INFO;
	}

	/**
	 * Sets the status to OK.
	 */
	public void setOK() {
		fStatusMessage= null;
		fSeverity= IStatus.OK;
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
		// TODO move to activator?
		return "org.eclipse.cdt.linkerscript.ui";
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
	 * Returns always an empty array.
	 * @see IStatus#getChildren()
	 */
	@Override
	public IStatus[] getChildren() {
		return new IStatus[0];
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
