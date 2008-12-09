/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.IProposalFilter;

import org.eclipse.cdt.internal.ui.preferences.ProposalFilterPreferencesUtil;
import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.text.CParameterListValidator;
import org.eclipse.cdt.internal.ui.text.Symbols;

/**
 * C/C++ content assist processor.
 */
public class CContentAssistProcessor extends ContentAssistProcessor {

	/**
	 * A wrapper for {@link ICompetionProposal}s.
	 */
	private static class CCompletionProposalWrapper implements ICCompletionProposal {

		private ICompletionProposal fWrappedProposal;

		public CCompletionProposalWrapper(ICompletionProposal proposal) {
			fWrappedProposal= proposal;
		}

		/*
		 * @see org.eclipse.cdt.ui.text.ICCompletionProposal#getIdString()
		 */
		public String getIdString() {
			return fWrappedProposal.getDisplayString();
		}

		/*
		 * @see org.eclipse.cdt.ui.text.ICCompletionProposal#getRelevance()
		 */
		public int getRelevance() {
			return RelevanceConstants.CASE_MATCH_RELEVANCE + RelevanceConstants.KEYWORD_TYPE_RELEVANCE;
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
		 */
		public void apply(IDocument document) {
			throw new UnsupportedOperationException();
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
		 */
		public String getAdditionalProposalInfo() {
			return fWrappedProposal.getAdditionalProposalInfo();
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
		 */
		public IContextInformation getContextInformation() {
			return fWrappedProposal.getContextInformation();
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
		 */
		public String getDisplayString() {
			return fWrappedProposal.getDisplayString();
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
		 */
		public Image getImage() {
			return fWrappedProposal.getImage();
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
		 */
		public Point getSelection(IDocument document) {
			return fWrappedProposal.getSelection(document);
		}

		/**
		 * @return the original proposal
		 */
		public ICompletionProposal unwrap() {
			return fWrappedProposal;
		}

	}


	private IContextInformationValidator fValidator;
	private final IEditorPart fEditor;

	public CContentAssistProcessor(IEditorPart editor, ContentAssistant assistant, String partition) {
		super(assistant, partition);
		fEditor= editor;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	@Override
	public IContextInformationValidator getContextInformationValidator() {
		if (fValidator == null) {
			fValidator= new CParameterListValidator();
		}
		return fValidator;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistProcessor#filterAndSort(java.util.List, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected List<ICompletionProposal> filterAndSortProposals(List<ICompletionProposal> proposals, IProgressMonitor monitor, ContentAssistInvocationContext context) {
		IProposalFilter filter = getCompletionFilter();
		ICCompletionProposal[] proposalsInput= new ICCompletionProposal[proposals.size()];
		// wrap proposals which are no ICCompletionProposals
		boolean wrapped= false;
		int i=0;
		for (ICompletionProposal proposal : proposals) {
			if (proposal instanceof ICCompletionProposal) {
				proposalsInput[i++]= (ICCompletionProposal)proposal;
			} else {
				wrapped= true;
				proposalsInput[i++]= new CCompletionProposalWrapper(proposal);
			}
		}
		// filter
		ICCompletionProposal[] proposalsFiltered = filter.filterProposals(proposalsInput);

		// sort
		boolean sortByAlphabet= CUIPlugin.getDefault().getPreferenceStore().getBoolean(ContentAssistPreference.ORDER_PROPOSALS);
		if (sortByAlphabet) {
			// already sorted alphabetically by DefaultProposalFilter
			// in case of custom proposal filter, keep ordering applied by filter
		} else {
			// sort by relevance
			CCompletionProposalComparator propsComp= new CCompletionProposalComparator();
			propsComp.setOrderAlphabetically(sortByAlphabet);
			Arrays.sort(proposalsFiltered, propsComp);
		}
		List<ICompletionProposal> filteredList;
		if (wrapped) {
			// unwrap again
			filteredList= new ArrayList<ICompletionProposal>(proposalsFiltered.length);
			for (ICCompletionProposal proposal : proposalsFiltered) {
				if (proposal instanceof CCompletionProposalWrapper) {
					filteredList.add(((CCompletionProposalWrapper)proposal).unwrap());
				} else {
					filteredList.add(proposal);
				}
			}
		} else {
			final ICompletionProposal[] tmp= proposalsFiltered;
			filteredList= Arrays.asList(tmp);
		}
		return filteredList;
	}

	private IProposalFilter getCompletionFilter() {
		IProposalFilter filter = null;
		try {
			IConfigurationElement filterElement = ProposalFilterPreferencesUtil.getPreferredFilterElement();
			if (null != filterElement) {
				Object contribObject = filterElement
						.createExecutableExtension("class"); //$NON-NLS-1$
				if ((contribObject instanceof IProposalFilter)) {
					filter = (IProposalFilter) contribObject;
				}
			}
		} catch (InvalidRegistryObjectException e) {
			// No action required since we will be using the fail-safe default filter
			CUIPlugin.log(e);
		} catch (CoreException e) {
			// No action required since we will be using the fail-safe default filter
			CUIPlugin.log(e);
		}

		if (null == filter) {
			// fail-safe default implementation
			filter = new DefaultProposalFilter();
		}
		return filter;
	}
	
	@Override
	protected List<IContextInformation> filterAndSortContextInformation(List<IContextInformation> contexts,
			IProgressMonitor monitor) {
		return contexts;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistProcessor#createContext(org.eclipse.jface.text.ITextViewer, int)
	 */
	@Override
	protected ContentAssistInvocationContext createContext(ITextViewer viewer, int offset, boolean isCompletion) {
		return new CContentAssistInvocationContext(viewer, offset, fEditor, isCompletion, isAutoActivated());
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistProcessor#verifyAutoActivation(org.eclipse.jface.text.ITextViewer, int)
	 */
	@Override
	protected boolean verifyAutoActivation(ITextViewer viewer, int offset) {
		IDocument doc= viewer.getDocument();
		if (doc == null) {
			return false;
		}
		if (offset <= 0) {
			return false;
		}
		try {
			char activationChar= doc.getChar(--offset);
			switch (activationChar) {
			case ':':
				return offset > 0 && doc.getChar(--offset) == ':';
			case '>':
				return offset > 0 && doc.getChar(--offset) == '-';
			case '.':
				// avoid completion of float literals
				CHeuristicScanner scanner= new CHeuristicScanner(doc);
				int token= scanner.previousToken(--offset, Math.max(0, offset - 200));
				// the scanner reports numbers as identifiers
				if (token == Symbols.TokenIDENT && !Character.isJavaIdentifierStart(doc.getChar(scanner.getPosition() + 1))) {
					// not a valid identifier
					return false;
				}
				return true;
			}
		} catch (BadLocationException exc) {
		}
		return false;
	}

}
