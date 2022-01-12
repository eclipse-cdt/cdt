/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

public class GotoAnnotationAction extends TextEditorAction {

	private boolean fForward;

	public GotoAnnotationAction(String prefix, boolean forward) {
		super(ConstructedCEditorMessages.getResourceBundle(), prefix, null);
		fForward = forward;
		if (forward)
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.GOTO_NEXT_ERROR_ACTION);
		else
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.GOTO_PREVIOUS_ERROR_ACTION);
	}

	@Override
	public void run() {
		CEditor e = (CEditor) getTextEditor();
		e.gotoAnnotation(fForward);
	}

	@Override
	public void setEditor(ITextEditor editor) {
		if (editor instanceof CEditor)
			super.setEditor(editor);
		update();
	}

	@Override
	public void update() {
		setEnabled(getTextEditor() instanceof CEditor);
	}
}
