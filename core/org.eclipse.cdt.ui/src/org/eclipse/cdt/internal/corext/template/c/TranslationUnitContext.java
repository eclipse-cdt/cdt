/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.internal.corext.util.Strings;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * A translation unit context.
 */
public abstract class TranslationUnitContext extends DocumentTemplateContext {

	/** The translation unit, may be <code>null</code>. */
	private final ITranslationUnit fTranslationUnit;
	/** A flag to force evaluation in head-less mode. */
	protected boolean fForceEvaluation;
	/** <code>true</code> if the context has a managed (i.e. added to the document) position, <code>false</code> otherwise. */
	protected final boolean fIsManaged;

	/**
	 * Creates a translation unit context.
	 *
	 * @param type the context type
	 * @param document the document
	 * @param completionOffset the completion position within the document
	 * @param completionLength the length of the context
	 * @param translationUnit the translation unit represented by the document
	 */
	protected TranslationUnitContext(TemplateContextType type, IDocument document, int completionOffset,
			int completionLength, ITranslationUnit translationUnit) {
		super(type, document, completionOffset, completionLength);
		fTranslationUnit = translationUnit;
		fIsManaged = false;
	}

	/**
	 * Creates a translation unit context.
	 *
	 * @param type the context type
	 * @param document the document
	 * @param completionPosition the completion position within the document
	 * @param translationUnit the translation unit represented by the document
	 */
	protected TranslationUnitContext(TemplateContextType type, IDocument document, Position completionPosition,
			ITranslationUnit translationUnit) {
		super(type, document, completionPosition);
		fTranslationUnit = translationUnit;
		fIsManaged = true;
	}

	/*
	 * @see org.eclipse.jface.text.templates.DocumentTemplateContext#canEvaluate(org.eclipse.jface.text.templates.Template)
	 */
	@Override
	public boolean canEvaluate(Template template) {
		if (fForceEvaluation)
			return true;

		String key = getKey();
		return template.matches(key, getContextType().getId()) && key.length() != 0
				&& template.getName().startsWith(key);
	}

	/*
	 * @see org.eclipse.cdt.internal.corext.template.DocumentTemplateContext#getKey()
	 */
	@Override
	public String getKey() {
		if (getCompletionLength() == 0)
			return super.getKey();

		try {
			IDocument document = getDocument();

			int start = getStart();
			int end = getCompletionOffset();
			return start <= end ? document.get(start, end - start) : ""; //$NON-NLS-1$

		} catch (BadLocationException e) {
			return super.getKey();
		}
	}

	/**
	 * Returns the translation unit if one is associated with this context, <code>null</code> otherwise.
	 */
	public final ITranslationUnit getTranslationUnit() {
		return fTranslationUnit;
	}

	/**
	 * Returns the enclosing element of a particular element type, <code>null</code>
	 * if no enclosing element of that type exists.
	 */
	public ICElement findEnclosingElement(int elementType) {
		if (fTranslationUnit == null)
			return null;

		try {
			ICElement element = fTranslationUnit.getElementAtOffset(getStart());
			while (element != null && element.getElementType() != elementType)
				element = element.getParent();

			return element;

		} catch (CModelException e) {
			return null;
		}
	}

	/**
	 * Sets whether evaluation is forced or not.
	 *
	 * @param evaluate <code>true</code> in order to force evaluation,
	 *            <code>false</code> otherwise
	 */
	public void setForceEvaluation(boolean evaluate) {
		fForceEvaluation = evaluate;
	}

	/**
	 * Get the associated <code>ICProject</code>.
	 * @return the associated <code>ICProject</code> or <code>null</code>
	 */
	protected ICProject getCProject() {
		ITranslationUnit translationUnit = getTranslationUnit();
		ICProject project = translationUnit == null ? null : translationUnit.getCProject();
		return project;
	}

	/**
	 * Get the indentation level at the position of code completion.
	 * @return the indentation level at the position of code completion
	 */
	protected int getIndentationLevel() {
		int start = getStart();
		IDocument document = getDocument();
		try {
			IRegion region = document.getLineInformationOfOffset(start);
			String lineContent = document.get(region.getOffset(), region.getLength());
			ICProject project = getCProject();
			return Strings.computeIndentUnits(lineContent, CodeFormatterUtil.getTabWidth(project),
					CodeFormatterUtil.getIndentWidth(project));
		} catch (BadLocationException e) {
			return 0;
		}
	}
}
