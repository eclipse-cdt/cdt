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
 * Interface of dictionaries to use for spell checking.
 */
public interface ISpellDictionary {
	/**
	 * Returns whether this dictionary accepts new words.
	 *
	 * @return <code>true</code> if this dictionary accepts new words, <code>false</code> otherwise
	 */
	public boolean acceptsWords();

	/**
	 * Externalizes the specified word.
	 *
	 * @param word       The word to externalize in the dictionary
	 */
	public void addWord(String word);

	/**
	 * Returns the ranked word proposals for an incorrectly spelled word.
	 *
	 * @param word       The word to retrieve the proposals for
	 * @param sentence   <code>true</code> iff the proposals start a new sentence,
	 *                   <code>false</code> otherwise
	 * @return Array of ranked word proposals
	 */
	public Set<RankedWordProposal> getProposals(String word, boolean sentence);

	/**
	 * Is the specified word correctly spelled?
	 *
	 * @param word the word to spell check
	 * @return <code>true</code> iff this word is correctly spelled, <code>false</code> otherwise
	 */
	public boolean isCorrect(String word);

	/**
	 * Is the dictionary loaded?
	 *
	 * @return <code>true</code> iff it is loaded, <code>false</code> otherwise
	 */
	public boolean isLoaded();

	/**
	 * Empties the dictionary.
	 */
	public void unload();

	/**
	 * Tells whether to strip non-letters from word boundaries.
	 *
	 * @param state <code>true</code> if non-letters should be stripped
	 */
	public void setStripNonLetters(boolean state);
}
