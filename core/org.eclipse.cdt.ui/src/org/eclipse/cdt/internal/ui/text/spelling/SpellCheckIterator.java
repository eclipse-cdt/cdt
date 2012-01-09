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
package org.eclipse.cdt.internal.ui.text.spelling;

import java.util.LinkedList;
import java.util.Locale;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.ibm.icu.text.BreakIterator;

import org.eclipse.cdt.internal.ui.text.IHtmlTagConstants;
import org.eclipse.cdt.internal.ui.text.spelling.engine.DefaultSpellChecker;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellCheckIterator;

/**
 * Iterator to spell check multiline comment regions.
 */
public class SpellCheckIterator implements ISpellCheckIterator {
	/** The content of the region */
	protected final String fContent;

	/** The last token */
	protected String fLastToken= null;

	/** The next break */
	protected int fNext= 1;

	/** The offset of the region */
	protected final int fOffset;

	/** The predecessor break */
	private int fPredecessor;

	/** The previous break */
	protected int fPrevious= 0;

	/** The sentence breaks */
	private final LinkedList<Integer> fSentenceBreaks= new LinkedList<Integer>();

	/** Does the current word start a sentence? */
	private boolean fStartsSentence= false;

	/** The successor break */
	protected int fSuccessor;

	/** The word iterator */
	private final BreakIterator fWordIterator;

	private boolean fIsIgnoringSingleLetters;

	/**
	 * Creates a new spell check iterator.
	 *
	 * @param document the document containing the specified partition
	 * @param region the region to spell check
	 * @param locale the locale to use for spell checking
	 */
	public SpellCheckIterator(IDocument document, IRegion region, Locale locale) {
		this(document, region, locale, BreakIterator.getWordInstance(locale));
	}

