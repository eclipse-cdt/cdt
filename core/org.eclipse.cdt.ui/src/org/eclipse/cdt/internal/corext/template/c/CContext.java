/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QnX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.internal.ui.util.Strings;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;



/**
 * A context for c/c++
 */
public class CContext extends TranslationUnitContext {	

	/** The platform default line delimiter. */
	private static final String PLATFORM_LINE_DELIMITER= System.getProperty("line.separator"); //$NON-NLS-1$

	/**
	 * Creates a javadoc template context.
	 * 
	 * @param type   the context type.
	 * @param string the document string.
	 * @param completionPosition the completion position within the document.
	 * @param unit the compilation unit (may be <code>null</code>).
	 */
	public CContext(TemplateContextType type, IDocument document, int completionOffset, int completionLength,
		ITranslationUnit translationUnit) {
		super(type, document, completionOffset, completionLength, translationUnit);
	}

	/*
	 * @see DocumentTemplateContext#getStart()
	 */ 
	public int getStart() {
		try {
			IDocument document= getDocument();

			if (getCompletionLength() == 0) {

				int start= getCompletionOffset();		
				while ((start != 0) && Character.isUnicodeIdentifierPart(document.getChar(start - 1)))
					start--;
					
				if ((start != 0) && Character.isUnicodeIdentifierStart(document.getChar(start - 1)))
					start--;
		
				return start;
			
			}

			int start= getCompletionOffset();
			int end= getCompletionOffset() + getCompletionLength();
			
			while (start != 0 && Character.isUnicodeIdentifierPart(document.getChar(start - 1))) {
				start--;
			}
			
			while (start != end && Character.isWhitespace(document.getChar(start))) {
				start++;
			}
			
			if (start == end) {
				start= getCompletionOffset();
			}
			
			return start;	
		} catch (BadLocationException e) {
			return super.getStart();	
		}

	}

	public int getEnd() {
		
		if (getCompletionLength() == 0)
			return super.getEnd();

		try {			
			IDocument document= getDocument();

			int start= getCompletionOffset();
			int end= getCompletionOffset() + getCompletionLength();
			
			while (start != end && Character.isWhitespace(document.getChar(end - 1))) {
				end--;
			}
			
			return end;	
		} catch (BadLocationException e) {
			return super.getEnd();
		}		
	}
	
	/*
	 * @see TemplateContext#canEvaluate(Template templates)
	 */
	public boolean canEvaluate(Template template) {
		String key= getKey();
		return template.matches(key, getContextType().getId())
			&& key.length() != 0 && template.getName().toLowerCase().startsWith(key.toLowerCase());
		//return template.matches(getKey(), getContextType().getName());
	}

	/*
	 * @see TemplateContext#evaluate(Template)
	 */
	public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {
		if (!canEvaluate(template))
			return null;
			
		TemplateTranslator translator= new TemplateTranslator();
		TemplateBuffer buffer= translator.translate(template.getPattern());

		getContextType().resolve(buffer, this);

		String lineDelimiter= null;
		try {
			lineDelimiter= getDocument().getLineDelimiter(0);
		} catch (BadLocationException e) {
		}

		if (lineDelimiter == null) {
			lineDelimiter= PLATFORM_LINE_DELIMITER;
		}

		IPreferenceStore prefs= CUIPlugin.getDefault().getPreferenceStore();
		boolean useCodeFormatter= prefs.getBoolean(PreferenceConstants.TEMPLATES_USE_CODEFORMATTER);			

		CFormatter formatter= new CFormatter(lineDelimiter, getIndentation(), useCodeFormatter);
		formatter.edit(buffer, this, getIndentation());					
		return buffer;
	}

	/**
	 * Returns the indentation level at the position of code completion.
	 */
	private int getIndentation() {
		int start= getStart();
		IDocument document= getDocument();
		try {
			IRegion region= document.getLineInformationOfOffset(start);
			String lineContent= document.get(region.getOffset(), region.getLength());
			return Strings.computeIndent(lineContent, CodeFormatterUtil.getTabWidth());
		} catch (BadLocationException e) {
			return 0;
		}
	}	

}



