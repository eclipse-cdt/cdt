/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Bryan Wilkinson (QNX)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationExtension;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;

import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.text.Symbols;

/**
 * A proposal computer wrapping the legacy {@link CCompletionProcessor2}.
 *
 * @since 4.0
 */
public class LegacyCompletionProposalComputer implements ICompletionProposalComputer {

	private static final class ContextInformationWrapper implements IContextInformation, IContextInformationExtension {

		private final IContextInformation fContextInformation;
		private int fPosition;

		public ContextInformationWrapper(IContextInformation contextInformation) {
			fContextInformation= contextInformation;
		}

		/*
		 * @see IContextInformation#getContextDisplayString()
		 */
		public String getContextDisplayString() {
			return fContextInformation.getContextDisplayString();
		}

			/*
		 * @see IContextInformation#getImage()
		 */
		public Image getImage() {
			return fContextInformation.getImage();
		}

		/*
		 * @see IContextInformation#getInformationDisplayString()
		 */
		public String getInformationDisplayString() {
			return fContextInformation.getInformationDisplayString();
		}

		/*
		 * @see IContextInformationExtension#getContextInformationPosition()
		 */
		public int getContextInformationPosition() {
			return fPosition;
		}

		public void setContextInformationPosition(int position) {
			fPosition= position;
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.IContextInformation#equals(java.lang.Object)
		 */
		public boolean equals(Object object) {
			if (object instanceof ContextInformationWrapper)
				return fContextInformation.equals(((ContextInformationWrapper) object).fContextInformation);
			else
				return fContextInformation.equals(object);
		}
	}

	private CCompletionProcessor2 fCompletionProcessor;

	/**
	 * Default constructor is required (executable extension).
	 */
	public LegacyCompletionProposalComputer() {
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#computeCompletionProposals(org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		if (context instanceof CContentAssistInvocationContext) {
			CContentAssistInvocationContext cContext= (CContentAssistInvocationContext)context;
			return internalComputeCompletionProposals(context.getInvocationOffset(), cContext, monitor);
		}
		return Collections.EMPTY_LIST;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#computeContextInformation(org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public List computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		if (context instanceof CContentAssistInvocationContext) {
			CContentAssistInvocationContext cContext= (CContentAssistInvocationContext)context;
			int contextInformationPosition= guessContextInformationPosition(cContext);
			if (contextInformationPosition >= 0) {
				List result= addContextInformations(cContext, contextInformationPosition, monitor);
				return result;
			}
		}
		return Collections.EMPTY_LIST;
	}

	protected int guessContextInformationPosition(ContentAssistInvocationContext context) {
		final int contextPosition= context.getInvocationOffset();
		
		IDocument document= context.getDocument();
		CHeuristicScanner scanner= new CHeuristicScanner(document);
		int bound= Math.max(-1, contextPosition - 200);
		
		// try the innermost scope of parentheses that looks like a method call
		int pos= contextPosition - 1;
		do {
			int paren= scanner.findOpeningPeer(pos, bound, '(', ')');
			if (paren == CHeuristicScanner.NOT_FOUND)
				break;
			paren= scanner.findNonWhitespaceBackward(paren - 1, bound);
			if (paren == CHeuristicScanner.NOT_FOUND) {
				break;
			}
			int token= scanner.previousToken(paren, bound);
			// next token must be a method name (identifier) or the closing angle of a
			// constructor call of a template type.
			if (token == Symbols.TokenIDENT || token == Symbols.TokenGREATERTHAN) {
				return paren + 1;
			}
			pos= paren;
		} while (true);
		
		return context.getInvocationOffset();
	}

	private List addContextInformations(CContentAssistInvocationContext context, int offset, IProgressMonitor monitor) {
		List proposals= internalComputeCompletionProposals(offset, context, monitor);
		List result= new ArrayList(proposals.size());

		for (Iterator it= proposals.iterator(); it.hasNext();) {
			ICompletionProposal proposal= (ICompletionProposal) it.next();
			IContextInformation contextInformation= proposal.getContextInformation();
			if (contextInformation != null) {
				ContextInformationWrapper wrapper= new ContextInformationWrapper(contextInformation);
				wrapper.setContextInformationPosition(offset);
				result.add(wrapper);
			}
		}
		return result;
	}

	private List internalComputeCompletionProposals(int offset, CContentAssistInvocationContext context, IProgressMonitor monitor) {
		IEditorPart editor= context.getEditor();
		if (editor != null) {
			fCompletionProcessor= new CCompletionProcessor2(editor);
			ICompletionProposal[] proposals= fCompletionProcessor.computeCompletionProposals(context.getViewer(), offset);
			if (proposals != null) {
				return Arrays.asList(proposals);
			}
		}
		return Collections.EMPTY_LIST;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#getErrorMessage()
	 */
	public String getErrorMessage() {
		if (fCompletionProcessor != null) {
			return fCompletionProcessor.getErrorMessage();
		}
		return null;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#sessionEnded()
	 */
	public void sessionEnded() {
		fCompletionProcessor= null;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#sessionStarted()
	 */
	public void sessionStarted() {
	}

}
