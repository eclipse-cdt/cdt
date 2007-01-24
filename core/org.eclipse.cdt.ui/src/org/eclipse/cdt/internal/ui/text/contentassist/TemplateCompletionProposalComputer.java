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

package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.templates.TemplateContextType;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;

import org.eclipse.cdt.internal.corext.template.c.CContextType;
import org.eclipse.cdt.internal.corext.template.c.CommentContextType;

import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.text.template.TemplateEngine;

/**
 * A completion proposal computer for templates.
 *
 * @since 4.0
 */
public class TemplateCompletionProposalComputer implements ICompletionProposalComputer {

	private final TemplateEngine fCTemplateEngine;
	private final TemplateEngine fCommentTemplateEngine;

	/**
	 * Default constructor is required (executable extension).
	 */
	public TemplateCompletionProposalComputer() {
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
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#computeCompletionProposals(org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		ITextViewer viewer= context.getViewer();
		int offset= context.getInvocationOffset();
		TemplateEngine engine= null;
		try {
			String partition= TextUtilities.getContentType(viewer.getDocument(), ICPartitions.C_PARTITIONING, offset, true);
			if (partition.equals(ICPartitions.C_MULTI_LINE_COMMENT)) {
				engine= fCommentTemplateEngine;
			} else {
				if (isValidContext(context)) {
					engine= fCTemplateEngine;
				}
			}
		} catch (BadLocationException x) {
			return Collections.EMPTY_LIST;
		}
		
		if (engine != null && context instanceof CContentAssistInvocationContext) {
			CContentAssistInvocationContext cContext= (CContentAssistInvocationContext)context;
			ITranslationUnit tUnit = cContext.getTranslationUnit();
			if (tUnit == null) {
				return Collections.EMPTY_LIST;
			}
			engine.reset();
			engine.complete(viewer, offset, tUnit);

			List result= engine.getResults();

			return result;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * Checks whether the given invocation context looks valid for template completion.
	 * 
	 * @param context  the content assist invocation context
	 * @return <code>false</code> if the given invocation context looks like a field reference
	 */
	private boolean isValidContext(ContentAssistInvocationContext context) {
		CHeuristicScanner scanner= new CHeuristicScanner(context.getDocument());
		int start= context.getInvocationOffset();
		return !scanner.looksLikeFieldReferenceBackward(start, Math.max(0, start-100));
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#computeContextInformation(org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public List computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		return Collections.EMPTY_LIST;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#sessionStarted()
	 */
	public void sessionStarted() {
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#sessionEnded()
	 */
	public void sessionEnded() {
		if (fCommentTemplateEngine != null) {
			fCommentTemplateEngine.reset();
		}
		if (fCTemplateEngine != null) {
			fCTemplateEngine.reset();
		}
	}

}
