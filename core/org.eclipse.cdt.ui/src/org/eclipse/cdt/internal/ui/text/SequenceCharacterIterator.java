/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.text.CharacterIterator;

import org.eclipse.core.runtime.Assert;

/**
 * A <code>CharSequence</code> based implementation of <code>CharacterIterator</code>.
 *
 * @since 4.0
 */
public class SequenceCharacterIterator implements CharacterIterator {

	private int fIndex = -1;
	private final CharSequence fSequence;
	private final int fFirst;
	private final int fLast;

	private void invariant() {
		Assert.isTrue(fIndex >= fFirst);
		Assert.isTrue(fIndex <= fLast);
	}

	/**
	 * Creates an iterator for the entire sequence.
	 *
	 * @param sequence the sequence backing this iterator
	 */
	public SequenceCharacterIterator(CharSequence sequence) {
		this(sequence, 0);
	}

	/**
	 * Creates an iterator.
	 *
	 * @param sequence the sequence backing this iterator
	 * @param first the first character to consider
	 * @throws IllegalArgumentException if the indices are out of bounds
	 */
	public SequenceCharacterIterator(CharSequence sequence, int first) throws IllegalArgumentException {
		this(sequence, first, sequence.length());
	}

	/**
	 * Creates an iterator.
	 *
	 * @param sequence the sequence backing this iterator
	 * @param first the first character to consider
	 * @param last the last character index to consider
	 * @throws IllegalArgumentException if the indices are out of bounds
	 */
	public SequenceCharacterIterator(CharSequence sequence, int first, int last) throws IllegalArgumentException {
		if (sequence == null)
			throw new NullPointerException();
		if (first < 0 || first > last)
			throw new IllegalArgumentException();
		if (last > sequence.length())
			throw new IllegalArgumentException();
		fSequence = sequence;
		fFirst = first;
		fLast = last;
		fIndex = first;
		invariant();
	}

	/*
	 * @see java.text.CharacterIterator#first()
	 */
	@Override
	public char first() {
		return setIndex(getBeginIndex());
	}

	/*
	 * @see java.text.CharacterIterator#last()
	 */
	@Override
	public char last() {
		if (fFirst == fLast)
			return setIndex(getEndIndex());
		return setIndex(getEndIndex() - 1);
	}

	/*
	 * @see java.text.CharacterIterator#current()
	 */
	@Override
	public char current() {
		if (fIndex >= fFirst && fIndex < fLast)
			return fSequence.charAt(fIndex);
		return DONE;
	}

	/*
	 * @see java.text.CharacterIterator#next()
	 */
	@Override
	public char next() {
		return setIndex(Math.min(fIndex + 1, getEndIndex()));
	}

	/*
	 * @see java.text.CharacterIterator#previous()
	 */
	@Override
	public char previous() {
		if (fIndex > getBeginIndex()) {
			return setIndex(fIndex - 1);
		}
		return DONE;
	}

	/*
	 * @see java.text.CharacterIterator#setIndex(int)
	 */
	@Override
	public char setIndex(int position) {
		if (position >= getBeginIndex() && position <= getEndIndex())
			fIndex = position;
		else
			throw new IllegalArgumentException();

		invariant();
		return current();
	}

	/*
	 * @see java.text.CharacterIterator#getBeginIndex()
	 */
	@Override
	public int getBeginIndex() {
		return fFirst;
	}

	/*
	 * @see java.text.CharacterIterator#getEndIndex()
	 */
	@Override
	public int getEndIndex() {
		return fLast;
	}

	/*
	 * @see java.text.CharacterIterator#getIndex()
	 */
	@Override
	public int getIndex() {
		return fIndex;
	}

	/*
	 * @see java.text.CharacterIterator#clone()
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
}
