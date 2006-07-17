/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import com.ibm.icu.text.BreakIterator;
import java.text.CharacterIterator;

import org.eclipse.jface.text.Assert;


/**
 * A C break iterator. It returns all breaks, including before and after
 * whitespace, and it returns all camel case breaks.
 * <p>
 * A line break may be any of "\n", "\r", "\r\n", "\n\r".
 * </p>
 *
 * @since 4.0
 */
public class CBreakIterator extends BreakIterator {

	/**
	 * A run of common characters.
	 */
	protected static abstract class Run {
		/** The length of this run. */
		protected int length;

		public Run() {
			init();
		}

		/**
		 * Returns <code>true</code> if this run consumes <code>ch</code>,
		 * <code>false</code> otherwise. If <code>true</code> is returned,
		 * the length of the receiver is adjusted accordingly.
		 *
		 * @param ch the character to test
		 * @return <code>true</code> if <code>ch</code> was consumed
		 */
		protected boolean consume(char ch) {
			if (isValid(ch)) {
				length++;
				return true;
			}
			return false;
		}

		/**
		 * Whether this run accepts that character; does not update state. Called
		 * from the default implementation of <code>consume</code>.
		 *
		 * @param ch the character to test
		 * @return <code>true</code> if <code>ch</code> is accepted
		 */
		protected abstract boolean isValid(char ch);

		/**
		 * Resets this run to the initial state.
		 */
		protected void init() {
			length= 0;
		}
	}

	static final class Whitespace extends Run {
		protected boolean isValid(char ch) {
			return Character.isWhitespace(ch) && ch != '\n' && ch != '\r';
		}
	}

	static final class LineDelimiter extends Run {
		/** State: INIT -> delimiter -> EXIT. */
		private char fState;
		private static final char INIT= '\0';
		private static final char EXIT= '\1';

		/*
		 * @see org.eclipse.cdt.internal.ui.text.CBreakIterator.Run#init()
		 */
		protected void init() {
			super.init();
			fState= INIT;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.text.CBreakIterator.Run#consume(char)
		 */
		protected boolean consume(char ch) {
			if (!isValid(ch) || fState == EXIT)
				return false;

			if (fState == INIT) {
				fState= ch;
				length++;
				return true;
			} else if (fState != ch) {
				fState= EXIT;
				length++;
				return true;
			} else {
				return false;
			}
		}

		protected boolean isValid(char ch) {
			return ch == '\n' || ch == '\r';
		}
	}

	static final class Identifier extends Run {
		/*
		 * @see org.eclipse.cdt.internal.ui.text.CBreakIterator.Run#isValid(char)
		 */
		protected boolean isValid(char ch) {
			return Character.isJavaIdentifierPart(ch);
		}
	}

	static final class CamelCaseIdentifier extends Run {
		/* states */
		private static final int S_INIT= 0;
		private static final int S_LOWER= 1;
		private static final int S_ONE_CAP= 2;
		private static final int S_ALL_CAPS= 3;
		private static final int S_UNDERSCORE= 4;
		private static final int S_EXIT= 5;
		private static final int S_EXIT_MINUS_ONE= 6;

		/* character types */
		private static final int K_INVALID= 0;
		private static final int K_LOWER= 1;
		private static final int K_UPPER= 2;
		private static final int K_UNDERSCORE= 3;
		private static final int K_OTHER= 4;

		private int fState;

		private final static int[][] MATRIX= new int[][] {
				// K_INVALID, K_LOWER,           K_UPPER,    K_UNDERSCORE, K_OTHER
				{  S_EXIT,    S_LOWER,           S_ONE_CAP,  S_UNDERSCORE, S_LOWER }, // S_INIT
				{  S_EXIT,    S_LOWER,           S_EXIT,     S_UNDERSCORE, S_LOWER }, // S_LOWER
				{  S_EXIT,    S_LOWER,           S_ALL_CAPS, S_UNDERSCORE, S_LOWER }, // S_ONE_CAP
				{  S_EXIT,    S_EXIT_MINUS_ONE,  S_ALL_CAPS, S_UNDERSCORE, S_LOWER }, // S_ALL_CAPS
				{  S_EXIT,    S_EXIT,            S_EXIT,     S_UNDERSCORE, S_EXIT  }, // S_UNDERSCORE
		};

