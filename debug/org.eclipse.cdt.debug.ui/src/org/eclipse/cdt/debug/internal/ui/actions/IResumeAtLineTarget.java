/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.actions; 

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
 
/**
 * An adapter for a "resume at line" operation.
 */
public interface IResumeAtLineTarget {
	/**
	 * Perform a resume at line operation on the given element that is 
	 * currently selected and suspended in the Debug view.
	 *  
	 * @param part the part on which the action has been invoked
	 * @param selection the selection on which the action has been invoked
	 * @param target suspended element to perform the "resume at line" action on
	 * @throws CoreException if unable to perform the action 
	 */
	public void resumeAtLine( IWorkbenchPart part, ISelection selection, ISuspendResume target ) throws CoreException;

	/**
	 * Returns whether a resume at line operation can be performed on the given
	 * element that is currently selected and suspended in the Debug view.
	 * 
	 * @param part the part on which the action has been invoked
	 * @param selection the selection on which the action has been invoked
	 * @param target suspended element to perform the "resume at line" action on
	 * @throws CoreException if unable to perform the action 
	 */
	public boolean canResumeAtLine( IWorkbenchPart part, ISelection selection, ISuspendResume target );
}
