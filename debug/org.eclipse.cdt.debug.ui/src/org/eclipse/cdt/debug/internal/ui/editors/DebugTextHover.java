/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Nokia - Refactored into CDI specific implementation of AbstractDebugTextHover

 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.ui.editors.AbstractDebugTextHover;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;

/**
 * The text hovering support for C/C++ debugger.
 */

public class DebugTextHover extends AbstractDebugTextHover {

	/**
	 * Returns the evaluation stack frame, or <code>null</code> if none.
	 * 
	 * @return the evaluation stack frame, or <code>null</code> if none
	 */
	protected ICStackFrame getFrame() {
        IAdaptable adaptable = getSelectionAdaptable();
        if (adaptable != null) {
            return (ICStackFrame) adaptable.getAdapter(ICStackFrame.class);
        }
        return null;
	}

	@Override
	protected boolean canEvaluate() {
		ICStackFrame frame = getFrame();
		if (frame != null)
			return frame.canEvaluate();
		
		return false;
	}

	@Override
	protected String evaluateExpression(String expression) {
		ICStackFrame frame = getFrame();
		String result = null;
		try {
			result = frame.evaluateExpressionToString(expression);
		} catch (DebugException e) {
			// ignore
		}
		return result;
	}
}
