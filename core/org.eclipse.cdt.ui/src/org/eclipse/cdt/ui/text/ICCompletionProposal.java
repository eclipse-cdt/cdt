/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * <p>
 * This interface can be implemented by clients.
 * </p>
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
