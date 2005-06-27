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


import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.model.CModelException;


/**
 * Represents a generic change to the workbench. An <code>IChange</code> object is typically 
 * created by calling <code>IRefactoring::createChange</code>.
 * <p>
 * Calling <code>perform</code> performs the actual change to the workbench. Clients then call 
 * <code>getUndoChange</code>. It is the implementors responsbility to make sure that the <code>
 * IChange</code> object returned by <code>getUndoChange</code> really represents a reverse change.
 * 
 * Clients can implement this interface.
 * <p>
 * <bf>NOTE:<bf> This class/interface is part of an interim API that is still under development 
 * and expected to change significantly before reaching stability. It is being made available at 
 * this early stage to solicit feedback from pioneering adopters on the understanding that any 
 * code that uses this API will almost certainly be broken (repeatedly) as the API evolves.</p>
 */
public interface IChange {

	public static final int REFACTORING_CHANGE_ABORTED= 900;

	/**
	 * The client is about to calling <code>perform</code> on this change. The client of this
	 * change must ensure that the method is called outside a runnable that modifies the 
	 * workbench. Typically <code>aboutToPerform</code>, <code>perform</code> and <code>
	 * performed</code> are used as follows:
	 * <pre>
	 *	try {
	 *		change.aboutToPerform(context);
	 *		workbench.run(new IWorkspaceRunnable() {
	 *			public void run(IProgressMonitor pm) throws CoreException {
	 *				change.perform(context, pm);
	 *			}
	 *		}, progressMonitor);
	 *	} finally {
	 *		change.performed();
	 *	}
	 * </pre>
	 * @see #performed()
	 */
	public RefactoringStatus aboutToPerform(ChangeContext context, IProgressMonitor pm);
	 
	/**
	 * Performs this change. It is <em>critical</em> that you call <code>perform</code> 
	 * <em>before</em> you call <code>getUndoChange</code>. In general, <code>IChange</code>
	 * objects do not know what the reverse will be before they are performed.
	 */
	 public void perform(ChangeContext context, IProgressMonitor pm) throws CModelException, ChangeAbortException;
	
	/**
	 * The change has been performed. Clients must ensure that this method is called after all
	 * resource deltas emitted by calling <code>perform</code> are processed. This method must
	 * be called even if the perform has thrown a runtime exception.
	 * @see #aboutToPerform(ChangeContext, IProgressMonitor)
	 */
	public void performed(); 
	 
	/**
	 * Returns the change that, when performed, will undo the receiver. <code>IChange</code> 
	 * objects can assume that <code>perform</code> has been called on them before. It is the 
	 * caller's responsiblity to make sure that this is true. As mentioned in the class comment, 
	 * it is the responsiblity of the implementors to make sure that this method does create a 
	 * reverse change.
	 * 
	 * @return the reverse change of this change
	 */
	public IChange getUndoChange();
	
	/**
	 * Sets the activation status for this <code>IChange</code>. When a change is not active, 
	 * then executing it is expected to do nothing.
	 *
	 * @param active the activation status for this change.
	 */
	public void setActive(boolean active);
	
	/**
	 * Returns the activation status of this <code>IChange</code>. This method doesn't
	 * consider the activation status of possible children.
	 *
	 * @return the change's activation status.
	 * @see #setActive(boolean)
	 */
	public boolean isActive();
	
	/**
	 * Returns the name of this change. The main purpose of the change's name is to 
	 * render it in the UI.
	 *
	 * @return the change's name.
	 */
	public String getName();
	
	/**
	 * Returns the language element modified by this <code>IChange</code>. The method
	 * may return <code>null</code> if the change isn't related to a language element.
	 * 
	 * @return the language element modified by this change
	 */
	public Object getModifiedLanguageElement();
	
	/**
	 * Returns whether the change can be undone.
	 * If <code>false</code> is returned, then the result 
	 * of <code>getUndoChange</code> will be ignored.
	 */
	public boolean isUndoable();
}
