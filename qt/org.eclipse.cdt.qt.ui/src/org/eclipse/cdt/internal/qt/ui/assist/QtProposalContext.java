/*
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.ui.assist;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.internal.corext.template.c.CContext;
import org.eclipse.cdt.ui.text.contentassist.ICEditorContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Point;

@SuppressWarnings("restriction")
public class QtProposalContext extends CContext {

	private final String contextId;

	public QtProposalContext(ICEditorContentAssistInvocationContext context, TemplateContextType ctxType) {
		super(ctxType, context.getDocument(), getCompletionPosition(context), context.getTranslationUnit());
		this.contextId = ctxType.getId();
	}

	private static Position getCompletionPosition(ICEditorContentAssistInvocationContext context) {
		// The normal CDT behaviour is to not offer template proposals when text is selected.  I
		// don't know why they avoid it, so I've opted to replace the selection instead.

		int adjustment = 0;
		IASTCompletionNode node = context.getCompletionNode();
		if (node != null) {
			String prefix = node.getPrefix();
			if (prefix != null)
				adjustment -= prefix.length();
		}

		int length = -adjustment;
		ITextViewer viewer = context.getViewer();
		if (viewer != null) {
			Point selection = viewer.getSelectedRange();
			if (selection != null && selection.y > 0)
				length += selection.y;
		}

		int offset = context.getInvocationOffset() + adjustment;
		return new Position(offset, length);
	}

	@Override
	public boolean canEvaluate(Template template) {
		// The base implementation uses a length of 0 to create an empty string for the key
		// and then refuses to apply the template.  This override offers all templates that
		// have the right ID.  This is ok, because only the templates that apply were proposed.
		return contextId.equals(template.getContextTypeId());
	}

	@Override
	public int getStart() {
		// The base implementation creates a different offset when the replacement length
		// is not 0.  We need to use the same start of the replacement region regardless of
		// whether or not characters are selected.

		try {
			IDocument document = getDocument();

			int start = getCompletionOffset();
			int end = getCompletionOffset() + getCompletionLength();

			while (start != 0 && isUnicodeIdentifierPartOrPoundSign(document.getChar(start - 1)))
				start--;

			while (start != end && Character.isWhitespace(document.getChar(start)))
				start++;

			if (start == end)
				start = getCompletionOffset();

			return start;
		} catch (BadLocationException e) {
			return super.getStart();
		}
	}

	private boolean isUnicodeIdentifierPartOrPoundSign(char c) {
		return Character.isUnicodeIdentifierPart(c) || c == '#';
	}
}
