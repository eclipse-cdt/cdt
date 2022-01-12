/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	 * Returns whether the toggle target can create a line breakpoint at the
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
	public boolean canCreateLineBreakpointsInteractive(IWorkbenchPart part, ISelection selection);

	/**
	 * Creates new line breakpoints interactively.  The implementation should
	 * allows the user to edit all of the breakpoint's settings prior to
	 * creating the breakpoint.  Unlike the
	 * {@link #toggleLineBreakpoints(IWorkbenchPart, ISelection)}
	 * method, this method does not remove the existing breakpoint at given
	 * location.  It always creates a new breakpoint
	 * <p>
	 * The selection varies depending on the given part. For example,
	 * a text selection is provided for text editors, and a structured
	 * selection is provided for tree views, and may be a multi-selection.
	 * </p>
	 * @param part the part on which the action has been invoked
	 * @param selection selection on which line breakpoints should be toggled
	 * @throws CoreException if unable to perform the action
	 */
	public void createLineBreakpointsInteractive(IWorkbenchPart part, ISelection selection) throws CoreException;

	/**
	 * Returns whether the toggle target can create a watchpoint at the
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
	public boolean canCreateWatchpointsInteractive(IWorkbenchPart part, ISelection selection);

	/**
	 * Creates new watchpoint interactively.  The implementation should
	 * allows the user to edit all of the breakpoint's settings prior to
	 * creating the breakpoint.  Unlike the
	 * {@link #toggleLineBreakpoints(IWorkbenchPart, ISelection)}
	 * method, this method does not remove the existing breakpoint at given
	 * location.  It always creates a new breakpoint
	 * <p>
	 * The selection varies depending on the given part. For example,
	 * a text selection is provided for text editors, and a structured
	 * selection is provided for tree views, and may be a multi-selection.
	 * </p>
	 * @param part the part on which the action has been invoked
	 * @param selection selection on which line breakpoints should be toggled
	 * @throws CoreException if unable to perform the action
	 */
	public void createWatchpointsInteractive(IWorkbenchPart part, ISelection selection) throws CoreException;

	/**
	 * Returns whether the toggle target can create a function breakpoint at the
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
	public boolean canCreateFunctionBreakpointInteractive(IWorkbenchPart part, ISelection selection);

	/**
	 * Creates new function breakpoint interactively.  The implementation should
	 * allows the user to edit all of the breakpoint's settings prior to
	 * creating the breakpoint.  Unlike the
	 * {@link #toggleLineBreakpoints(IWorkbenchPart, ISelection)}
	 * method, this method does not remove the existing breakpoint at given
	 * location.  It always creates a new breakpoint
	 * <p>
	 * The selection varies depending on the given part. For example,
	 * a text selection is provided for text editors, and a structured
	 * selection is provided for tree views, and may be a multi-selection.
	 * </p>
	 * @param part the part on which the action has been invoked
	 * @param selection selection on which line breakpoints should be toggled
	 * @throws CoreException if unable to perform the action
	 */
	public void createFunctionBreakpointInteractive(IWorkbenchPart part, ISelection selection) throws CoreException;

	/**
	 * Returns whether the toggle target can create an event breakpoint at the
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
	public boolean canCreateEventBreakpointsInteractive(IWorkbenchPart part, ISelection selection);

	/**
	 * Creates a new event breakpoint interactively.  The implementation should
	 * allows the user to edit all of the breakpoint's settings prior to
	 * creating the breakpoint.  Unlike the
	 * {@link #toggleLineBreakpoints(IWorkbenchPart, ISelection)}
	 * method, this method does not remove the existing breakpoint at given
	 * location.  It always creates a new breakpoint
	 * <p>
	 * The selection varies depending on the given part. For example,
	 * a text selection is provided for text editors, and a structured
	 * selection is provided for tree views, and may be a multi-selection.
	 * </p>
	 * @param part the part on which the action has been invoked
	 * @param selection selection on which line breakpoints should be toggled
	 * @throws CoreException if unable to perform the action
	 */
	public void createEventBreakpointsInteractive(IWorkbenchPart part, ISelection selection) throws CoreException;

}
