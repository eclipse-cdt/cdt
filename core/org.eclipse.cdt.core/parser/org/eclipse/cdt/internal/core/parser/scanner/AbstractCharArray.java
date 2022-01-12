/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

/**
 * Abstract class for providing input to the lexer.
 * @since 5.2
 */
public abstract class AbstractCharArray {
	/**
	 * Returns the length of this array or -1 if it is yet, unknown. This method may be called
	 * before the array has been traversed.
	 */
	public abstract int tryGetLength();

	/**
	 * Returns the length of the array. This method is called only after the lexer has worked its
	 * way through the array. Therefore for subclasses it is efficient enough to read through to
	 * the end of the array and provide the length.
	 */
	public abstract int getLength();

	/**
	 * Checks whether the given offset is valid for this array. Subclasses may assume
	 * that offset is non-negative.
	 */
	public abstract boolean isValidOffset(int offset);

	/**
	 * Computes 64-bit hash value of the character array. This method doesn't cause any I/O if
	 * called after the array has been traversed.
	 * @return The hash value of the contents of the array.
	 */
	public abstract long getContentsHash();

	/**
	 * Returns the character at the given position, subclasses do not have to do range checks.
	 */
	public abstract char get(int offset);

	/**
	 * Copies a range of characters to the given destination. Subclasses do not have to do any
	 * range checks.
	 */
	public abstract void arraycopy(int offset, char[] destination, int destinationPos, int length);

	/**
	 * Returns the {@link CharSequence} representing a range in the character array.
	 */
	public CharSequence subSequence(int start, int end) {
		return new SubArray(start, end);
	}

	/**
	 * Returns {@code true} if there were I/O errors while retrieving contents of this array.
	 */
	public abstract boolean hasError();

	/**
	 * This method is slow. Use only for debugging.
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (int pos = 0; isValidOffset(pos); pos++) {
			buf.append(get(pos));
		}
		return buf.toString();
	}

	private class SubArray implements CharSequence {
		private final int start;
		private final int end;

		SubArray(int start, int end) {
			checkStartEnd(start, end);
			this.start = start;
			this.end = end;
		}

		@Override
		public int length() {
			return end - start;
		}

		@Override
		public char charAt(int index) {
			return get(start + index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			checkStartEnd(start, end);
			if (end > this.end - this.start)
				throw new IndexOutOfBoundsException(String.valueOf(end));
			return new SubArray(this.start + start, this.start + end);
		}

		private void checkStartEnd(int start, int end) {
			if (start < 0)
				throw new IndexOutOfBoundsException(String.valueOf(start));
			if (end < start)
				throw new IndexOutOfBoundsException(String.valueOf(end) + " < " + String.valueOf(start)); //$NON-NLS-1$
		}
	}
}
