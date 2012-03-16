/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import org.eclipse.cdt.debug.ui.breakpoints.IToggleBreakpointsTargetCExtension;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A toggle method breakpoint action that can be contributed an object
 * contribution. The action will toggle method breakpoints on objects
 * that provide an <code>IToggleBreakpointsTarget</code> adapter.
 * <p>
 * This class is based on {@link org.eclipse.debug.ui.actions.ToggleMethodBreakpointActionDelegate }
 * class.  In addition to the copied functionality, it adds the handling of 
 * action-triggering event.
 * </p>
 * 
 * @since 7.2
 */
public class CToggleMethodBreakpointActionDelegate extends CToggleBreakpointObjectActionDelegate {

	protected void performAction(IToggleBreakpointsTarget target, IWorkbenchPart part, ISelection selection, Event event) 
	    throws CoreException 
	{
	    if ((event.stateMask & SWT.MOD1) != 0 && 
	        target instanceof IToggleBreakpointsTargetCExtension &&
	        ((IToggleBreakpointsTargetCExtension)target).canCreateLineBreakpointsInteractive(part, selection)) 
	    {
	        ((IToggleBreakpointsTargetCExtension)target).createLineBreakpointsInteractive(part, selection);
	    } 
	    else {
	        target.toggleMethodBreakpoints(part, selection);
	    }
	}
}
