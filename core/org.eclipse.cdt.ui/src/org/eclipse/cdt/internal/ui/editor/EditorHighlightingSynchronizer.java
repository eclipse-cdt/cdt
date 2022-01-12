/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;

/**
 * Turns off occurrences highlighting on a C editor until linked mode is left.
 *
 * @since 5.0
 */
public class EditorHighlightingSynchronizer implements ILinkedModeListener {
	private final CEditor fEditor;
	private final boolean fWasOccurrencesOn;

	/**
	 * Creates a new synchronizer.
	 *
	 * @param editor the editor the occurrences markers of which will be
	 *        synchronized with the linked mode
	 */
	public EditorHighlightingSynchronizer(CEditor editor) {
		Assert.isLegal(editor != null);
		fEditor = editor;
		fWasOccurrencesOn = fEditor.isMarkingOccurrences();

		if (fWasOccurrencesOn && !isEditorDisposed())
			fEditor.uninstallOccurrencesFinder();
	}

	@Override
	public void left(LinkedModeModel environment, int flags) {
		if (fWasOccurrencesOn && !isEditorDisposed())
			fEditor.installOccurrencesFinder(true);
	}

	private boolean isEditorDisposed() {
		return fEditor == null || fEditor.getSelectionProvider() == null;
	}

	@Override
	public void suspend(LinkedModeModel environment) {
	}

	@Override
	public void resume(LinkedModeModel environment, int flags) {
	}
}
