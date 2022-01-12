/*******************************************************************************
 * Copyright (c) 2008 Freescale Secmiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * An adapter for a "move to line" operation.
 */
public interface IMoveToLineTarget {
	/**
	 * Perform a move to line operation on the given element that is
	 * currently selected and suspended in the Debug view.
	 *
	 * @param part the part on which the action has been invoked
	 * @param selection the selection on which the action has been invoked
	 * @param target suspended element to perform the "resume at line" action on
	 * @throws CoreException if unable to perform the action
	 */
	public void moveToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) throws CoreException;

	/**
	 * Returns whether a move to line operation can be performed on the given
	 * element that is currently selected and suspended in the Debug view.
	 *
	 * @param part the part on which the action has been invoked
	 * @param selection the selection on which the action has been invoked
	 * @param target suspended element to perform the "resume at line" action on
	 * @throws CoreException if unable to perform the action
	 */
	public boolean canMoveToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target);
}
