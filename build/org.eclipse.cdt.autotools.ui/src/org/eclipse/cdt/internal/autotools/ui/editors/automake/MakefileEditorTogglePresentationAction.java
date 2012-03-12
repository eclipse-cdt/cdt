/*******************************************************************************
 * Copyright (c) 2000, 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - modified for Automake editor usage
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.internal.autotools.ui.MakeUIImages;
import org.eclipse.cdt.internal.autotools.ui.MakeUIMessages;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;


/**
 */
public class MakefileEditorTogglePresentationAction extends TextEditorAction {

	/**
	 * Constructor for MakefileEditorTogglePresentationAction.
	 */
	public MakefileEditorTogglePresentationAction() {
		super(MakeUIMessages.getResourceBundle(), "TogglePresentation.", null); //$NON-NLS-1$
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
		setChecked(getTextEditor() != null && getTextEditor().showsHighlightRangeOnly());
		setEnabled(true);
	}

}
