/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;

import org.eclipse.cdt.internal.corext.template.c.TranslationUnitContext;
import org.eclipse.cdt.internal.corext.template.c.TranslationUnitContextType;

import org.eclipse.cdt.internal.ui.text.c.hover.SourceViewerInformationControl;
import org.eclipse.cdt.internal.ui.text.contentassist.RelevanceConstants;

public class TemplateEngine {

	private static final String $_LINE_SELECTION= "${" + GlobalTemplateVariables.LineSelection.NAME + '}'; //$NON-NLS-1$
	private static final String $_WORD_SELECTION= "${" + GlobalTemplateVariables.WordSelection.NAME + '}'; //$NON-NLS-1$

	/** The context type. */
	private final TemplateContextType fContextType;	
	/** The result proposals. */
	private final ArrayList<ICompletionProposal> fProposals= new ArrayList<ICompletionProposal>();
	/** Positions created on the key documents to remove in reset. */
	private final Map<IDocument, Position> fPositions= new HashMap<IDocument, Position>();
	/** Pattern to match the start of a line content */
	private final Pattern fStartOfLineContentPattern = Pattern.compile("[^ \t]"); //$NON-NLS-1$

	public static class CTemplateProposal extends TemplateProposal implements ICCompletionProposal {
		
		@Override
		public IInformationControlCreator getInformationControlCreator() {
			return new IInformationControlCreator() {
				@Override
				public IInformationControl createInformationControl(Shell parent) {
					int orientation = SWT.LEFT_TO_RIGHT;
					return new SourceViewerInformationControl(parent, false, orientation, null);
				}
			};
		}

		public CTemplateProposal(Template template, TemplateContext context, IRegion region, Image image) {
			super(template, context, region, image, RelevanceConstants.CASE_MATCH_RELEVANCE + RelevanceConstants.TEMPLATE_TYPE_RELEVANCE);
		}

        @Override
		public String getIdString() {
            return getDisplayString();
        }
        
	}
	/**
	 * Creates the template engine for a particular context type.
	 * See <code>TemplateContext</code> for supported context types.
	 */
	public TemplateEngine(TemplateContextType contextType) {
		Assert.isNotNull(contextType);
		fContextType= contextType;
	}

	/**
	 * Empties the collector.
	 */
	public void reset() {
		fProposals.clear();
		for (Entry<IDocument, Position> entry2 : fPositions.entrySet()) {
			Entry<IDocument, Position> entry= entry2;
			IDocument doc= entry.getKey();
			Position position= entry.getValue();
			doc.removePosition(position);
		}
		fPositions.clear();
	}

	/**
	 * Returns the array of matching templates.
	 */
	public List<ICompletionProposal> getResults() {
		return fProposals;
	}

	/**
	 * Inspects the context of the compilation unit around <code>completionPosition</code>
	 * and feeds the collector with proposals.
	 * @param viewer the text viewer
	 * @param completionPosition the context position in the document of the text viewer
	 * @param translationUnit the translation unit (may be <code>null</code>)
	 */
	public void complete(ITextViewer viewer, int completionPosition, ITranslationUnit translationUnit) {
		if (!(fContextType instanceof TranslationUnitContextType))
			return;

	    IDocument document= viewer.getDocument();
		Point selection= viewer.getSelectedRange();
		boolean linesSelected= areLinesSelected(viewer);
		boolean showLineSelectionTemplates = linesSelected;
		boolean showWordSelectionTemplates = !linesSelected || isOnlyWordOnLine(viewer);
		if (linesSelected) {
			// adjust line selection to start at column 1 and end at line delimiter
			try {
				IRegion startLine = document.getLineInformationOfOffset(selection.x);
				IRegion endLine = document.getLineInformationOfOffset(selection.x + selection.y - 1);
				completionPosition= selection.x= startLine.getOffset();
				selection.y= endLine.getOffset() + endLine.getLength() - startLine.getOffset();
			} catch (BadLocationException exc) {
			}
		}
		Position position= new Position(completionPosition, selection.y);

		// remember selected text
		String selectedText= null;
		if (selection.y != 0) {
			try {
				selectedText= document.get(selection.x, selection.y);
				document.addPosition(position);
				fPositions.put(document, position);
			} catch (BadLocationException e) {}
		}

		TranslationUnitContext context= ((TranslationUnitContextType) fContextType).createContext(document, position, translationUnit);
		context.setVariable("selection", selectedText); //$NON-NLS-1$
		int start= context.getStart();
		int end= context.getEnd();
		IRegion region= new Region(start, end - start);

		Template[] templates= CUIPlugin.getDefault().getTemplateStore().getTemplates();

		Image image= CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_TEMPLATE);
		if (selection.y == 0) {
			for (int i= 0; i != templates.length; i++)
				if (context.canEvaluate(templates[i]))
					fProposals.add(new CTemplateProposal(templates[i], context, region, image));

		} else {

			if (linesSelected || context.getKey().length() == 0)
				context.setForceEvaluation(true);

			for (int i= 0; i != templates.length; i++) {
				Template template= templates[i];
				if (context.canEvaluate(template) &&
					template.getContextTypeId().equals(context.getContextType().getId()) &&
					((showWordSelectionTemplates && template.getPattern().indexOf($_WORD_SELECTION)	!= -1 || 
					 (showLineSelectionTemplates &&	template.getPattern().indexOf($_LINE_SELECTION) != -1))))
				{
					fProposals.add(new CTemplateProposal(templates[i], context, region, image));
				}
			}
		}

	}

	/**
	 * Returns <code>true</code> if one line is completely selected or if multiple lines are selected. Being
	 * completely selected means that all characters are selected except the new line characters and
	 * leading/trailing spaces.
	 * 
	 * @return <code>true</code> if one or multiple lines are selected
	 */
	private boolean areLinesSelected(ITextViewer viewer) {
		if (viewer == null)
			return false;

		Point s= viewer.getSelectedRange();
		if (s.y == 0)
			return false;

		try {

			IDocument document= viewer.getDocument();
			int startLine= document.getLineOfOffset(s.x);
			IRegion line= document.getLineInformation(startLine);
			
			String lineContent = document.get(line.getOffset(), line.getLength());
			Matcher m = fStartOfLineContentPattern.matcher(lineContent);
			int lineContentStart = 0;
			if(m.find())
				lineContentStart = m.start() + line.getOffset();
			int lineContentLength = lineContent.trim().length();
			
			return s.x <= lineContentStart && s.x + s.y >= lineContentStart + lineContentLength;

		} catch (BadLocationException x) {
			return false;
		}
	}
	
	/**
	 * Returns <code>true</code> if there's only one word on the line
	 * 
	 * @return <code>true</code> if only one word is on the line
	 */
	private boolean isOnlyWordOnLine(ITextViewer viewer) {
		if (viewer == null)
			return false;

		Point s= viewer.getSelectedRange();
		if (s.y == 0)
			return false;

		try {

			IDocument document= viewer.getDocument();
			int startLine= document.getLineOfOffset(s.x);
			IRegion line= document.getLineInformation(startLine);
			
			String lineContent = document.get(line.getOffset(), line.getLength());
			
			return lineContent.trim().lastIndexOf(' ', s.x) == -1;

		} catch (BadLocationException x) {
			return false;
		}
	}
}

