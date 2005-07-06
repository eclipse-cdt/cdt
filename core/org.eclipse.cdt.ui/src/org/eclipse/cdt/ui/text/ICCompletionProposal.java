/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.ui.text;

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

    /**
     * Returns an id string that uniquely identifies this proposal. For most things this is the
     * same as the display name. For functions, this strips off the parameter names and the
     * return type.
     * 
     * @return the string that uniquely identifies this proposal
     */
    String getIdString();
}

