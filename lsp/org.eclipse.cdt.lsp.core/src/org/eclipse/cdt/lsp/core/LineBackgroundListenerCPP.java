/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.core;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class LineBackgroundListenerCPP implements LineBackgroundListener {
	private IDocument currentDocument;
	private Color lineBackgroundColor;

	public void setCurrentDocument(IDocument currentDocument) {
		this.currentDocument = currentDocument;
	}

	@Override
	public void lineGetBackground(LineBackgroundEvent event) {
		lineBackgroundColor = new Color(Display.getCurrent(),
				PreferenceConverter.getColor(CUIPlugin.getDefault().getPreferenceStore(), CEditor.INACTIVE_CODE_COLOR));
		if (currentDocument == null) {
			return;
		}

		Position[] inactivePositions = null;
		try {
			inactivePositions = currentDocument
					.getPositions(PresentationReconcilerCPP.INACTIVE_CODE_HIGHLIGHTING_POSITION_CATEGORY);
		} catch (BadPositionCategoryException e) {
			Activator.log(e);
		}

		if (inactivePositions == null) {
			return;
		}

		try {
			for (Position eachInactivePosition : inactivePositions) {
				int regionStartLine = currentDocument.getLineOfOffset(eachInactivePosition.getOffset());
				int regionEndLine = currentDocument
						.getLineOfOffset(eachInactivePosition.getOffset() + eachInactivePosition.getLength());
				if (event.lineOffset >= currentDocument.getLineOffset(regionStartLine)
						&& event.lineOffset <= currentDocument.getLineOffset(regionEndLine)) {
					event.lineBackground = lineBackgroundColor;
					break;
				}
			}
		} catch (BadLocationException e) {
			Activator.log(e);
		}
	}
}
