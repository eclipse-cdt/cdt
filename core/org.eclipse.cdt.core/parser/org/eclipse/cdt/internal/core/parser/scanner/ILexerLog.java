/*******************************************************************************
 * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.IProblem;

/**
 * Interface between the lexer and the preprocessor for picking up warnings and comments.
 * @since 5.0
 */
public interface ILexerLog {
	ILexerLog NULL = new ILexerLog() {
		@Override
		public void handleComment(boolean isBlockComment, int offset, int endOffset, AbstractCharArray input) {
		}

		@Override
		public void handleProblem(int problemID, char[] info, int offset, int endOffset) {
		}
	};

	/**
	 * A problem has been detected
	 * @param problemID id as defined in {@link IProblem}
	 * @param info additional info as required for {@link IProblem}.
	 * @param offset The offset of the problem in the source of the lexer.
	 * @param endOffset end offset of the problem in the source of the lexer.
	 */
	void handleProblem(int problemID, char[] info, int offset, int endOffset);

	/**
	 * A comment has been detected
	 * @param isBlockComment <code>true</code> for block-comments, <code>false</code> for line-comments.
	 * @param source the input of the lexer.
	 * @param offset the offset where the comment starts
	 * @param endOffset the offset where the comment ends
	 * @param input the contents of the file being parsed
	 */
	void handleComment(boolean isBlockComment, int offset, int endOffset, AbstractCharArray input);
}
