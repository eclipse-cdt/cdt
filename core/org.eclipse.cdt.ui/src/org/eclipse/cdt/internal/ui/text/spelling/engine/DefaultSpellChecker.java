/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.cdt.internal.ui.text.spelling.SpellingPreferences;

/**
 * Default spell checker for standard text.
 */
public class DefaultSpellChecker implements ISpellChecker {
	/** Array of URL prefixes */
	public static final String[] URL_PREFIXES= new String[] { "http://", "https://", "www.", "ftp://", "ftps://", "news://", "mailto://" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

	/**
	 * Does this word contain digits?
	 *
	 * @param word the word to check
	 * @return <code>true</code> iff this word contains digits, <code>false</code> otherwise
	 */
	protected static boolean isDigits(final String word) {
		for (int index= 0; index < word.length(); index++) {
			if (Character.isDigit(word.charAt(index)))
				return true;
		}
		return false;
	}

	/**
	 * Does this word contain mixed-case letters?
	 *
	 * @param word
	 *                   The word to check
	 * @param sentence
	 *                   <code>true</code> iff the specified word starts a new
	 *                   sentence, <code>false</code> otherwise
	 * @return <code>true</code> iff the contains mixed-case letters, <code>false</code>
	 *               otherwise
	 */
	protected static boolean isMixedCase(final String word, final boolean sentence) {
		final int length= word.length();
		boolean upper= Character.isUpperCase(word.charAt(0));

		if (sentence && upper && (length > 1))
			upper= Character.isUpperCase(word.charAt(1));

		if (upper) {
			for (int index= length - 1; index > 0; index--) {
				if (Character.isLowerCase(word.charAt(index)))
					return true;
			}
		} else {
			for (int index= length - 1; index > 0; index--) {
				if (Character.isUpperCase(word.charAt(index)))
					return true;
			}
		}
		return false;
	}

	/**
	 * Does this word contain upper-case letters only?
	 *
	 * @param word
	 *                   The word to check
	 * @return <code>true</code> iff this word only contains upper-case
	 *               letters, <code>false</code> otherwise
	 */
	protected static boolean isUpperCase(final String word) {
		for (int index= word.length() - 1; index >= 0; index--) {
			if (Character.isLowerCase(word.charAt(index)))
				return false;
		}
		return true;
	}

	/**
	 * Does this word look like an URL?
	 *
	 * @param word
	 *                   The word to check
	 * @return <code>true</code> iff this word looks like an URL, <code>false</code>
	 *               otherwise
	 */
	protected static boolean isUrl(final String word) {
		for (String element : URL_PREFIXES) {
			if (word.startsWith(element))
				return true;
		}
		return false;
	}

	/**
	 * The dictionaries to use for spell checking. Synchronized to avoid
	 * concurrent modifications.
	 */
	private final Set<ISpellDictionary> fDictionaries= Collections.synchronizedSet(new HashSet<ISpellDictionary>());

	/**
	 * The words to be ignored. Synchronized to avoid concurrent modifications.
	 */
	private final Set<String> fIgnored= Collections.synchronizedSet(new HashSet<String>());

	/**
	 * The spell event listeners. Synchronized to avoid concurrent
	 * modifications.
	 */
	private final Set<ISpellEventListener> fListeners= Collections.synchronizedSet(new HashSet<ISpellEventListener>());

	/**
	 * The locale of this checker.
	 */
	private Locale fLocale;

	/**
	 * Creates a new default spell checker.
	 *
	 * @param store the preference store for this spell checker
	 * @param locale the locale
	 */
	public DefaultSpellChecker(IPreferenceStore store, Locale locale) {
		Assert.isLegal(store != null);
		Assert.isLegal(locale != null);
		
		fLocale= locale;
	}

	/*
	 * @see org.eclipse.spelling.done.ISpellChecker#addDictionary(org.eclipse.spelling.done.ISpellDictionary)
	 */
	@Override
	public final void addDictionary(final ISpellDictionary dictionary) {
		// synchronizing is necessary as this is a write access
		fDictionaries.add(dictionary);
	}

	/*
	 * @see org.eclipse.spelling.done.ISpellChecker#addListener(org.eclipse.spelling.done.ISpellEventListener)
	 */
	@Override
	public final void addListener(final ISpellEventListener listener) {
		// synchronizing is necessary as this is a write access
		fListeners.add(listener);
	}

	/*
	 * @see org.eclipse.cdt.ui.text.spelling.engine.ISpellChecker#acceptsWords()
	 */
	@Override
	public boolean acceptsWords() {
		// synchronizing might not be needed here since acceptWords is
		// a read-only access and only called in the same thread as
		// the modifying methods add/checkWord (?)
		Set<ISpellDictionary> copy;
		synchronized (fDictionaries) {
			copy= new HashSet<ISpellDictionary>(fDictionaries);
		}

		ISpellDictionary dictionary= null;
		for (final Iterator<ISpellDictionary> iterator= copy.iterator(); iterator.hasNext();) {
			dictionary= iterator.next();
			if (dictionary.acceptsWords())
				return true;
		}
		return false;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker#addWord(java.lang.String)
	 */
	@Override
	public void addWord(final String word) {
		// synchronizing is necessary as this is a write access
		Set<ISpellDictionary> copy;
		synchronized (fDictionaries) {
			copy= new HashSet<ISpellDictionary>(fDictionaries);
		}

		final String addable= word.toLowerCase();
		for (ISpellDictionary dictionary : copy) {
			if (dictionary.acceptsWords())
				dictionary.addWord(addable);
		}
	}

	/*
	 * @see org.eclipse.cdt.ui.text.spelling.engine.ISpellChecker#checkWord(java.lang.String)
	 */
	@Override
	public final void checkWord(final String word) {
		// synchronizing is necessary as this is a write access
		fIgnored.remove(word.toLowerCase());
	}

	/*
	 * @see org.eclipse.spelling.done.ISpellChecker#execute(org.eclipse.spelling.ISpellCheckTokenizer)
	 */
	@Override
	public void execute(final ISpellCheckIterator iterator) {
		final boolean ignoreDigits= SpellingPreferences.isIgnoreDigits();
		final boolean ignoreMixed= SpellingPreferences.isIgnoreMixed();
		final boolean ignoreSentence= SpellingPreferences.isIgnoreSentence();
		final boolean ignoreUpper= SpellingPreferences.isIgnoreUpper();
		final boolean ignoreUrls= SpellingPreferences.isIgnoreUrls();
		final boolean ignoreNonLetters= SpellingPreferences.isIgnoreNonLetters();
		final boolean ignoreSingleLetters= SpellingPreferences.isIgnoreSingleLetters();
		
		iterator.setIgnoreSingleLetters(ignoreSingleLetters);
		
		synchronized (fDictionaries) {
			Iterator<ISpellDictionary> iter= fDictionaries.iterator();
			while (iter.hasNext())
				iter.next().setStripNonLetters(ignoreNonLetters);
		}

		String word= null;
		boolean starts= false;

		while (iterator.hasNext()) {
			word= iterator.next();
			if (word != null) {
				// synchronizing is necessary as this is called inside the reconciler
				if (!fIgnored.contains(word)) {
					starts= iterator.startsSentence();
					if (!isCorrect(word)) {
					    boolean isMixed= isMixedCase(word, true);
					    boolean isUpper= isUpperCase(word);
					    boolean isDigits= isDigits(word);
					    boolean isUrl= isUrl(word);

					    if (!ignoreMixed && isMixed || !ignoreUpper && isUpper || !ignoreDigits && isDigits || !ignoreUrls && isUrl || !(isMixed || isUpper || isDigits || isUrl))
					        fireEvent(new SpellEvent(this, word, iterator.getBegin(), iterator.getEnd(), starts, false));
					} else {
						if (!ignoreSentence && starts && Character.isLowerCase(word.charAt(0)))
							fireEvent(new SpellEvent(this, word, iterator.getBegin(), iterator.getEnd(), true, true));
					}
				}
			}
		}
	}

	/**
	 * Fires the specified event.
	 *
	 * @param event
	 *                   Event to fire
	 */
	protected final void fireEvent(final ISpellEvent event) {
		// synchronizing is necessary as this is called from execute
		Set<ISpellEventListener> copy;
		synchronized (fListeners) {
			copy= new HashSet<ISpellEventListener>(fListeners);
		}
		for (ISpellEventListener spellEventListener : copy) {
			spellEventListener.handle(event);
		}
	}

	/*
	 * @see org.eclipse.spelling.done.ISpellChecker#getProposals(java.lang.String,boolean)
	 */
	@Override
	public Set<RankedWordProposal> getProposals(final String word, final boolean sentence) {
		// synchronizing might not be needed here since getProposals is
		// a read-only access and only called in the same thread as
		// the modifying methods add/removeDictionary (?)
		Set<ISpellDictionary> copy;
		synchronized (fDictionaries) {
			copy= new HashSet<ISpellDictionary>(fDictionaries);
		}

		ISpellDictionary dictionary= null;
		final HashSet<RankedWordProposal> proposals= new HashSet<RankedWordProposal>();

		for (final Iterator<ISpellDictionary> iterator= copy.iterator(); iterator.hasNext();) {
			dictionary= iterator.next();
			proposals.addAll(dictionary.getProposals(word, sentence));
		}
		return proposals;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker#ignoreWord(java.lang.String)
	 */
	@Override
	public final void ignoreWord(final String word) {
		// synchronizing is necessary as this is a write access
		fIgnored.add(word.toLowerCase());
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker#isCorrect(java.lang.String)
	 */
	@Override
	public final boolean isCorrect(final String word) {
		// synchronizing is necessary as this is called from execute
		Set<ISpellDictionary> copy;
		synchronized (fDictionaries) {
			copy= new HashSet<ISpellDictionary>(fDictionaries);
		}

		if (fIgnored.contains(word.toLowerCase()))
			return true;

		ISpellDictionary dictionary= null;
		for (final Iterator<ISpellDictionary> iterator= copy.iterator(); iterator.hasNext();) {
			dictionary= iterator.next();
			if (dictionary.isCorrect(word))
				return true;
		}
		return false;
	}

	/*
	 * @see org.eclipse.spelling.done.ISpellChecker#removeDictionary(org.eclipse.spelling.done.ISpellDictionary)
	 */
	@Override
	public final void removeDictionary(final ISpellDictionary dictionary) {
		// synchronizing is necessary as this is a write access
		fDictionaries.remove(dictionary);
	}

	/*
	 * @see org.eclipse.spelling.done.ISpellChecker#removeListener(org.eclipse.spelling.done.ISpellEventListener)
	 */
	@Override
	public final void removeListener(final ISpellEventListener listener) {
		// synchronizing is necessary as this is a write access
		fListeners.remove(listener);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker#getLocale()
	 */
	@Override
	public Locale getLocale() {
		return fLocale;
	}
}
