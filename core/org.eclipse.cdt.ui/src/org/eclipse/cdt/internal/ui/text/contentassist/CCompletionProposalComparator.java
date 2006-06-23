/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;


import java.util.Comparator;

import org.eclipse.cdt.ui.text.ICCompletionProposal;

public class CCompletionProposalComparator implements Comparator {

	private boolean fOrderAlphabetically;

	/**
	 * Constructor for CompletionProposalComparator.
	 */
	public CCompletionProposalComparator() {
		fOrderAlphabetically= false;
	}
	
	public void setOrderAlphabetically(boolean orderAlphabetically) {
		fOrderAlphabetically= orderAlphabetically;
	}
	
	/* (non-Javadoc)
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2) {
		ICCompletionProposal c1= (ICCompletionProposal) o1;
		ICCompletionProposal c2= (ICCompletionProposal) o2;
        
		if (!fOrderAlphabetically) {
			int relevanceDif= c2.getRelevance() - c1.getRelevance();
			if (relevanceDif != 0) {
				return relevanceDif;
			}
		}
        
        String id1 = c1.getIdString();
        String id2 = c2.getIdString();
        
		return id1.compareToIgnoreCase(id2);
	}	
	
}


