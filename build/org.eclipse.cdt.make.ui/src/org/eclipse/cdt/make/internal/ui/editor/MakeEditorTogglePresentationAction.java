/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 */
public class MakeEditorTogglePresentationAction extends TextEditorAction {

	private final static String ACTION_ID = "org.eclipse.cdt.make.ui.MakeEditorTogglePresentationAction"; //$NON-NLS-1$
	/**
	 * Constructor for MakeEditorTogglePresentationAction.
	 * @param bundle
	 * @param prefix
	 * @param editor
	 */
	public MakeEditorTogglePresentationAction() {
		super(MakeUIPlugin.getDefault().getResourceBundle(), "MakeEditorTogglePresentationAction.", null); //$NON-NLS-1$

		setToolTipText("MakeEditorTogglePresentationAction.tooltip"); //$NON-NLS-1$
		setActionDefinitionId(ACTION_ID);
		update();

	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		super.run();
		ITextEditor editor = getTextEditor();
		if (editor == null)
			return;
		if (!(editor instanceof MakeTextEditor))
			return;
		((MakeTextEditor) editor).setPresentationState(!((MakeTextEditor) editor).getPresentationState());
		// update();
	}

	/**
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setChecked(isChecked());
		setEnabled(isEnabled());
	}

	/**
	 * @see org.eclipse.ui.texteditor.TextEditorAction#setEditor(ITextEditor)
	 */
	public void setEditor(ITextEditor editor) {
		super.setEditor(editor);
		if (editor instanceof MakeTextEditor) {
			((MakeTextEditor) editor).setPresentationState(isChecked());
		}
		update();
	}

}
