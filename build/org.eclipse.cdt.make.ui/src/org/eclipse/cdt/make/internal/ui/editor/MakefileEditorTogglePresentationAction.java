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

import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 */
public class MakefileEditorTogglePresentationAction extends TextEditorAction {

	private final static String ACTION_ID = "org.eclipse.cdt.make.ui.MakefileEditorTogglePresentationAction"; //$NON-NLS-1$
	/**
	 * Constructor for MakefileEditorTogglePresentationAction.
	 * @param bundle
	 * @param prefix
	 * @param editor
	 */
	public MakefileEditorTogglePresentationAction() {
		super(MakeUIPlugin.getDefault().getResourceBundle(), "MakefileEditorTogglePresentationAction.", null); //$NON-NLS-1$

		setToolTipText("MakefileEditorTogglePresentationAction.tooltip"); //$NON-NLS-1$
		setActionDefinitionId(ACTION_ID);
		MakeUIImages.setImageDescriptors(this, MakeUIImages.T_TOOL, MakeUIImages.IMG_TOOLS_MAKEFILE_SEGMENT_EDIT);
		update();
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		ITextEditor editor= getTextEditor();
		editor.resetHighlightRange();
		boolean show = editor.showsHighlightRangeOnly();
		setChecked(!show);
		editor.showHighlightRangeOnly(!show);
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
		update();
	}

}
