/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.template;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.contentassist.ICompletionContributor;

import org.eclipse.cdt.internal.corext.template.c.CContextType;
import org.eclipse.cdt.internal.corext.template.c.CommentContextType;

/**
 * A completion processor for templates.
 * Can be used directly as implementation of {@link IContentAssistProcessor} or
 * as implementation of the extension point interface {@link ICompletionContributor}.
 *
 * @since 4.0
 */
public class CTemplateCompletionProcessor implements IContentAssistProcessor, ICompletionContributor {

	private static final ICompletionProposal[] NO_PROPOSALS= new ICompletionProposal[0];
	private static final IContextInformation[] NO_CONTEXTS= new IContextInformation[0];

	private IEditorPart fEditor;
	
	private final TemplateEngine fCTemplateEngine;
	private final TemplateEngine fCommentTemplateEngine;

	/**
	 * Create a new template completion processor to be used as <code>IContentAssistProcessor</code>.
	 * 
	 * @param editor  the editor, may not be <code>null</code>
	 */
	public CTemplateCompletionProcessor(IEditorPart editor) {
		this();
		Assert.isNotNull(editor);
		fEditor= editor;
	}

	/**
	 * Default constructor is required (executable extension).
	 */
	public CTemplateCompletionProcessor() {
		TemplateContextType contextType= CUIPlugin.getDefault().getTemplateContextRegistry().getContextType(CContextType.ID);
		if (contextType == null) {
			contextType= new CContextType();
			CUIPlugin.getDefault().getTemplateContextRegistry().addContextType(contextType);
		}
		if (contextType != null)
			fCTemplateEngine= new TemplateEngine(contextType);
		else
			fCTemplateEngine= null;
		contextType= CUIPlugin.getDefault().getTemplateContextRegistry().getContextType(CommentContextType.ID);
		if (contextType == null) {
			contextType= new CommentContextType();
			CUIPlugin.getDefault().getTemplateContextRegistry().addContextType(contextType);
		}
		if (contextType != null)
			fCommentTemplateEngine= new TemplateEngine(contextType);
		else
			fCommentTemplateEngine= null;
	}
	
	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		Assert.isNotNull(fEditor);
		TemplateEngine engine;
		try {
			String partition= TextUtilities.getContentType(viewer.getDocument(), ICPartitions.C_PARTITIONING, offset, true);
			if (partition.equals(ICPartitions.C_MULTI_LINE_COMMENT))
				engine= fCommentTemplateEngine;
			else
				engine= fCTemplateEngine;
		} catch (BadLocationException x) {
			return NO_PROPOSALS;
		}
		
		if (engine != null) {
			IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
			if (workingCopy == null)
				return NO_PROPOSALS;
			
			engine.reset();
			engine.complete(viewer, offset, workingCopy);

			List result= engine.getResults();

			return (ICompletionProposal[]) result.toArray(new ICompletionProposal[result.size()]);
		}
		return NO_PROPOSALS;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return NO_CONTEXTS;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionContributor#contributeCompletionProposals(org.eclipse.jface.text.ITextViewer, int, org.eclipse.cdt.core.model.IWorkingCopy, org.eclipse.cdt.core.dom.ast.ASTCompletionNode, java.lang.String, java.util.List)
	 */
	public void contributeCompletionProposals(ITextViewer viewer, int offset,
			IWorkingCopy workingCopy, ASTCompletionNode completionNode,
			String prefix, List proposals) {
		// TODO We should use the completion node to determine the proper context for the templates
		// For now we just keep the current behavior
		TemplateEngine engine;
		try {
			String partition= TextUtilities.getContentType(viewer.getDocument(), ICPartitions.C_PARTITIONING, offset, true);
			if (partition.equals(ICPartitions.C_MULTI_LINE_COMMENT))
				engine= fCommentTemplateEngine;
			else
				engine= fCTemplateEngine;
		} catch (BadLocationException x) {
			return;
		}
		
		if (engine != null) {
			engine.reset();
			engine.complete(viewer, offset, workingCopy);

			List result= engine.getResults();
			proposals.addAll(result);
		}
	}

}
