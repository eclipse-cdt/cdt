/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.pullup.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;

/**
 * Encapsulates the logic of moving a single member from one class to another.
 * 
 * @author Simon Taddiken
 */
public interface MoveAction {
	
	/**
	 * Whether the member to be moved can be inserted in the target class. If not, the
	 * result is <code>false</code> and the <tt>status</tt> is filled with a proper error
	 * message.
	 * @param status Status to fill if moving is not possible
	 * @param pm ProgressMonitor for progress reporting
	 * @return Whether this action can be executed without breaking code.
	 */
	public boolean isPossible(RefactoringStatus status, IProgressMonitor pm);
	
	/**
	 * Executes this action by applying required changes to the source and target ASTs.
	 * 
	 * @param mc ModificationCollector which provides {@link ASTRewrite} instances.
	 * @param editGroup EditGroup for the resulting changes.
	 * @param pm ProgressMonitor for progress reporting
	 * @throws CoreException If running this action fails for any reason.
	 */
	public void run(ModificationCollector mc, TextEditGroup editGroup, IProgressMonitor pm) 
			throws CoreException;
}