		/*
		 * @see org.eclipse.cdt.internal.ui.text.CBreakIterator.Run#init()
		 */
		protected void init() {
			super.init();
			fState= S_INIT;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.text.CBreakIterator.Run#consumes(char)
		 */
		protected boolean consume(char ch) {
			int kind= getKind(ch);
			fState= MATRIX[fState][kind];
			switch (fState) {
				case S_LOWER:
				case S_ONE_CAP:
				case S_ALL_CAPS:
				case S_UNDERSCORE:
					length++;
					return true;
				case S_EXIT:
					return false;
				case S_EXIT_MINUS_ONE:
					length--;
					return false;
				default:
					Assert.isTrue(false);
					return false;
			}
		}

		/**
		 * Determines the kind of a character.
		 *
		 * @param ch the character to test
		 */
		private int getKind(char ch) {
			if (Character.isUpperCase(ch))
				return K_UPPER;
			if (Character.isLowerCase(ch))
				return K_LOWER;
			if (ch == '_')
				return K_UNDERSCORE;
			if (Character.isJavaIdentifierPart(ch)) // digits...
				return K_OTHER;
			return K_INVALID;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.text.CBreakIterator.Run#isValid(char)
		 */
		protected boolean isValid(char ch) {
			return Character.isJavaIdentifierPart(ch);
		}
	}

	static final class Other extends Run {
		/*
		 * @see org.eclipse.cdt.internal.ui.text.CBreakIterator.Run#isValid(char)
		 */
		protected boolean isValid(char ch) {
			return !Character.isWhitespace(ch) && !Character.isJavaIdentifierPart(ch);
		}
	}

	private static final Run WHITESPACE= new Whitespace();
	private static final Run DELIMITER= new LineDelimiter();
	private static final Run IDENTIFIER= new Identifier();
	private static final Run CAMELCASE= new CamelCaseIdentifier();
	private static final Run OTHER= new Other();

	/** The platform break iterator (word instance) used as a base. */
	protected final BreakIterator fIterator;
	/** The text we operate on. */
	protected CharSequence fText;
	/** our current position for the stateful methods. */
	private int fIndex;
	/** Break on camel case word boundaries */
	private boolean fCamelCaseBreakEnabled = true;


	/**
	 * Creates a new break iterator.
	 */
	public CBreakIterator() {
		fIterator= BreakIterator.getWordInstance();
		fIndex= fIterator.current();
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#current()
	 */
	public int current() {
		return fIndex;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#first()
	 */
	public int first() {
		fIndex= fIterator.first();
		return fIndex;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#following(int)
	 */
	public int following(int offset) {
		// work around too eager IAEs in standard implementation
		if (offset == getText().getEndIndex())
			return DONE;

		int next= fIterator.following(offset);
		if (next == DONE)
			return DONE;

		// TODO deal with complex script word boundaries
		// Math.min(offset + run.length, next) does not work
		// since BreakIterator.getWordInstance considers _ as boundaries
		// seems to work fine, however
		Run run= consumeRun(offset);
		return offset + run.length;

	}

	/**
	 * Consumes a run of characters at the limits of which we introduce a break.
	 * @param offset the offset to start at
	 * @return the run that was consumed
	 */
	private Run consumeRun(int offset) {
		// assert offset < length

		char ch= fText.charAt(offset);
		int length= fText.length();
		Run run= getRun(ch);
		while (run.consume(ch) && offset < length - 1) {
			offset++;
			ch= fText.charAt(offset);
		}

		return run;
	}

	/**
	 * Returns a run based on a character.
	 *
	 * @param ch the character to test
	 * @return the correct character given <code>ch</code>
	 */
	private Run getRun(char ch) {
		Run run;
		if (WHITESPACE.isValid(ch))
			run= WHITESPACE;
		else if (DELIMITER.isValid(ch))
			run= DELIMITER;
		else if (IDENTIFIER.isValid(ch)) {
			if (fCamelCaseBreakEnabled)
				run= CAMELCASE;
			else
				run= IDENTIFIER;
		}
		else if (OTHER.isValid(ch))
			run= OTHER;
		else {
			Assert.isTrue(false);
			return null;
		}

		run.init();
		return run;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#getText()
	 */
	public CharacterIterator getText() {
		return fIterator.getText();
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#isBoundary(int)
	 */
	public boolean isBoundary(int offset) {
        if (offset == getText().getBeginIndex())
            return true;
        else
            return following(offset - 1) == offset;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#last()
	 */
	public int last() {
		fIndex= fIterator.last();
		return fIndex;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#next()
	 */
	public int next() {
		fIndex= following(fIndex);
		return fIndex;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#next(int)
	 */
	public int next(int n) {
		return fIterator.next(n);
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#preceding(int)
	 */
	public int preceding(int offset) {
		if (offset == getText().getBeginIndex())
			return DONE;

		if (isBoundary(offset - 1))
			return offset - 1;

		int previous= offset - 1;
		do {
			previous= fIterator.preceding(previous);
		} while (!isBoundary(previous));

		int last= DONE;
		while (previous < offset) {
			last= previous;
			previous= following(previous);
		}

		return last;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#previous()
	 */
	public int previous() {
		fIndex= preceding(fIndex);
		return fIndex;
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#setText(java.lang.String)
	 */
	public void setText(String newText) {
		setText((CharSequence) newText);
	}

	/**
	 * Creates a break iterator given a char sequence.
	 * @param newText the new text
	 */
	public void setText(CharSequence newText) {
		fText= newText;
		fIterator.setText(new SequenceCharacterIterator(newText));
		first();
	}

	/*
	 * @see com.ibm.icu.text.BreakIterator#setText(java.text.CharacterIterator)
	 */
	public void setText(CharacterIterator newText) {
		if (newText instanceof CharSequence) {
			fText= (CharSequence) newText;
			fIterator.setText(newText);
			first();
		} else {
			throw new UnsupportedOperationException("CharacterIterator not supported"); //$NON-NLS-1$
		}
	}

	/**
	 * Enables breaks at word boundaries inside a camel case identifier.
	 *  
	 * @param camelCaseBreakEnabled <code>true</code> to enable, <code>false</code> to disable.
	 */
	public void setCamelCaseBreakEnabled(boolean camelCaseBreakEnabled) {
		fCamelCaseBreakEnabled = camelCaseBreakEnabled;
	}

	/**
	 * @return <code>true</code> if breaks at word boundaries inside
	 * a camel case identifier are enabled.
	 */
	public boolean isCamelCaseBreakEnabled() {
		return fCamelCaseBreakEnabled;
	}
}
