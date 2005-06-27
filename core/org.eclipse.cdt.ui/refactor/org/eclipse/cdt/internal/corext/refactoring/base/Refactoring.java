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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.cdt.internal.corext.refactoring.UndoManager;

/**
 * Superclass for all refactorings.
 * <p>
 * <bf>NOTE:<bf> This class/interface is part of an interim API that is still under development 
 * and expected to change significantly before reaching stability. It is being made available at 
 * this early stage to solicit feedback from pioneering adopters on the understanding that any 
 * code that uses this API will almost certainly be broken (repeatedly) as the API evolves.</p>
 */
public abstract class Refactoring implements IRefactoring, IAdaptable {

	private static IUndoManager fgUndoManager= new UndoManager();
	
	public static IUndoManager getUndoManager() {
		return fgUndoManager;
	}
	
	
	/* non java-doc
	 * for debugging only
	 */
	public String toString(){
		return getName();
	}
	
	//---- Conditions ---------------------------
	
	/**
	 * Checks if this refactoring can be activated.
	 * Typically, this is used in the ui to check if a corresponding menu entry should be shown.
	 * Must not return <code>null</code>.
	 */ 
	public abstract RefactoringStatus checkActivation(IProgressMonitor pm) throws CModelException;
	
	/**
	 * After <code>checkActivation</code> has been performed and the user has provided all input
	 * necessary to perform the refactoring this method is called to check the remaining preconditions.
	 * Typically, this is used in the ui after the user has pressed 'next' on the last user input page.
	 * This method is always called after <code>checkActivation</code> and only if the status returned by
	 * <code>checkActivation</code> <code>isOK</code>.
	 * Must not return <code>null</code>.
	 * @see #checkActivation
	 * @see RefactoringStatus#isOK
	 */ 		
	public abstract RefactoringStatus checkInput(IProgressMonitor pm) throws CModelException;
	
	/**
	 * @see IRefactoring#checkPreconditions
	 * This implementation performs <code>checkActivation</code>
	 * and <code>checkInput</code> and merges the results.
	 * 
	 * @see #checkActivation
	 * @see #checkInput
	 * @see RefactoringStatus#merge
	 */
	public RefactoringStatus checkPreconditions(IProgressMonitor pm) throws CModelException{
		pm.beginTask("", 11); //$NON-NLS-1$
		RefactoringStatus result= new RefactoringStatus();
		result.merge(checkActivation(new SubProgressMonitor(pm, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK)));
		if (!result.hasFatalError())
			result.merge(checkInput(new SubProgressMonitor(pm, 10, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK)));	
		pm.done();
		return result;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this))
			return this;
		return null;
	}		
}
