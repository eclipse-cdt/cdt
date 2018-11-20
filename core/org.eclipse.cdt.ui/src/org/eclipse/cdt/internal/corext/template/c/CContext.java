/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
 *     QNX Software Systems
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;

/**
 * A context for C/C++.
 */
public class CContext extends TranslationUnitContext {
	/**
	 * Creates a C/C++ code template context.
	 *
	 * @param type the context type
	 * @param document the document
	 * @param completionOffset the completion position within the document
	 * @param completionLength the length of the context
	 * @param translationUnit the translation unit represented by the document
	 */
	public CContext(TemplateContextType type, IDocument document, int completionOffset, int completionLength,
			ITranslationUnit translationUnit) {
		super(type, document, completionOffset, completionLength, translationUnit);
	}

	/**
	 * Creates a C/C++ code template context.
	 *
	 * @param type the context type.
	 * @param document the document.
	 * @param completionPosition the completion position within the document
	 * @param translationUnit the translation unit (may be <code>null</code>).
	 */
	public CContext(TemplateContextType type, IDocument document, Position completionPosition,
			ITranslationUnit translationUnit) {
		super(type, document, completionPosition, translationUnit);
	}

	@Override
	public int getStart() {
		if (fIsManaged && getCompletionLength() > 0)
			return super.getStart();

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

	@Override
	public int getEnd() {
		if (fIsManaged || getCompletionLength() == 0)
			return super.getEnd();

		try {
			IDocument document = getDocument();

			int start = getCompletionOffset();
			int end = getCompletionOffset() + getCompletionLength();

			while (start != end && Character.isWhitespace(document.getChar(end - 1)))
				end--;

			return end;
		} catch (BadLocationException e) {
			return super.getEnd();
		}
	}

	@Override
	public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {
		if (!canEvaluate(template))
			return null;

		TemplateTranslator translator = new TemplateTranslator();
		TemplateBuffer buffer = translator.translate(template.getPattern());

		getContextType().resolve(buffer, this);

		IPreferenceStore prefs = CUIPlugin.getDefault().getPreferenceStore();
		boolean useCodeFormatter = prefs.getBoolean(PreferenceConstants.TEMPLATES_USE_CODEFORMATTER);

		ICProject project = getCProject();
		int indentationLevel = isReadOnly() ? 0 : getIndentationLevel();
		CFormatter formatter = new CFormatter(TextUtilities.getDefaultLineDelimiter(getDocument()), indentationLevel,
				useCodeFormatter, project);
		formatter.format(buffer, this);

		return buffer;
	}

	private boolean isUnicodeIdentifierPartOrPoundSign(char c) {
		return Character.isUnicodeIdentifierPart(c) || c == '#';
	}
}
