/*******************************************************************************
 * Copyright (c) 2020 Sergey Vladimirov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sergey Vladimirov - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.metrics;

import org.eclipse.cdt.codan.checkers.CodanCheckersActivator;
import org.eclipse.cdt.codan.core.cxx.model.AbstractAstFunctionChecker;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.model.ProblemPreference;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.util.IntArray;

public class LinesOfCodeChecker extends AbstractAstFunctionChecker {

	public static final String ER_LINES_OF_CODE_EXCEEDED_ID = "org.eclipse.cdt.codan.internal.checkers.LinesOfCodeExceededProblem"; //$NON-NLS-1$

	/**
	 * @return positions of single <tt>\r</tt>, <tt>\n</tt> or position of <tt>\n</tt> in <tt>\r\n</tt> pair
	 **/
	private static int[] findLineBreaks(String text) {
		IntArray result = new IntArray();
		final int length = text.length();
		for (int i = 0; i < length; i++) {
			if (text.charAt(i) == '\n') {
				result.add(i);
			} else if (text.charAt(i) == '\r' && i != length - 1 && text.charAt(i + 1) == '\n') {
				result.add(i + 1);
				i = i + 1;
				continue;
			} else if (text.charAt(i) == '\r') {
				result.add(i);
			}
		}
		return result.toArray();
	}

	private static final String trimRight(String text) {
		int newLength = text.length();
		char lastChar = text.charAt(newLength - 1);
		while (Character.isWhitespace(lastChar)) {
			newLength--;
			if (newLength == 0) {
				return ""; //$NON-NLS-1$
			}
			lastChar = text.charAt(newLength - 1);
		}
		return text.substring(0, newLength);
	}

	@ProblemPreference(key = "countFuncBodyTokens", nls = MetricCheckersMessages.class)
	private boolean countFuncBodyTokens = false;

	@ProblemPreference(key = "maxLines", nls = MetricCheckersMessages.class)
	private int maxLines = 50;

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		super.addPreferencesForAnnotatedFields(problem);
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		super.loadPreferencesForAnnotatedFields(getProblemById(ER_LINES_OF_CODE_EXCEEDED_ID, getFile()));
		super.processAst(ast);
	}

	@Override
	protected void processFunction(IASTFunctionDefinition func) {
		final IASTStatement body = func.getBody();

		final IASTFileLocation bodyFileLocation = body.getFileLocation();
		final int startingLineNumber = bodyFileLocation.getStartingLineNumber();
		final int endingLineNumber = bodyFileLocation.getEndingLineNumber();
		final int totalBodyLines = endingLineNumber - startingLineNumber + 1;
		if (totalBodyLines <= maxLines) {
			// not need to check, not enough lines in function body even with empty lines and comments
			return;
		}

		// the biggest problem with line breaks that IToken doesn't have line property
		// thus we need to iterate over all tokens and check their offsets
		// and compare them with line break offsets
		String rawSignature = trimRight(body.getRawSignature());

		final int[] lineBreaksOffsets = findLineBreaks(rawSignature);
		if (lineBreaksOffsets.length == 0) {
			// single line function body
			return;
		}

		int nextLineBreakIndex = 0;
		int unusedLines = 0;

		IToken prevToken, token;
		try {
			prevToken = null;
			token = body.getSyntax();
		} catch (Exception exc) {
			CodanCheckersActivator.log(exc);
			return;
		}

		if (!countFuncBodyTokens && token.getType() == IToken.tLBRACE && token.getNext() != null) {
			// if function body start '{' is single on line (not counting function def.) then count line as unused
			if (nextLineBreakIndex < lineBreaksOffsets.length
					&& token.getOffset() < lineBreaksOffsets[nextLineBreakIndex]
					&& token.getNext().getOffset() > lineBreaksOffsets[nextLineBreakIndex]) {
				unusedLines++;
				nextLineBreakIndex++;
			}

			prevToken = token;
			token = token.getNext();
		}

		while (token != null
				&& !(!countFuncBodyTokens && token.getNext() == null && token.getType() == IToken.tRBRACE)) {
			if (token.getOffset() > lineBreaksOffsets[nextLineBreakIndex]) {
				unusedLines--;
				while (token.getOffset() > lineBreaksOffsets[nextLineBreakIndex]) {
					unusedLines++;
					nextLineBreakIndex++;

					if (nextLineBreakIndex == lineBreaksOffsets.length) {
						break;
					}
				}
			}

			if (nextLineBreakIndex == lineBreaksOffsets.length) {
				break;
			}

			prevToken = token;
			token = token.getNext();
		}

		if (!countFuncBodyTokens && token != null && token.getNext() == null && token.getType() == IToken.tRBRACE
				&& prevToken != null) {
			// if function body end '}' is single on line then count line as unused
			if (prevToken.getOffset() < lineBreaksOffsets[nextLineBreakIndex]
					&& token.getOffset() > lineBreaksOffsets[nextLineBreakIndex]) {
				unusedLines++;
			}
		}

		final int usedLines = totalBodyLines - unusedLines;
		if (usedLines > maxLines) {
			reportProblem(ER_LINES_OF_CODE_EXCEEDED_ID, func, usedLines, maxLines);
		}
	}
}
