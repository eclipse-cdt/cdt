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
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling.engine;

import java.util.Set;

/**
 * Event fired by spell checkers.
 */
public interface ISpellEvent {

	/**
	 * Returns the begin index of the incorrectly spelled word.
	 *
	 * @return The begin index of the word
	 */
	public int getBegin();

	/**
	 * Returns the end index of the incorrectly spelled word.
	 *
	 * @return The end index of the word
	 */
	public int getEnd();

	/**
	 * Returns the proposals for the incorrectly spelled word.
	 *
	 * @return Array of proposals for the word
	 */
	public Set<RankedWordProposal> getProposals();

	/**
	 * Returns the incorrectly spelled word.
	 *
	 * @return The incorrect word
	 */
	public String getWord();

	/**
	 * Was the incorrectly spelled word found in the dictionary?
	 *
	 * @return <code>true</code> iff the word was found, <code>false</code> otherwise
	 */
	public boolean isMatch();

	/**
	 * Does the incorrectly spelled word start a new sentence?
	 *
	 * @return <code>true<code> iff the word starts a new sentence, <code>false</code> otherwise
	 */
	public boolean isStart();
}
