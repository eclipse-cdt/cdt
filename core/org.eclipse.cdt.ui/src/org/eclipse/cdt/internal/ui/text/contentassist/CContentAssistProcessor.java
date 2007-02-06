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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
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

	private IContextInformationValidator fValidator;
	private final IEditorPart fEditor;

	public CContentAssistProcessor(IEditorPart editor, ContentAssistant assistant, String partition) {
		super(assistant, partition);
		fEditor= editor;
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
		ICCompletionProposal[] proposalsInput = (ICCompletionProposal[]) proposals.toArray(new ICCompletionProposal[proposals.size()]) ;
		ICCompletionProposal[] proposalsFiltered = filter.filterProposals(proposalsInput);
		
		return Arrays.asList(proposalsFiltered);
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

}