	/**
	 * Creates a new spell check iterator.
	 *
	 * @param document the document containing the specified partition
	 * @param region the region to spell check
	 * @param locale the locale to use for spell checking
	 * @param breakIterator the break-iterator
	 */
	public SpellCheckIterator(IDocument document, IRegion region, Locale locale, BreakIterator breakIterator) {
		fOffset= region.getOffset();
		fWordIterator= breakIterator;

		String content;
		try {
			content= document.get(region.getOffset(), region.getLength());
		} catch (Exception exception) {
			content= ""; //$NON-NLS-1$
		}
		fContent= content;

		fWordIterator.setText(content);
		fPredecessor= fWordIterator.first();
		fSuccessor= fWordIterator.next();

		final BreakIterator iterator= BreakIterator.getSentenceInstance(locale);
		iterator.setText(content);

		int offset= iterator.current();
		while (offset != BreakIterator.DONE) {
			fSentenceBreaks.add(new Integer(offset));
			offset= iterator.next();
		}
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellCheckIterator#setIgnoreSingleLetters(boolean)
	 */
	@Override
	public void setIgnoreSingleLetters(boolean state) {
		fIsIgnoringSingleLetters= state;
	}

	/*
	 * @see org.eclipse.spelling.done.ISpellCheckIterator#getBegin()
	 */
	@Override
	public final int getBegin() {
		return fPrevious + fOffset;
	}

	/*
	 * @see org.eclipse.spelling.done.ISpellCheckIterator#getEnd()
	 */
	@Override
	public final int getEnd() {
		return fNext + fOffset - 1;
	}

	/*
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public final boolean hasNext() {
		return fSuccessor != BreakIterator.DONE;
	}

	/**
	 * Does the specified token consist of at least one letter and digits
	 * only?
	 *
	 * @param begin the begin index
	 * @param end the end index
	 * @return <code>true</code> iff the token consists of digits and at
	 *         least one letter only, <code>false</code> otherwise
	 */
	protected final boolean isAlphaNumeric(final int begin, final int end) {
		char character= 0;
		boolean letter= false;
		for (int index= begin; index < end; index++) {
			character= fContent.charAt(index);
			if (Character.isLetter(character))
				letter= true;

			if (!Character.isLetterOrDigit(character))
				return false;
		}
		return letter;
	}

	/**
	 * Checks the last token against the given tags?
	 *
	 * @param tags the tags to check
	 * @return <code>true</code> iff the last token is in the given array
	 */
	protected final boolean isToken(final String[] tags) {
		return isToken(fLastToken, tags);
	}
	
	/**
	 * Checks the given  token against the given tags?
	 *
	 * @param token the token to check
	 * @param tags the tags to check
	 * @return <code>true</code> iff the last token is in the given array
	 */
	protected final boolean isToken(final String token, final String[] tags) {
		if (token != null) {
			for (String tag : tags) {
				if (token.equals(tag))
					return true;
			}
		}
		return false;
	}

	/**
	 * Is the current token a single letter token surrounded by
	 * non-whitespace characters?
	 *
	 * @param begin the begin index
	 * @return <code>true</code> iff the token is a single letter token,
	 *         <code>false</code> otherwise
	 */
	protected final boolean isSingleLetter(final int begin) {
		if (!Character.isLetter(fContent.charAt(begin)))
			return false;

		if (begin > 0 && !Character.isWhitespace(fContent.charAt(begin - 1)))
			return false;

		if (begin < fContent.length() - 1 && !Character.isWhitespace(fContent.charAt(begin + 1)))
			return false;
		
		return true;
	}

	/**
	 * Does the specified token look like an URL?
	 *
	 * @param begin the begin index
	 * @return <code>true</code> iff this token look like an URL,
	 *         <code>false</code> otherwise
	 */
	protected final boolean isUrlToken(final int begin) {
		for (String element : DefaultSpellChecker.URL_PREFIXES) {
			if (fContent.startsWith(element, begin))
				return true;
		}
		return false;
	}

	/**
	 * Does the specified token consist of whitespace only?
	 *
	 * @param begin the begin index
	 * @param end the end index
	 * @return <code>true</code> iff the token consists of whitespace
	 *         only, <code>false</code> otherwise
	 */
	protected final boolean isWhitespace(final int begin, final int end) {
		for (int index= begin; index < end; index++) {
			if (!Character.isWhitespace(fContent.charAt(index)))
				return false;
		}
		return true;
	}

	/*
	 * @see java.util.Iterator#next()
	 */
	@Override
	public String next() {
		String token= nextToken();
		while (token == null && fSuccessor != BreakIterator.DONE)
			token= nextToken();

		fLastToken= token;
		return token;
	}

	/**
	 * Advances the end index to the next word break.
	 */
	protected final void nextBreak() {
		fNext= fSuccessor;
		fPredecessor= fSuccessor;
		fSuccessor= fWordIterator.next();
	}

	/**
	 * Returns the next sentence break.
	 *
	 * @return the next sentence break
	 */
	protected final int nextSentence() {
		return fSentenceBreaks.getFirst().intValue();
	}

	/**
	 * Determines the next token to be spell checked.
	 *
	 * @return the next token to be spell checked, or <code>null</code>
	 *         iff the next token is not a candidate for spell checking.
	 */
	protected String nextToken() {
		String token= null;
		fPrevious= fPredecessor;
		fStartsSentence= false;
		nextBreak();

		boolean update= false;
		if (fNext - fPrevious > 0) {
			if (fSuccessor != BreakIterator.DONE && fContent.charAt(fPrevious) == IHtmlTagConstants.HTML_TAG_PREFIX && (Character.isLetter(fContent.charAt(fNext)) || fContent.charAt(fNext) == '/')) {
				if (fContent.startsWith(IHtmlTagConstants.HTML_CLOSE_PREFIX, fPrevious))
					nextBreak();

				nextBreak();

				if (fSuccessor != BreakIterator.DONE && fContent.charAt(fNext) == IHtmlTagConstants.HTML_TAG_POSTFIX) {
					nextBreak();
					if (fSuccessor != BreakIterator.DONE) {
						update= true;
						token= fContent.substring(fPrevious, fNext);
					}
				}
			} else if (fSuccessor != BreakIterator.DONE && fContent.charAt(fPrevious) == IHtmlTagConstants.HTML_ENTITY_START && (Character.isLetter(fContent.charAt(fNext)))) {
				nextBreak();
				if (fSuccessor != BreakIterator.DONE && fContent.charAt(fNext) == IHtmlTagConstants.HTML_ENTITY_END) {
					nextBreak();
					if (isToken(fContent.substring(fPrevious, fNext), IHtmlTagConstants.HTML_ENTITY_CODES)) {
						skipTokens(fPrevious, IHtmlTagConstants.HTML_ENTITY_END);
						update= true;
					} else {
						token= fContent.substring(fPrevious, fNext);
					}
				} else {
					token= fContent.substring(fPrevious, fNext);
				}
				
				update= true;
			} else if (!isWhitespace(fPrevious, fNext) && isAlphaNumeric(fPrevious, fNext)) {
				if (isUrlToken(fPrevious)) {
					skipTokens(fPrevious, ' ');
				} else if (fNext - fPrevious > 1 || isSingleLetter(fPrevious) && !fIsIgnoringSingleLetters) {
					token= fContent.substring(fPrevious, fNext);
				}
				update= true;
			}
		}

		if (update && fSentenceBreaks.size() > 0) {
			if (fPrevious >= nextSentence()) {
				while (fSentenceBreaks.size() > 0 && fPrevious >= nextSentence())
					fSentenceBreaks.removeFirst();

				fStartsSentence= (fLastToken == null) || (token != null);
			}
		}
		return token;
	}

	/*
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public final void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Skip the tokens until the stop character is reached.
	 *
	 * @param begin the begin index
	 * @param stop the stop character
	 */
	protected final void skipTokens(final int begin, final char stop) {
		int end= begin;

		while (end < fContent.length() && fContent.charAt(end) != stop)
			end++;

		if (end < fContent.length()) {
			fNext= end;
			fPredecessor= fNext;
			fSuccessor= fWordIterator.following(fNext);
		} else {
			fSuccessor= BreakIterator.DONE;
		}
	}

	/*
	 * @see org.eclipse.spelling.done.ISpellCheckIterator#startsSentence()
	 */
	@Override
	public final boolean startsSentence() {
		return fStartsSentence;
	}
}
