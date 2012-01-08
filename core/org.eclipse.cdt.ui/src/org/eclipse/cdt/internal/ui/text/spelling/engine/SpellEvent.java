/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling.engine;

import java.util.Set;

/**
 * Spell event fired for words detected by a spell check iterator.
 */
public class SpellEvent implements ISpellEvent {
	/** The begin index of the word in the spell checkable medium */
	private final int fBegin;

	/** The spell checker that causes the event */
	private final ISpellChecker fChecker;

	/** The end index of the word in the spell checkable medium */
	private final int fEnd;

	/** Was the word found in the dictionary? */
	private final boolean fMatch;

	/** Does the word start a new sentence? */
	private final boolean fSentence;

	/** The word that causes the spell event */
	private final String fWord;

	/**
	 * Creates a new spell event.
	 *
	 * @param checker    The spell checker that causes the event
	 * @param word       The word that causes the event
	 * @param begin      The begin index of the word in the spell checkable medium
	 * @param end        The end index of the word in the spell checkable medium
	 * @param sentence   <code>true</code> iff the word starts a new sentence,
	 *                   <code>false</code> otherwise
	 * @param match      <code>true</code> iff the word was found in the dictionary,
	 *                   <code>false</code> otherwise
	 */
	protected SpellEvent(final ISpellChecker checker, final String word, final int begin, final int end, final boolean sentence, final boolean match) {
		fChecker= checker;
		fEnd= end;
		fBegin= begin;
		fWord= word;
		fSentence= sentence;
		fMatch= match;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellEvent#getBegin()
	 */
	@Override
	public final int getBegin() {
		return fBegin;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellEvent#getEnd()
	 */
	@Override
	public final int getEnd() {
		return fEnd;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellEvent#getProposals()
	 */
	@Override
	public final Set<RankedWordProposal> getProposals() {
		return fChecker.getProposals(fWord, fSentence);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellEvent#getWord()
	 */
	@Override
	public final String getWord() {
		return fWord;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellEvent#isMatch()
	 */
	@Override
	public final boolean isMatch() {
		return fMatch;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellEvent#isStart()
	 */
	@Override
	public final boolean isStart() {
		return fSentence;
	}
}
