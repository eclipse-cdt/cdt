package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * An exception to wrap a status. This is necessary to use the core's IRunnableWithProgress
 * support
 */

public class CUIException extends CoreException {
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	public CUIException(IStatus status) {
		super(status);
	}	
}
