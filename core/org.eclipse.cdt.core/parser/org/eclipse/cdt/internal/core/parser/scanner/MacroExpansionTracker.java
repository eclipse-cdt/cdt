/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * Collects information while macro expansion is performed.
 */
public class MacroExpansionTracker {
	public class MacroInfo {
		private TokenList fMacroCall = new TokenList();
		private ArrayList<TokenList> fArguments = new ArrayList<>();

		public MacroInfo(Token identifier) {
			fMacroCall.append(identifier);
		}

		public void setArgument(TokenList tokenList) {
			fArguments.add(tokenList);
		}
	}

	private final int fStepToTrack;

	private int fStepCount;
	private String fPreStep;
	private ReplaceEdit fReplacement;
	private IMacroBinding fMacroDefinition;

	private char[] fInput;
	private String fReplacementText = ""; //$NON-NLS-1$
	private LinkedList<MacroInfo> fMacroStack = new LinkedList<>();

	private IToken fReplaceFrom;
	private IToken fReplaceTo;

	public MacroExpansionTracker(int stepToTrack) {
		fStepToTrack = stepToTrack;
	}

	/**
	 * Returns whether the requested step has already been encountered.
	 */
	public boolean isDone() {
		return fStepCount > fStepToTrack;
	}

	/**
	 * Returns whether we are currently looking at the requested step.
	 */
	public boolean isRequestedStep() {
		return fStepCount == fStepToTrack;
	}

	/**
	 * Returns the total amount of steps encountered so far.
	 */
	public int getStepCount() {
		return fStepCount;
	}

	/**
	 * Returns the code as it looks like just before performing the step that was tracked.
	 */
	public String getCodeBeforeStep() {
		return fPreStep;
	}

	/**
	 * Returns the replacement that represents the change by the step that was tracked.
	 */
	public ReplaceEdit getReplacement() {
		return fReplacement;
	}

	/**
	 * Returns the macro that is expanded in the step that was tracked.
	 */
	public IMacroBinding getExpandedMacro() {
		return fMacroDefinition;
	}

	/**
	 * Informs the tracker that macro expansion is started.
	 */
	void start(char[] input) {
		fInput = input;
	}

	/**
	 * Informs the tracker that the expansion is done.
	 * @param result the list of tokens after performing partial expansion up to the step that was
	 * tracked.
	 * @param endOffset the end offset of the input that was read from the lexer.
	 */
	void finish(TokenList result, int endOffset) {
		final char[] lexInput = fInput;
		if (!isDone()) {
			// special case we compute the entire expansion as one step, the result contains the
			// expanded text
			StringBuilder replacementText = new StringBuilder();
			toString(result, lexInput, replacementText, replacementText, replacementText);
			fPreStep = new String(lexInput);
			fReplacement = new ReplaceEdit(0, endOffset, replacementText.toString());
		} else {
			// the regular case the result contains the text before the step
			StringBuilder before = new StringBuilder();
			StringBuilder replace = new StringBuilder();
			StringBuilder after = new StringBuilder();
			toString(result, lexInput, before, replace, after);
			int offset = before.length();
			// workaround bug 220158
			final CharSequence csr = replace;
			final CharSequence csa = after;
			before.append(csr).append(csa);
			before.append(lexInput, endOffset, lexInput.length - endOffset);
			fPreStep = before.toString();
			fReplacement = new ReplaceEdit(offset, replace.length(), fReplacementText);
		}
	}

	/**
	 * There was no macro at the beginning of the input.
	 */
	void fail() {
		fPreStep = new String(fInput);
		fReplacement = new ReplaceEdit(0, 0, ""); //$NON-NLS-1$
	}

	private void toString(TokenList tokenList, char[] rootInput, StringBuilder before, StringBuilder replace,
			StringBuilder after) {
		StringBuilder buf = before;
		Token t = tokenList.first();
		if (t == null) {
			return;
		}
		Token l = null;
		Token n;
		for (; t != null; l = t, t = n) {
			n = (Token) t.getNext();
			if (l != null && MacroExpander.hasImplicitSpace(l, t)) {
				char[] input = getInputForSource(l.fSource, rootInput);
				if (input == null) {
					buf.append(' ');
				} else {
					final int from = l.getEndOffset();
					final int to = t.getOffset();
					buf.append(input, from, to - from);
				}
			}
			if (t == fReplaceFrom) {
				buf = replace;
			}
			char[] input = getInputForSource(t.fSource, rootInput);
			if (input == null) {
				buf.append(t.getCharImage());
			} else {
				buf.append(input, t.getOffset(), t.getLength());
			}
			if (t == fReplaceTo) {
				buf = after;
			}
		}
	}

