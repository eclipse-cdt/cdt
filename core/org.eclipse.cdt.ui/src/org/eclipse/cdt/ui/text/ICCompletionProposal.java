package org.eclipse.cdt.ui.text;

/*******************************************************************************
 * Copyright (c) 2000, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/


import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * CompletionProposal with a relevance value.
 * The relevance value is used to sort the completion proposals. Proposals with higher relevance
 * should be listed before proposals with lower relevance.
 */
public interface ICCompletionProposal extends ICompletionProposal {
	
	/**
	 * Returns the relevance of the proposal.
	 */
	int getRelevance();

}

