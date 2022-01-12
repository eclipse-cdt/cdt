/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - Modified from MacroDefinitionRule to support Automake files
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

class AutomakeMacroDefinitionRule implements IPredicateRule {
	private static final int INIT_STATE = 0;
	private static final int VAR_STATE = 1;
	private static final int END_VAR_STATE = 2;
	private static final int EQUAL_STATE = 3;
	private static final int FINISH_STATE = 4;
	private static final int ERROR_STATE = 5;

	private IToken token;
	private StringBuilder buffer = new StringBuilder();
	protected IToken defaultToken;

	public AutomakeMacroDefinitionRule(IToken token, IToken defaultToken) {
		this.token = token;
		this.defaultToken = defaultToken;
	}

	@Override
	public IToken getSuccessToken() {
		return token;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		buffer.setLength(0);
		int c;
		int state = INIT_STATE;

		if (resume)
			scanToBeginOfLine(scanner);

		for (c = scanner.read(); c != ICharacterScanner.EOF; c = scanner.read()) {
			switch (state) {
			case INIT_STATE:
				if (c != '\n' && Character.isWhitespace((char) c)) {
					break;
				}
				if (isValidCharacter(c)) {
					state = VAR_STATE;
				} else {
					state = ERROR_STATE;
				}
				break;
			case VAR_STATE:
				if (isValidCharacter(c)) {
					break;
				}
			case END_VAR_STATE:
				if (Character.isWhitespace((char) c)) {
					state = END_VAR_STATE;
				} else if (c == ':' || c == '+') {
					state = EQUAL_STATE;
				} else if (c == '=') {
					state = FINISH_STATE;
				} else {
					//						if (state == END_VAR_STATE) {
					//							scanner.unread(); // Return back to the space
					//						}
					state = ERROR_STATE;
				}
				break;
			case EQUAL_STATE:
				if (c == '=') {
					state = FINISH_STATE;
				} else {
					state = ERROR_STATE;
				}
				break;
			case FINISH_STATE:
				break;
			default:
				break;
			}
			if (state >= FINISH_STATE) {
				break;
			}
			buffer.append((char) c);
		}

		if (state == FINISH_STATE) {
			scanToEndOfLine(scanner);
			return token;
		}

		boolean debug = true;
		if (debug) {
			//        	System.out.println("This should be a 'c':  " + peek(scanner));
			//                System.out.println("This is what's in the **REST OF** the buffer:");
			//                int count = 0;
			//                for (int c = scanner.read(); c != ICharacterScanner.EOF; c = scanner.read()) {
			//                        System.out.println((char) c);
			//                        count++;
			//                }
			//                // Unread what we just displayed
			//                for (int i = 0; i < count; i++) {
			//                        scanner.unread();
			//                }
		}

		if (defaultToken.isUndefined()) {
			// If c is EOF, we've read it and broken out of the for loop above,
			// but we need to unread it since it got read but not put into the
			// buffer
			if (state == ERROR_STATE || c == ICharacterScanner.EOF)
				scanner.unread();
			unreadBuffer(scanner);
			debug = true;
			if (debug) {
				//            	System.out.println("This should be an 'i':  " + peek(scanner));
				//                    System.out.println("We've supposedly just unread the entire buffer.  Here it is:");
				//                    int count = 0;
				//                    for (int c = scanner.read(); c != ICharacterScanner.EOF; c = scanner.read()) {
				//                            System.out.println((char) c);
				//                            count++;
				//                    }
				//                    // Unread what we just displayed
				//                    for (int i = 0; i < count + 1; i++) {
				//                            scanner.unread();
				//                    }
				//                    System.out.println("... just to be safe, here's the first character:  " + peek(scanner));
			}

		}

		return Token.UNDEFINED;
	}

	public char peek(ICharacterScanner scanner) {
		char c = (char) scanner.read();
		scanner.unread();
		return c;
	}

	@Override
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
				if (c == delimiters[i][0] && sequenceDetected(scanner, delimiters[i])) {
					return;
				}
			}
		}
	}

	private void scanToBeginOfLine(ICharacterScanner scanner) {
		while (scanner.getColumn() != 0) {
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
				for (; i > 0; i--) {
					scanner.unread();
				}
				return false;
			}
		}

		return true;
	}

	protected boolean isValidCharacter(int c) {
		char c0 = (char) c;
		return Character.isLetterOrDigit(c0) || (c0 == '_') || (c0 == '-') || (c0 == '@') || (c0 == '+') || (c0 == '$')
				|| (c0 == '(') || (c0 == ')');
	}

}
