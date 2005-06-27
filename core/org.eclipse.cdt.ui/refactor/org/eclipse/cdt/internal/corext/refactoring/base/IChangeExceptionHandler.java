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



/**
 * An <code>ChangeExceptionHandler</code> is informed about any exception that occurrs during
 * performing a change. Implementors of this interface can control if the change is supposed to
 * be continued or if it is to be aborted.
 * <p>
 * <bf>NOTE:<bf> This class/interface is part of an interim API that is still under development 
 * and expected to change significantly before reaching stability. It is being made available at 
 * this early stage to solicit feedback from pioneering adopters on the understanding that any 
 * code that uses this API will almost certainly be broken (repeatedly) as the API evolves.</p>
 */ 
public interface IChangeExceptionHandler {


	/**
	 * Handles the given exception.
	 * 
	 * @param context the change context passed to <code>IChange.perform</code>
	 * @param change the change that caused the exception
	 * @param exception the exception cought during executing the change
	 * @exception ChangeAbortException if the change is to be aborted
	 */
	public void handle(ChangeContext context, IChange change, Exception exception) throws ChangeAbortException;	
}
