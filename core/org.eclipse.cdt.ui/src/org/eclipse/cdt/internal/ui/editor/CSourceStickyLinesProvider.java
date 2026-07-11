/*******************************************************************************
 * Copyright (c) 2025, 2026 Red Hat and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - reference Java implementation
 *     John Dallaway - initial C/C++ implementation (#1455)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.stickyscroll.IStickyLine;
import org.eclipse.ui.texteditor.stickyscroll.IStickyLinesProvider;
import org.eclipse.ui.texteditor.stickyscroll.StickyLine;

public class CSourceStickyLinesProvider implements IStickyLinesProvider {

	@Override
	public List<IStickyLine> getStickyLines(ISourceViewer sourceViewer, int lineNumber,
			StickyLinesProperties properties) {
		final List<IStickyLine> stickyLines = new LinkedList<>();
		final IEditorPart editor = properties.editor();
		final ICElement inputElement = getInputElement(editor);
		if (inputElement instanceof ITranslationUnit tu) {
			final IStatus status = CUIPlugin.getDefault().getASTProvider().runOnAST(tu, ASTProvider.WAIT_IF_OPEN, null,
					(lang, ast) -> {
						final IDocument document = sourceViewer.getDocument();
						final CSourceStickyLinesProcessor processor = new CSourceStickyLinesProcessor(document, ast);
						try {
							processor.calculateStickyLines(lineNumber)
									.forEach(line -> stickyLines.add(new StickyLine(line, sourceViewer)));
						} catch (BadLocationException e) {
							return Status.error("Error calculating sticky lines", e); //$NON-NLS-1$
						}
						return Status.OK_STATUS;
					});
			if (!status.isOK()) {
				ILog.get().log(status);
			}
		}
		return stickyLines;
	}

	private ICElement getInputElement(IEditorPart part) {
		final IEditorInput editorInput = part.getEditorInput();
		if (null != editorInput) {
			return CDTUITools.getEditorInputCElement(editorInput);
		}
		return null;
	}

}
