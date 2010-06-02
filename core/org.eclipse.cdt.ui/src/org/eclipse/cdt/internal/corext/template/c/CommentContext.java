/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * A context for (block) comments.
 *
 * @since 4.0
 */
public class CommentContext extends TranslationUnitContext {

	/**
	 * Creates a comment template context.
	 * 
	 * @param type the context type.
	 * @param document the document.
	 * @param completionOffset the completion offset within the document.
	 * @param completionLength the completion length within the document.
	 * @param translationUnit the translation unit (may be <code>null</code>).
	 */
	public CommentContext(TemplateContextType type, IDocument document,
			int completionOffset, int completionLength, ITranslationUnit translationUnit) {
		super(type, document, completionOffset, completionLength, translationUnit);
	}

	/**
	 * Creates a comment template context.
	 * 
	 * @param type the context type.
	 * @param document the document.
	 * @param completionPosition the completion position within the document
	 * @param translationUnit the translation unit (may be <code>null</code>).
	 */
	public CommentContext(TemplateContextType type, IDocument document,
			Position completionPosition, ITranslationUnit translationUnit) {
		super(type, document, completionPosition, translationUnit);
	}

	/*
	 * @see DocumentTemplateContext#getStart()
	 */ 
	@Override
	public int getStart() {
		if (fIsManaged && getCompletionLength() > 0)
			return super.getStart();
		
		try {
			IDocument document= getDocument();

			if (getCompletionLength() == 0) {
				int start= getCompletionOffset();
		
				while ((start != 0) && !Character.isWhitespace(document.getChar(start - 1)))
					start--;
				
				if ((start != 0) && !Character.isWhitespace(document.getChar(start - 1)))
					start--;
		
				return start;
				
			} 

			int start= getCompletionOffset();
			int end= getCompletionOffset() + getCompletionLength();
			
			while (start != 0 && !Character.isWhitespace(document.getChar(start - 1)))
				start--;
			
			while (start != end && Character.isWhitespace(document.getChar(start)))
				start++;
			
			if (start == end)
				start= getCompletionOffset();	
			
			return start;					
			

		} catch (BadLocationException e) {
			return getCompletionOffset();	
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.template.DocumentTemplateContext#getEnd()
	 */
	@Override
	public int getEnd() {
		if (fIsManaged || getCompletionLength() == 0)		
			return super.getEnd();

		try {			
			IDocument document= getDocument();

			int start= getCompletionOffset();
			int end= getCompletionOffset() + getCompletionLength();
			
			while (start != end && Character.isWhitespace(document.getChar(end - 1)))
				end--;
			
			return end;	

		} catch (BadLocationException e) {
			return super.getEnd();
		}		
	}

	/*
	 * @see TemplateContext#evaluate(Template)
	 */
	@Override
	public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {
		TemplateTranslator translator= new TemplateTranslator();
		TemplateBuffer buffer= translator.translate(template);

		getContextType().resolve(buffer, this);
		
		// don't use code formatter for comment templates
		boolean useCodeFormatter= false;

		ICProject project= getCProject();
		CFormatter formatter= new CFormatter(TextUtilities.getDefaultLineDelimiter(getDocument()), getIndentationLevel(), useCodeFormatter, project);
		formatter.format(buffer, this);
			
		return buffer;
	}

}
