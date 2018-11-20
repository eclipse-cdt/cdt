/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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

	@Override
	public void run() {
		ITextEditor editor = getTextEditor();
		editor.resetHighlightRange();
		boolean show = editor.showsHighlightRangeOnly();
		setChecked(!show);
		editor.showHighlightRangeOnly(!show);
	}

	@Override
	public void update() {
		setChecked(getTextEditor() != null && getTextEditor().showsHighlightRangeOnly());
		setEnabled(true);
	}

}
