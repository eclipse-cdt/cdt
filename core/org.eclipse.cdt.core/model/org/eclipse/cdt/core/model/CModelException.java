/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

 
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.internal.core.model.CModelStatus;

/**
 * A checked exception representing a failure in the C model.
 * C model exceptions contain a C-specific status object describing the
 * cause of the exception.
 *
 * @see ICModelStatus
 * @see ICModelStatusConstants
 */
public class CModelException extends CoreException {
	/**
	 * Creates a C model exception that wrappers the given <code>Throwable</code>.
	 * The exception contains a C-specific status object with severity
	 * <code>IStatus.ERROR</code> and the given status code.
	 *
	 * @param exception the <code>Throwable</code>
	 * @param code one of the C-specific status codes declared in
	 *   <code>ICModelStatusConstants</code>
	 * @return the new C model exception
	 * @see ICModelStatusConstants
	 * @see org.eclipse.core.runtime.IStatus#ERROR
	 */
	public CModelException(Throwable e, int code) {
		this(new CModelStatus(code, e)); 
	}

	/**
	 * Creates a C model exception for the given <code>CoreException</code>.
	 * Equivalent to 
	 * <code>CModelException(exception,ICModelStatusConstants.CORE_EXCEPTION</code>.
	 *
	 * @param exception the <code>CoreException</code>
	 * @return the new C model exception
	 */
	public CModelException(CoreException exception) {
		this(new CModelStatus(exception));
	}

	/**
	 * Creates a C model exception for the given C-specific status object.
	 *
	 * @param status the C-specific status object
	 * @return the new C model exception
	 */
	public CModelException(ICModelStatus status) {
		super(status);
	}

	/**
	 * Returns the underlying <code>Throwable</code> that caused the failure.
	 *
	 * @return the wrappered <code>Throwable</code>, or <code>null</code> if the
	 *   direct case of the failure was at the C model layer
	 */
	public Throwable getException() {
		return getStatus().getException();
	}

	/**
	 * Returns the C model status object for this exception.
	 * Equivalent to <code>(ICModelStatus) getStatus()</code>.
	 *
	 * @return a status object
	 */
	public ICModelStatus getCModelStatus() {
		return (ICModelStatus) getStatus();
	}

	/**
	 * Returns whether this exception indicates that a C model element does not
	 * exist. Such exceptions have a status with a code of
	 * <code>ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST</code>.
	 * This is a convenience method.
	 *
	 * @return <code>true</code> if this exception indicates that a C model
	 *   element does not exist
	 * @see ICModelStatus#doesNotExist
	 * @see ICModelStatusConstants#ELEMENT_DOES_NOT_EXIST
	 */
	public boolean doesNotExist() {
		ICModelStatus cModelStatus = getCModelStatus();
		return cModelStatus != null && cModelStatus.doesNotExist();
	}

	/**
	 * Returns a printable representation of this exception suitable for debugging
	 * purposes only.
	 */
	public String toString() {
		StringBuffer buffer= new StringBuffer();
		buffer.append("C Model Exception: "); //$NON-NLS-1$
		if (getException() != null) {
			if (getException() instanceof CoreException) {
				CoreException c= (CoreException)getException();
				buffer.append("Core Exception [code "); //$NON-NLS-1$
				buffer.append(c.getStatus().getCode());
				buffer.append("] "); //$NON-NLS-1$
				buffer.append(c.getStatus().getMessage());
			} else {
				buffer.append(getException().toString());
			}
		} else {
			buffer.append(getStatus().toString());
		}
		return buffer.toString();
	}
}
