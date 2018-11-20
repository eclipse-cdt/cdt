/*******************************************************************************
 *  Copyright (c) 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

import lpg.lpgjavaruntime.IToken;

@SuppressWarnings("restriction")
public final class ParserUtil {

	private ParserUtil() {
	}

	public static int offset(IToken token) {
		return token.getStartOffset();
	}

	public static int offset(IASTNode node) {
		return ((ASTNode) node).getOffset();
	}

	public static int length(IToken token) {
		return endOffset(token) - offset(token);
	}

	public static int length(IASTNode node) {
		return ((ASTNode) node).getLength();
	}

	public static int endOffset(IASTNode node) {
		return offset(node) + length(node);
	}

	public static int endOffset(IToken token) {
		return token.getEndOffset();
	}

	public static void setOffsetAndLength(IASTNode node, IToken token) {
		((ASTNode) node).setOffsetAndLength(offset(token), length(token));
	}

	public static void setOffsetAndLength(IASTNode node, int offset, int length) {
		((ASTNode) node).setOffsetAndLength(offset, length);
	}

	public static void setOffsetAndLength(IASTNode node, IASTNode from) {
		setOffsetAndLength(node, offset(from), length(from));
	}

	public static boolean isSameName(IASTName name1, IASTName name2) {
		return Arrays.equals(name1.getLookupKey(), name2.getLookupKey());
	}

	/**
	 * Allows simple pattern match testing of lists of tokens.
	 *
	 * @throws NullPointerException if source or pattern is null
	 */
	public static boolean matchTokens(List<IToken> source, ITokenMap tokenMap, Integer... pattern) {
		if (source.size() != pattern.length) // throws NPE if either parameter is null
			return false;

		for (int i = 0, n = pattern.length; i < n; i++) {
			if (tokenMap.mapKind(source.get(i).getKind()) != pattern[i].intValue())
				return false;
		}
		return true;
	}

	/**
	 * Finds the tokens in the given list that are between startOffset and endOffset.
	 * Note, the offsets have to be exact.
	 */
	public static List<IToken> tokenOffsetSubList(List<IToken> tokens, int startOffset, int endOffset) {
		int first = 0, last = 0;
		int i = 0;
		for (IToken t : tokens) {
			if (offset(t) == startOffset) {
				first = i;
			}
			if (endOffset(t) == endOffset) {
				last = i;
				break;
			}
			i++;
		}
		return tokens.subList(first, last + 1);
	}

}
