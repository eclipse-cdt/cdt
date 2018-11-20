/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems and others.
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
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * TextOperationAction
 */
public class TextOperationAction extends Action implements IUpdate {

	private int fOperationCode = -1;
	private ITextOperationTarget fOperationTarget;

	public TextOperationAction(ITextViewer viewer, int operationCode) {
		fOperationCode = operationCode;
		fOperationTarget = viewer.getTextOperationTarget();
		update();
	}

	/**
	 * Updates the enabled state of the action.
	 * Fires a property change if the enabled state changes.
	 *
	 * @see Action#firePropertyChange(String, Object, Object)
	 */
	@Override
	public void update() {

		boolean wasEnabled = isEnabled();
		boolean isEnabled = (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
		setEnabled(isEnabled);

		if (wasEnabled != isEnabled) {
			firePropertyChange(ENABLED, wasEnabled ? Boolean.TRUE : Boolean.FALSE,
					isEnabled ? Boolean.TRUE : Boolean.FALSE);
		}
	}

	/**
	 * @see Action#run()
	 */
	@Override
	public void run() {
		if (fOperationCode != -1 && fOperationTarget != null) {
			fOperationTarget.doOperation(fOperationCode);
		}
	}
}
