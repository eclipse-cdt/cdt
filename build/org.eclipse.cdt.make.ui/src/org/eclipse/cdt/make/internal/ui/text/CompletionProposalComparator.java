package org.eclipse.cdt.make.internal.ui.text;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.Comparator;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class CompletionProposalComparator implements Comparator {

	/**
	 * Constructor for CompletionProposalComparator.
	 */
	public CompletionProposalComparator() {
	}
	
	/* (non-Javadoc)
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2) {
		ICompletionProposal c1= (ICompletionProposal) o1;
		ICompletionProposal c2= (ICompletionProposal) o2;
		return c1.getDisplayString().compareToIgnoreCase(c2.getDisplayString());
	}	
	
}
