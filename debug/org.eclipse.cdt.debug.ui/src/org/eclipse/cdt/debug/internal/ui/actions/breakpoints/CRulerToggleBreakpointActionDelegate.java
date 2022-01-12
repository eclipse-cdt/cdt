/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import org.eclipse.cdt.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.actions.ToggleBreakpointAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This is a copy of the RulerToggleBreakpointActionDelegate in platform.
 * It updates the toggle action to include an accelertor text in its label.
 * See bug 374153.
 *
 * @see org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate
 */
public class CRulerToggleBreakpointActionDelegate extends AbstractRulerActionDelegate {

	private IEditorPart fEditor = null;
	private ToggleBreakpointAction fDelegate = null;

	@Override
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		fDelegate = new ToggleBreakpointAction(editor, null, rulerInfo);
		fDelegate.setText(ActionMessages.getString("CRulerToggleBreakpointActionDelegate_label")); //$NON-NLS-1$

		return fDelegate;
	}

	@Override
	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
		if (fEditor != null) {
			if (fDelegate != null) {
				fDelegate.dispose();
				fDelegate = null;
			}
		}
		fEditor = targetEditor;
		super.setActiveEditor(callerAction, targetEditor);
	}

	@Override
	public void init(IAction action) {
	}

	@Override
	public void dispose() {
		if (fDelegate != null) {
			fDelegate.dispose();
		}
		fDelegate = null;
		fEditor = null;
	}

	@Override
	public void runWithEvent(IAction action, Event event) {
		if (fDelegate != null) {
			fDelegate.runWithEvent(event);
		}
	}
}