	private char[] getInputForSource(Object source, char[] rootInput) {
		if (source instanceof MacroExpander) {
			return rootInput;
		}
		if (source instanceof PreprocessorMacro) {
			final PreprocessorMacro pm = (PreprocessorMacro) source;
			if (!pm.isDynamic()) {
				return pm.getExpansionImage();
			}
		}
		return null;
	}

	/**
	 * Informs the tracker that a function-style expansion is started.
	 * @param identifier the identifier token for the macro expansion.
	 */
	public void startFunctionStyleMacro(Token identifier) {
		fMacroStack.add(new MacroInfo(identifier));
	}

	/**
	 * All tokens defining a function-style macro expansion are reported.
	 */
	public void addFunctionStyleMacroExpansionToken(Token t) {
		fMacroStack.getLast().fMacroCall.append(t);
	}

	/**
	 * The expanded arguments for the function-style macro expansion are reported.
	 * @param tokenList the expanded argument, or <code>null</code> if it should not
	 * be expanded.
	 */
	public void setExpandedMacroArgument(TokenList tokenList) {
		fMacroStack.getLast().setArgument(tokenList);
	}

	/**
	 * Called for the requested step.
	 * @param macro the macro expanded in the requested step.
	 * @param replacement the replacement for the expansion.
	 * @param result a list to store the macro call with the arguments substituted in.
	 */
	public void storeFunctionStyleMacroReplacement(PreprocessorMacro macro, TokenList replacement, TokenList result) {
		MacroInfo minfo = fMacroStack.getLast();
		fMacroDefinition = macro;
		fReplaceFrom = minfo.fMacroCall.first();
		appendFunctionStyleMacro(result);
		fReplaceTo = result.last();
		StringBuilder buf = new StringBuilder();
		toString(replacement, fInput, buf, buf, buf);
		fReplacementText = buf.toString();
	}

	/**
	 * Append the current function-style macro with the arguments substituted.
	 */
	public void appendFunctionStyleMacro(TokenList result) {
		MacroInfo minfo = fMacroStack.getLast();
		boolean active = true;
		int nesting = -1;
		int pcount = 0;

		Token n;
		Token l = null;
		for (Token t = minfo.fMacroCall.first(); t != null; l = t, t = n) {
			n = (Token) t.getNext();
			switch (t.getType()) {
			case IToken.tLPAREN:
				if (active) {
					result.append(t);
				}
				// the first one sets nesting to zero.
				++nesting;
				if (nesting == 0) {
					if (pcount < minfo.fArguments.size()) {
						TokenList p = minfo.fArguments.get(pcount);
						if (p != null) {
							active = false;
							if (n != null && n.getType() != IToken.tCOMMA && n.getType() != IToken.tRPAREN) {
								MacroExpander.addSpacemarker(t, n, result);
								result.appendAll(p);
							}
						}
					}
				}
				break;

			case IToken.tRPAREN:
				if (!active && nesting == 0) {
					MacroExpander.addSpacemarker(l, t, result);
					active = true;
				}
				if (active) {
					result.append(t);
				}
				if (nesting > 0) {
					nesting--;
				}
				break;

			case IToken.tCOMMA:
				if (nesting == 0) {
					if (++pcount < minfo.fArguments.size()) {
						if (!active) {
							MacroExpander.addSpacemarker(l, t, result);
						}
						result.append(t);
						TokenList p = minfo.fArguments.get(pcount);
						active = p == null;
						if (!active) {
							if (n != null && n.getType() != IToken.tCOMMA && n.getType() != IToken.tRPAREN) {
								MacroExpander.addSpacemarker(t, n, result);
								result.appendAll(p);
							}
						}
					}
				} else if (active) {
					result.append(t);
				}
				break;

			default:
				if (active) {
					result.append(t);
				}
			}
		}
	}

	/**
	 * Informs the tracker that the function style macro has been expanded.
	 */
	public void endFunctionStyleMacro() {
		fStepCount++;
		fMacroStack.removeLast();
	}

	/**
	 * Called for the requested step
	 * @param macro the macro expanded in the requested step.
	 * @param identifier the token that gets replaced.
	 * @param replacement the replacement
	 * @param result a list to store the macro in.
	 */
	public void storeObjectStyleMacroReplacement(PreprocessorMacro macro, Token identifier, TokenList replacement,
			TokenList result) {
		fMacroDefinition = macro;
		fReplaceFrom = fReplaceTo = identifier;
		result.append(identifier);
		StringBuilder buf = new StringBuilder();
		toString(replacement, fInput, buf, buf, buf);
		fReplacementText = buf.toString();
	}

	/**
	 * Informs the tracker that an object style macro has been expanded.
	 */
	public void endObjectStyleMacro() {
		fStepCount++;
	}
}