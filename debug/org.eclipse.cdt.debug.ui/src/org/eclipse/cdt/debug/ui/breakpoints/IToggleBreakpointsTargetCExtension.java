/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Second extension interface for {@link org.eclipse.debug.ui.actions.IToggleBreakpointsTarget}.
 * This interface provides the ability open edit a breakpoint's properties and 
 * to create a breakpoint in the given context with additional user input 
 * (such as using a dialog or a wizard).
 * <p>
 * Clients implementing <code>IToggleBreakpointsTarget</code> may optionally
 * implement this interface.
 * </p>
 * @since 7.2
 * @see org.eclipse.debug.ui.actions.ToggleBreakpointAction
 */
public interface IToggleBreakpointsTargetCExtension extends IToggleBreakpointsTargetExtension {

    /**
     * Returns whether the toggle target can create a a breakpoint at the 
     * given location.  If the implementation does not support creating the 
     * breakpoint interactively then it should return <code>false</code>.
     * <p>
     * The selection varies depending on the given part. For example,
     * a text selection is provided for text editors, and a structured
     * selection is provided for tree views, and may be a multi-selection.
     * </p>
     * @param part the part on which the action has been invoked  
     * @param selection selection on which line breakpoints should be toggled
     * @return Returns <code>true</code> if toggle target is able interactively
     * create a breakpoint(s) at the given location.
     */
    public boolean canCreateBreakpointsInteractive(IWorkbenchPart part, ISelection selection);

	/**
     * Creates new breakpoints interactively.  The implementation should allows
     * the user to edit all of the breakpoint's settings prior to creating the 
     * breakpoint.  
     * <p>
     * The selection varies depending on the given part. For example,
     * a text selection is provided for text editors, and a structured
     * selection is provided for tree views, and may be a multi-selection.
     * </p>
     * @param part the part on which the action has been invoked  
     * @param selection selection on which line breakpoints should be toggled
     * @throws CoreException if unable to perform the action 
     */
    public void createBreakpointsInteractive(IWorkbenchPart part, ISelection selection) throws CoreException;
}
