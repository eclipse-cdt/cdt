/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.base;


import java.io.PrintStream;
import java.io.PrintWriter;

import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.cdt.internal.corext.refactoring.RefactoringCoreMessages;


/**
 * This exception is thrown if an unexpected errors occurs during execution
 * of a change object.
 */
public class ChangeAbortException extends RuntimeException {

	private Throwable fThrowable;


	/**
	 * Creates a new <code>ChangeAbortException</code> for the given throwable.
	 * 
	 * @param t the unexpected throwable caught while performing the change
	 * @param context the change context used to process the change
	 */
	public ChangeAbortException(Throwable t) {
		fThrowable= t;
		Assert.isNotNull(fThrowable);
	}
	
	/**
	 * Returns the <code>Throwable</code> that has caused the change to fail.
	 * 
	 * @return the throwable that has caused the change to fail
	 */
	public Throwable getThrowable() {
		return fThrowable;
	}
	
	/**
	 * Prints a stack trace out for the exception, and
	 * any nested exception that it may have embedded in
	 * its Status object.
	 */
	public void printStackTrace(PrintStream output) {
		synchronized (output) {
			output.print("ChangeAbortException: "); //$NON-NLS-1$
			super.printStackTrace(output);
			
			if (fThrowable != null) {
				output.print(RefactoringCoreMessages.getFormattedString("ChangeAbortException.wrapped", "ChangeAbortException: ")); //$NON-NLS-2$ //$NON-NLS-1$
				fThrowable.printStackTrace(output);
			}
		}
	}
	/**
	 * Prints a stack trace out for the exception, and
	 * any nested exception that it may have embedded in
	 * its Status object.
	 */
	public void printStackTrace(PrintWriter output) {
		synchronized (output) {
			output.print("ChangeAbortException: "); //$NON-NLS-1$
			super.printStackTrace(output);
			
			if (fThrowable != null) {
				output.print(RefactoringCoreMessages.getFormattedString("ChangeAbortException.wrapped", "ChangeAbortException: ")); //$NON-NLS-2$ //$NON-NLS-1$
				fThrowable.printStackTrace(output);
			}
		}
	}		
}
