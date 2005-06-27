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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * Represents a refactoring.
 * <p>
 * <bf>NOTE:<bf> This class/interface is part of an interim API that is still under development 
 * and expected to change significantly before reaching stability. It is being made available at 
 * this early stage to solicit feedback from pioneering adopters on the understanding that any 
 * code that uses this API will almost certainly be broken (repeatedly) as the API evolves.</p>
 */ 
public interface IRefactoring {
	
	/**
	 * Checks the proconditions of the receiving refactoring object.
	 * If the resulting <code>IStatus</code> has severity <code>IStatus.ERROR</code>,
	 * than <code>createChange</code> will not be called on the receiver.
	 * Must not return <code>null</code>.
	 * Implementors can assume the progress monitor to be not initialized.
	 * @see RefactoringStatus
	 * @see RefactoringStatus#OK
	 * @see RefactoringStatus#ERROR
	 */
	public RefactoringStatus checkPreconditions(IProgressMonitor pm) throws CModelException;
	
	/**
	 * Creates an <code>IChange</code> object that performs the actual refactoring.
	 * This is guaranteed not to be called before <code>checkPreconditions</code> or
	 * if <code>checkPreconditions</code> returns an <code>RefactoringStatus</code>
	 * object with severity <code>RefactoringStatus.ERROR</code>.
	 * Implementors can assume the progress monitor to be not initialized.
	 */
	public IChange createChange(IProgressMonitor pm) throws CModelException;


	/**
	 * Returns the name of this refactoring.
	 * 
	 * @return the refactoring's name. Mainly used in the UI.
	 */ 
	public String getName();
}
