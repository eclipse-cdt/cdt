/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.cdt.internal.ui.text;

import java.text.CharacterIterator;

import org.eclipse.core.runtime.Assert;

import com.ibm.icu.text.BreakIterator;

/**
 * Breaks C text into word starts, also stops at line start and end. No
 * direction dependency.
 *
 * @since 4.0
 */
public class CWordIterator extends BreakIterator {

	/**
	 * The underlying C break iterator. It returns all breaks, including
	 * before and after every whitespace.
	 */
	private CBreakIterator fIterator;
	/** The current index for the stateful operations. */
	private int fIndex;

	/**
	 * Creates a new word iterator.
	 */
	public CWordIterator() {
		fIterator = new CBreakIterator();
		first();
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#first()
	 */
	@Override
	public int first() {
		fIndex = fIterator.first();
		return fIndex;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#last()
	 */
	@Override
	public int last() {
		fIndex = fIterator.last();
		return fIndex;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#next(int)
	 */
	@Override
	public int next(int n) {
		int next = 0;
		while (--n > 0 && next != DONE) {
			next = next();
		}
		return next;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#next()
	 */
	@Override
	public int next() {
		fIndex = following(fIndex);
		return fIndex;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#previous()
	 */
	@Override
	public int previous() {
		fIndex = preceding(fIndex);
		return fIndex;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#preceding(int)
	 */
	@Override
	public int preceding(int offset) {
		int first = fIterator.preceding(offset);
		if (isWhitespace(first, offset)) {
			int second = fIterator.preceding(first);
			if (second != DONE && !isDelimiter(second, first))
				return second;
		}
		return first;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#following(int)
	 */
	@Override
	public int following(int offset) {
		int first = fIterator.following(offset);
		if (eatFollowingWhitespace(offset, first)) {
			int second = fIterator.following(first);
			if (isWhitespace(first, second))
				return second;
		}
		return first;
	}

	private boolean eatFollowingWhitespace(int offset, int exclusiveEnd) {
		if (exclusiveEnd == DONE || offset == DONE)
			return false;

		if (isWhitespace(offset, exclusiveEnd))
			return false;
		if (isDelimiter(offset, exclusiveEnd))
			return false;

		return true;
	}

	/**
	 * Returns <code>true</code> if the given sequence into the underlying text
	 * represents a delimiter, <code>false</code> otherwise.
	 *
	 * @param offset the offset
	 * @param exclusiveEnd the end offset
	 * @return <code>true</code> if the given range is a delimiter
	 */
	private boolean isDelimiter(int offset, int exclusiveEnd) {
		if (exclusiveEnd == DONE || offset == DONE)
			return false;

		Assert.isTrue(offset >= 0);
		Assert.isTrue(exclusiveEnd <= getText().getEndIndex());
		Assert.isTrue(exclusiveEnd > offset);

		CharSequence seq = fIterator.fText;

		while (offset < exclusiveEnd) {
			char ch = seq.charAt(offset);
			if (ch != '\n' && ch != '\r')
				return false;
			offset++;
		}

		return true;
	}

	/**
	 * Returns <code>true</code> if the given sequence into the underlying text
	 * represents whitespace, but not a delimiter, <code>false</code> otherwise.
	 *
	 * @param offset the offset
	 * @param exclusiveEnd the end offset
	 * @return <code>true</code> if the given range is whitespace
	 */
	private boolean isWhitespace(int offset, int exclusiveEnd) {
		if (exclusiveEnd == DONE || offset == DONE)
			return false;

		Assert.isTrue(offset >= 0);
		Assert.isTrue(exclusiveEnd <= getText().getEndIndex());
		Assert.isTrue(exclusiveEnd > offset);

		CharSequence seq = fIterator.fText;

		while (offset < exclusiveEnd) {
			char ch = seq.charAt(offset);
			if (!Character.isWhitespace(ch))
				return false;
			if (ch == '\n' || ch == '\r')
				return false;
			offset++;
		}

		return true;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#current()
	 */
	@Override
	public int current() {
		return fIndex;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#getText()
	 */
	@Override
	public CharacterIterator getText() {
		return fIterator.getText();
	}

	/**
	 * Sets the text as <code>CharSequence</code>.
	 * @param newText the new text
	 */
	public void setText(CharSequence newText) {
		fIterator.setText(newText);
		first();
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#setText(java.text.CharacterIterator)
	 */
	@Override
	public void setText(CharacterIterator newText) {
		fIterator.setText(newText);
		first();
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#setText(java.lang.String)
	 */
	@Override
	public void setText(String newText) {
		setText((CharSequence) newText);
	}

	/**
	 * Enables breaks at word boundaries inside a camel case identifier.
	 *
	 * @param camelCaseBreakEnabled <code>true</code> to enable,
	 * <code>false</code> to disable.
	 */
	public void setCamelCaseBreakEnabled(boolean camelCaseBreakEnabled) {
		fIterator.setCamelCaseBreakEnabled(camelCaseBreakEnabled);
	}

	/**
	 * @return <code>true</code> if breaks at word boundaries inside
	 * a camel case identifier are enabled.
	 */
	public boolean isCamelCaseBreakEnabled() {
		return fIterator.isCamelCaseBreakEnabled();
	}
}
