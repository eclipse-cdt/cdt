/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;

import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;

/**
 * CompletionProposalComputer based on a specified set of GenericTag objects.
 * @since 5.0
 */
public class GenericTagCompletionProposalComputer implements ICompletionProposalComputer {
	protected GenericDocTag[] tags;
	protected char[] tagMarkers;
	
	/**
	 * Constructs a proposal computer for the specified tags
	 * @param tags
	 */
	public GenericTagCompletionProposalComputer(GenericDocTag[] tags, char[] tagMarkers) {
		this.tags= tags;
		this.tagMarkers= tagMarkers;
	}
	
	/**
	 * @param c the character to test
	 * @return whether the specified character is a tag prefix marker 
	 */
	protected boolean isTagMarker(char c) {
		for(char candidate : tagMarkers)
			if(c == candidate)
				return true;
		return false;
	}
	
	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#computeCompletionProposals(org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		IDocument doc= context.getDocument();
		int ivcOffset= context.getInvocationOffset();
		try {
			ITypedRegion tr= TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, ivcOffset, false);
			int firstNonWS= ivcOffset;
			while(firstNonWS-1> tr.getOffset() && !Character.isWhitespace(doc.get(firstNonWS-1, 1).charAt(0)))
				firstNonWS--;
			String prefix= doc.get(firstNonWS, ivcOffset-firstNonWS);
			if(prefix.length()>0 && isTagMarker(prefix.charAt(0))) {
				List<ICCompletionProposal> proposals= new ArrayList<ICCompletionProposal>();
				char tagMarker= prefix.charAt(0);
				for(int i=0; i<tags.length; i++) {
					String tag= tags[i].getTagName();
					if(tag.toLowerCase().startsWith(prefix.substring(1).toLowerCase())) {						
						CCompletionProposal proposal= new CCompletionProposal(tagMarker+tag, ivcOffset-prefix.length(), prefix.length(), null, tagMarker+tag, 1, context.getViewer()); 
						String description= tags[i].getTagDescription();
						if(description!=null && description.length()>0) {
							proposal.setAdditionalProposalInfo(description);
						}
						proposals.add(proposal);
					}
				}
				return proposals;
			}
		} catch(BadLocationException ble) {
			// offset is zero, ignore
		}
		return new ArrayList();
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
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#sessionEnded()
	 */
	public void sessionEnded() {}
	
	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#sessionStarted()
	 */
	public void sessionStarted() {}
}
