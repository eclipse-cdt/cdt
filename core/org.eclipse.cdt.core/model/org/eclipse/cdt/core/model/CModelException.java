/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.cdt.internal.core.model.CModelStatus;
import org.eclipse.core.runtime.CoreException;

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
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3546638828312998451L;

	/**
	 * Creates a C model exception that wrappers the given <code>Throwable</code>.
	 * The exception contains a C-specific status object with severity
	 * <code>IStatus.ERROR</code> and the given status code.
	 *
	 * @param e the {@link Throwable}
	 * @param code one of the C-specific status codes declared in
	 *   <code>ICModelStatusConstants</code>
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
	 */
	public CModelException(CoreException exception) {
		this(new CModelStatus(exception));
	}

	/**
	 * Creates a C model exception for the given C-specific status object.
	 *
	 * @param status the C-specific status object
	 */
	public CModelException(ICModelStatus status) {
		super(status);
	}

	/**
	 * Returns the underlying <code>Throwable</code> that caused the failure.
	 *
	 * @return the wrapped <code>Throwable</code>, or <code>null</code> if the
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
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("C Model Exception: "); //$NON-NLS-1$
		if (getException() != null) {
			if (getException() instanceof CoreException) {
				CoreException c = (CoreException) getException();
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
