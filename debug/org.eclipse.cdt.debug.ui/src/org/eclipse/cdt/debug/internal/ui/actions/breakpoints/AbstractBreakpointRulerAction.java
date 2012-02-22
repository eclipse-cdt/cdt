/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems) - bug 183397
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.RulerBreakpointAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Abstract base implementation of the breakpoint ruler actions.
 * 
 * @see {@link RulerBreakpointAction} 
 */
public abstract class AbstractBreakpointRulerAction extends Action implements IUpdate {
	
	private final IWorkbenchPart fTargetPart;
	private final IVerticalRulerInfo fRulerInfo;
	
	/**
	 * Constructs an action to work on breakpoints in the specified
	 * part with the specified vertical ruler information.
	 * 
	 * @param part  a text editor or DisassemblyView
	 * @param info  vertical ruler information
	 */
	public AbstractBreakpointRulerAction(IWorkbenchPart part, IVerticalRulerInfo info) {
		Assert.isTrue(part instanceof ITextEditor);
		fTargetPart = part;
		fRulerInfo = info;
	}

	/**
	 * Returns the breakpoint at the last line of mouse activity in the ruler
	 * or <code>null</code> if none.
	 * 
	 * @return breakpoint associated with activity in the ruler or <code>null</code>
	 */
	protected IBreakpoint getBreakpoint() {
	    IWorkbenchPart targetPart = getTargetPart();
	    if (targetPart instanceof ITextEditor) {
	        return CDebugUIUtils.getBreakpointFromEditor((ITextEditor)targetPart, getVerticalRulerInfo());
	    }
	    return null;
	}
	
	/**
	 * Returns the workbench part this action was created for.
	 * 
	 * @return workbench part, a text editor or a DisassemblyView
	 */
	protected IWorkbenchPart getTargetPart() {
		return fTargetPart;
	}
	
	/**
	 * Returns the vertical ruler information this action was created for.
	 * 
	 * @return vertical ruler information
	 */
	protected IVerticalRulerInfo getVerticalRulerInfo() {
		return fRulerInfo;
	}

}
