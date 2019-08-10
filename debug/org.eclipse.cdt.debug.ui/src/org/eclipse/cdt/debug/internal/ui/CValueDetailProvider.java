/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IValueDetailListener;

/**
 * Computes a detailed description of the given value.
 */
public class CValueDetailProvider {

	//The shared instance.
	private static CValueDetailProvider fInstance = null;

	public static CValueDetailProvider getDefault() {
		if (fInstance == null) {
			fInstance = new CValueDetailProvider();
		}
		return fInstance;
	}

	public void computeDetail(final IValue value, final IValueDetailListener listener) {
		if (value instanceof ICValue) {
			final ICStackFrame frame = CDebugUIUtils.getCurrentStackFrame();
			if (frame != null) {
				DebugPlugin.getDefault()
						.asyncExec(() -> listener.detailComputed(value, ((ICValue) value).evaluateAsExpression(frame)));
			} else { // no valid stack frame, clear detail pane
				listener.detailComputed(value, ""); //$NON-NLS-1$
			}
		}
	}
}
