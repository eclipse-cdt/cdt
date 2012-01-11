/*******************************************************************************
 * Copyright (c) 2011 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.param.RootProblemPreference;
import org.eclipse.cdt.codan.internal.checkers.CaseBreakChecker;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;

public class CaseBreakQuickFixComment extends AbstractCodanCMarkerResolution {
	@Override
	public String getLabel() {
		return Messages.CaseBreakQuickFixComment_Label;
	}

	@Override
	public void apply(IMarker marker, IDocument document) {
		try {
			int line = marker.getAttribute(IMarker.LINE_NUMBER, -1);
			if (line < 0)
				return;
			int offset = document.getLineOffset(line);
			String indent = getIndentationStr(document, line);
			String comment = getNoBreakComment(marker);
			String editStr = String.format("%s/* %s */\n", indent, comment);//$NON-NLS-1$ 
			InsertEdit edit = new InsertEdit(offset, editStr);
			edit.apply(document);
		} catch (MalformedTreeException e) {
			return;
		} catch (BadLocationException e) {
			return;
		}
	}

	private String getIndentationStr(IDocument document, int line) throws BadLocationException {
		int prevLine = line - 1;
		IRegion lineInformation = document.getLineInformation(prevLine);
		String prevLineStr = document.get(lineInformation.getOffset(), lineInformation.getLength());
		int nonSpace = prevLineStr.indexOf(prevLineStr.trim());
		String indent = prevLineStr.substring(0, nonSpace);
		return indent;
	}

	private String getNoBreakComment(IMarker marker) {
		IProblem problem = getProblem(marker);
		RootProblemPreference map = (RootProblemPreference) problem.getPreference();
		String comment = (String) map.getChildValue(CaseBreakChecker.PARAM_NO_BREAK_COMMENT);
		if (comment == null || comment.trim().length() == 0)
			comment = "no break"; //$NON-NLS-1$
		return comment;
	}
}
