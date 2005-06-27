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


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;

import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.cdt.internal.corext.refactoring.RefactoringCoreMessages;

/**
 * A change context is used to give an <code>IChange</code> object access to several workspace
 * resource independend from whether the change is executed head less or not. 
 * <p>
 * <bf>NOTE:<bf> This class/interface is part of an interim API that is still under development 
 * and expected to change significantly before reaching stability. It is being made available at 
 * this early stage to solicit feedback from pioneering adopters on the understanding that any 
 * code that uses this API will almost certainly be broken (repeatedly) as the API evolves.</p>
 */ 
public class ChangeContext {

	private IFile[] fUnsavedFiles;
	private List fHandledUnsavedFiles;
	private IChangeExceptionHandler fExceptionHandler;
	private IChange fFailedChange;
	private boolean fTryToUndo;
	private List fPerformedChanges= new ArrayList();


	/**
	 * Creates a new change context with the given exception handler.
	 * 
	 * @param handler object to handle exceptions caught during performing
	 *  a change. Must not be <code>null</code>
	 */
	public ChangeContext(IChangeExceptionHandler handler) {
		// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactor change
		this(handler, new IFile[] {});
	}
	
	/**
	 * Creates a new change context with the given exception handler.
	 * 
	 * @param handler object to handle exceptions caught during performing
	 *  a change. Must not be <code>null</code>
	 */
	public ChangeContext(IChangeExceptionHandler handler, IFile[] unsavedFiles) {
		// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactor change
		fExceptionHandler= handler;
		Assert.isNotNull(fExceptionHandler);
		fUnsavedFiles= unsavedFiles;
		Assert.isNotNull(fUnsavedFiles);
		fHandledUnsavedFiles= new ArrayList(fUnsavedFiles.length);
	}
	
	/**
	 * Returns the list of unsaved resources.
	 * 
	 * @return the list of unsaved resources
	 */
	public IFile[] getUnsavedFiles() {
		// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactor change
		return fUnsavedFiles;
	}
	
	public void checkUnsavedFile(RefactoringStatus status, IFile file) {
		if (fHandledUnsavedFiles.contains(file))
			return;
			
		// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactor change
		for (int i= 0; i < fUnsavedFiles.length; i++) {
			if (fUnsavedFiles[i].equals(file)) {
				status.addFatalError(RefactoringCoreMessages.getFormattedString("Change.is_unsaved", file.getFullPath().toString())); //$NON-NLS-1$
				fHandledUnsavedFiles.add(file);
				return;
			}
		}
	}
		
	/**
	 * Returns the exception handler used to report exception back to the client.
	 * 
	 * @return the exception handler to report exceptions
	 */
	public IChangeExceptionHandler getExceptionHandler() {
		return fExceptionHandler;
	}
	
	/**
	 * Sets the change that caused an exception to the given value.
	 * 
	 * @param change the change that caused an exception
	 */
	public void setFailedChange(IChange change) {
		fFailedChange= change;
	}
	
	/**
	 * Returns the change that caused an exception.
	 * 
	 * @return the change that caused an exception
	 */
	public IChange getFailedChange() {
		return fFailedChange;
	}
	
	/**
	 * An unexpected error has occurred during execution of a change. Communicate
	 * to the outer operation that the successfully performed changes collected by 
	 * this change context are supposed to be undone.
	 * 
	 * @see ChangeContext#addPerformedChange(IChange)
	 */
	public void setTryToUndo() {
		fTryToUndo= true;
	}
	
	/**
	 * Returns <code>true</code> if an exception has been caught during execution of
	 * the change and the outer operation should try to undo all successfully performed
	 * changes. Otherwise <code>false</code> is returned.
	 * 
	 * @return if the outer operation should try to undo all successfully performed
	 *  changes
	 */
	public boolean getTryToUndo() {
		return fTryToUndo;
	}
	
	/**
	 * Adds the given change to the list of successfully performed changes.
	 * 
	 * @param the change executed successfully.
	 */
	public void addPerformedChange(IChange change) {
//		if (change instanceof ICompositeChange)
//			return;
			
		fPerformedChanges.add(change);
	}
	
	/**
	 * Returns all changes that have been performed successfully
	 * 
	 * @return the successfully performed changes.
	 */
	public IChange[] getPerformedChanges() {
		return (IChange[])fPerformedChanges.toArray(new IChange[fPerformedChanges.size()]);
	}
	
	/**
	 * Removes all performed changes from this context.
	 */
	public void clearPerformedChanges() {
		fPerformedChanges= new ArrayList(1);
	}
}
