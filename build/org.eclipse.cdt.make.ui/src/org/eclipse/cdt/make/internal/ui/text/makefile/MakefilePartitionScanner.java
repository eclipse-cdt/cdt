/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class MakefilePartitionScanner extends RuleBasedPartitionScanner {
	// Partition types
	public final static String MAKEFILE_COMMENT = "makefile_comment"; //$NON-NLS-1$
	public final static String MAKEFILE_MACRO_ASSIGNEMENT = "makefile_macro_assignement"; //$NON-NLS-1$
	public final static String MAKEFILE_INCLUDE_BLOCK = "makefile_include_block"; //$NON-NLS-1$
	public final static String MAKEFILE_IF_BLOCK = "makefile_if_block"; //$NON-NLS-1$
	public final static String MAKEFILE_DEF_BLOCK = "makefile_def_block"; //$NON-NLS-1$
	public final static String MAKEFILE_OTHER = "makefile_other"; //$NON-NLS-1$

	public final static String[] TYPES =
		new String[] {
			MAKEFILE_COMMENT,
			MAKEFILE_MACRO_ASSIGNEMENT,
			MAKEFILE_INCLUDE_BLOCK,
			MAKEFILE_IF_BLOCK,
			MAKEFILE_DEF_BLOCK,
			MAKEFILE_OTHER,
		// All other
	};

	/** The predefined delimiters of this tracker */
	private char[][] fModDelimiters = { { '\r', '\n' }, {
			'\r' }, {
			'\n' }
	};

	/**
	 * Constructor for MakefilePartitionScanner
	 */
	public MakefilePartitionScanner() {
		super();

		IToken tComment = new Token(MAKEFILE_COMMENT);
		IToken tMacro = new Token(MAKEFILE_MACRO_ASSIGNEMENT);
		IToken tInclude = new Token(MAKEFILE_INCLUDE_BLOCK);
		IToken tIf = new Token(MAKEFILE_IF_BLOCK);
		IToken tDef = new Token(MAKEFILE_DEF_BLOCK);
		IToken tOther = new Token(MAKEFILE_OTHER);

		List rules = new ArrayList();

		// Add rule for single line comments.

		rules.add(new EndOfLineRule("#", tComment, '\\')); //$NON-NLS-1$

		rules.add(new EndOfLineRule("include", tInclude)); //$NON-NLS-1$

		rules.add(new EndOfLineRule("export", tDef)); //$NON-NLS-1$
		rules.add(new EndOfLineRule("unexport", tDef)); //$NON-NLS-1$
		rules.add(new EndOfLineRule("vpath", tDef)); //$NON-NLS-1$
		rules.add(new EndOfLineRule("override", tDef)); //$NON-NLS-1$
		rules.add(new MultiLineRule("define", "endef", tDef)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MultiLineRule("override define", "endef", tDef)); //$NON-NLS-1$ //$NON-NLS-2$

		// Add rules for multi-line comments and javadoc.
		rules.add(new MultiLineRule("ifdef", "endif", tIf)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MultiLineRule("ifndef", "endif", tIf)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MultiLineRule("ifeq", "endif", tIf)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MultiLineRule("ifnneq", "endif", tIf)); //$NON-NLS-1$ //$NON-NLS-2$

		// Last rule must be supplied with default token!
		rules.add(new MacroRule(tMacro, tOther)); //$NON-NLS-1$

		IPredicateRule[] result = new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);

	}

	/*
	 * @see ICharacterScanner#getLegalLineDelimiters
	 */
	public char[][] getLegalLineDelimiters() {
		return fModDelimiters;
	}

	private class MacroRule implements IPredicateRule {
		private static final int INIT_STATE = 0;
		private static final int VAR_STATE = 1;
		private static final int END_VAR_STATE = 2;
		private static final int EQUAL_STATE = 3;
		private static final int FINISH_STATE = 4;
		private static final int ERROR_STATE = 5;

		private IToken token;
		private StringBuffer buffer = new StringBuffer();
		protected IToken defaultToken;
		public MacroRule(IToken token, IToken defaultToken) {
			this.token = token;
			this.defaultToken = defaultToken;
		}

		public IToken getSuccessToken() {
			return token;
		}

		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			buffer.setLength(0);
			int state = INIT_STATE;

			if (resume)
				scanToBeginOfLine(scanner);

			for (int c = scanner.read(); c != ICharacterScanner.EOF; c = scanner.read()) {
				switch (state) {
					case INIT_STATE :
						if (c != '\n' && Character.isWhitespace((char) c))
							break;
						if (isValidCharacter(c))
							state = VAR_STATE;
						else
							state = ERROR_STATE;
						break;
					case VAR_STATE :
						if (isValidCharacter(c))
							break;
					case END_VAR_STATE :
						if (c != '\n' && Character.isWhitespace((char) c))
							state = END_VAR_STATE;
						else if (c == ':' || c == '+')
							state = EQUAL_STATE;
						else if (c == '=')
							state = FINISH_STATE;
						else {
							if (state == END_VAR_STATE)
								scanner.unread(); // Return back to the space
							state = ERROR_STATE;
						}
						break;
					case EQUAL_STATE :
						if (c == '=')
							state = FINISH_STATE;
						else
							state = ERROR_STATE;
						break;
					case FINISH_STATE :
						break;
					default :
						break;
				}
				if (state >= FINISH_STATE)
					break;
				buffer.append((char) c);
			}

			scanner.unread();

			if (state == FINISH_STATE) {
				scanToEndOfLine(scanner);
				return token;
			}

			if (defaultToken.isUndefined())
				unreadBuffer(scanner);

			return Token.UNDEFINED;

		}

		public IToken evaluate(ICharacterScanner scanner) {
			return evaluate(scanner, false);
		}

		/**
		 * Returns the characters in the buffer to the scanner.
		 *
		 * @param scanner the scanner to be used
		 */
		protected void unreadBuffer(ICharacterScanner scanner) {
			for (int i = buffer.length() - 1; i >= 0; i--)
				scanner.unread();
		}

		private void scanToEndOfLine(ICharacterScanner scanner) {
			int c;
			char[][] delimiters = scanner.getLegalLineDelimiters();
			while ((c = scanner.read()) != ICharacterScanner.EOF) {
				// Check for end of line since it can be used to terminate the pattern.
				for (int i = 0; i < delimiters.length; i++) {
					if (c == delimiters[i][0] && sequenceDetected(scanner, delimiters[i]))
						return;
				}
			}
		}

		private void scanToBeginOfLine(ICharacterScanner scanner) {
			while(scanner.getColumn() != 0) {
				scanner.unread();
			}
		}

		private boolean sequenceDetected(ICharacterScanner scanner, char[] sequence) {
			for (int i = 1; i < sequence.length; i++) {
				int c = scanner.read();
				if (c == ICharacterScanner.EOF) {
					return true;
				} else if (c != sequence[i]) {
					// Non-matching character detected, rewind the scanner back to the start.
					for (; i > 0; i--)
						scanner.unread();
					return false;
				}
			}

			return true;
		}
		protected boolean isValidCharacter(int c) {
			char c0 = (char) c;
			return Character.isLetterOrDigit(c0) || (c0 == '_');
		}

	}

}
