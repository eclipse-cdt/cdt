/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
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
package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 */
public class MakefileEditorTogglePresentationAction extends TextEditorAction {
	/**
	 * Constructor for MakefileEditorTogglePresentationAction.
	 */
	public MakefileEditorTogglePresentationAction() {
		super(MakeUIPlugin.getDefault().getResourceBundle(), "TogglePresentation.", null); //$NON-NLS-1$
		setDisabledImageDescriptor(MakeUIImages.getImageDescriptor(MakeUIImages.IMG_DTOOL_SEGMENT_EDIT));
		setImageDescriptor(MakeUIImages.getImageDescriptor(MakeUIImages.IMG_ETOOL_SEGMENT_EDIT));
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
