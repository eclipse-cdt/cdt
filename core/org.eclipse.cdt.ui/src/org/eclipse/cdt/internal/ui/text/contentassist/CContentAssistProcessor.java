/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import java.util.Iterator;
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
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.IProposalFilter;

import org.eclipse.cdt.internal.ui.preferences.ProposalFilterPreferencesUtil;
import org.eclipse.cdt.internal.ui.text.CParameterListValidator;

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
			return -1;
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


	private static final int IDX_AFTERDASH = 0;
	private static final int IDX_AFTERCOLON = 1;
	private static final int IDX_AFTEROTHER = 2;
	private static final int IDX_ALL = 3;
	


	private IContextInformationValidator fValidator;
	private final IEditorPart fEditor;
	private char[][] fCompletionAutoActivationCharacters;
	private ISourceViewer fViewer;

	public CContentAssistProcessor(IEditorPart editor, ISourceViewer viewer, ContentAssistant assistant, String partition) {
		super(assistant, partition);
		fEditor= editor;
		fViewer= viewer;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		if (fValidator == null) {
			fValidator= new CParameterListValidator();
		}
		return fValidator;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistProcessor#filterAndSort(java.util.List, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected List filterAndSortProposals(List proposals, IProgressMonitor monitor, ContentAssistInvocationContext context) {
		IProposalFilter filter = getCompletionFilter();
		ICCompletionProposal[] proposalsInput= new ICCompletionProposal[proposals.size()];
		// wrap proposals which are no ICCompletionProposals
		int i=0;
		for (Iterator iterator = proposals.iterator(); iterator.hasNext(); ) {
			ICompletionProposal proposal= (ICompletionProposal) iterator.next();
			if (proposal instanceof ICCompletionProposal) {
				proposalsInput[i++]= (ICCompletionProposal)proposal;
			} else {
				proposalsInput[i++]= new CCompletionProposalWrapper(proposal);
			}
		}
		ICCompletionProposal[] proposalsFiltered = filter.filterProposals(proposalsInput);
		// unwrap again
		ArrayList filteredList= new ArrayList(proposalsFiltered.length);
		for (int j= 0; j < proposalsFiltered.length; j++) {
			ICCompletionProposal proposal= proposalsFiltered[j];
			if (proposal instanceof CCompletionProposalWrapper) {
				filteredList.add(((CCompletionProposalWrapper)proposal).unwrap());
			} else {
				filteredList.add(proposal);
			}
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
			CUIPlugin.getDefault().log(e);
		} catch (CoreException e) {
			// No action required since we will be using the fail-safe default filter
			CUIPlugin.getDefault().log(e);
		}

		if (null == filter) {
			// fail-safe default implementation
			filter = new DefaultProposalFilter();
		}
		return filter;
	}
	
	protected List filterAndSortContextInformation(List contexts,
			IProgressMonitor monitor) {
		return contexts;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistProcessor#createContext(org.eclipse.jface.text.ITextViewer, int)
	 */
	protected ContentAssistInvocationContext createContext(ITextViewer viewer, int offset, boolean isCompletion) {
		return new CContentAssistInvocationContext(viewer, offset, fEditor, isCompletion);
	}
	
	public void setCompletionProposalAutoActivationCharacters(char[] activationSet) {
		if (activationSet == null) {
			fCompletionAutoActivationCharacters= null;
		}
		else {
			final int len= activationSet.length;
			StringBuffer afterDash= new StringBuffer(len);
			StringBuffer afterColon= new StringBuffer(len);
			StringBuffer afterOther= new StringBuffer(len);
			for (int i = 0; i < activationSet.length; i++) {
				final char c = activationSet[i];
				switch(c) {
				case ':':
					afterColon.append(c);
					break;
				case '>':
					afterDash.append(c);
					break;
				default:
					afterDash.append(c);
				afterColon.append(c);
				afterOther.append(c);
				break;
				}
			}
			fCompletionAutoActivationCharacters= new char[4][];
			fCompletionAutoActivationCharacters[IDX_AFTERDASH]= afterDash.toString().toCharArray();
			fCompletionAutoActivationCharacters[IDX_AFTERCOLON]= afterColon.toString().toCharArray();
			fCompletionAutoActivationCharacters[IDX_AFTEROTHER]= afterOther.toString().toCharArray();
			fCompletionAutoActivationCharacters[IDX_ALL]= activationSet;
		}
	}


	public char[] getCompletionProposalAutoActivationCharacters() {
		if (fCompletionAutoActivationCharacters == null) {
			return null;
		}
		
		if (fViewer != null) {
			char prevChar= 0;
			try {
				final IDocument doc= fViewer.getDocument();
				if (doc != null) {
					prevChar= doc.getChar(fViewer.getSelectedRange().x-1);
				}
			} 
			catch (BadLocationException e) {
				// beginning of document.
			}
			switch (prevChar) {
				case ':':
					return fCompletionAutoActivationCharacters[IDX_AFTERCOLON];
				case '-':
					return fCompletionAutoActivationCharacters[IDX_AFTERDASH];
				default:
					return fCompletionAutoActivationCharacters[IDX_AFTEROTHER];
			}
		}
		return fCompletionAutoActivationCharacters[IDX_ALL];
	}

}
