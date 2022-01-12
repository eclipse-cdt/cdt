/*******************************************************************************
 * Copyright (c) 2012, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.util;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.util.TextUtil;

/**
 * Collection of helper methods for common operations on AST nodes.
 */
public class ASTNodes {
	/** Not instantiatable. */
	private ASTNodes() {
	}

	/**
	 * Returns the offset of the given node, or -1 if the node is not part of the translation
	 * unit file or doesn't have a file-location.
	 * @see IASTNode#getFileLocation()
	 */
	public static int offset(IASTNode node) {
		if (!node.isPartOfTranslationUnitFile())
			return -1;
		IASTFileLocation nodeLocation = node.getFileLocation();
		return nodeLocation != null ? nodeLocation.getNodeOffset() : -1;
	}

	/**
	 * Returns the end offset of the given node, or -1 if the node is not part of the translation
	 * unit file or doesn't have a file-location.
	 * @see IASTNode#getFileLocation()
	 */
	public static int endOffset(IASTNode node) {
		if (!node.isPartOfTranslationUnitFile())
			return -1;
		IASTFileLocation nodeLocation = node.getFileLocation();
		return nodeLocation != null ? nodeLocation.getNodeOffset() + nodeLocation.getNodeLength() : -1;
	}

	/**
	 * Returns the 1-based starting line number of the given node, or 0 if the node is not part of
	 * the translation unit file or doesn't have a file-location.
	 * @see IASTNode#getFileLocation()
	 */
	public static int getStartingLineNumber(IASTNode node) {
		if (!node.isPartOfTranslationUnitFile())
			return 0;
		IASTFileLocation nodeLocation = node.getFileLocation();
		return nodeLocation != null ? nodeLocation.getStartingLineNumber() : 0;
	}

	/**
	 * Returns the 1-based ending line number of the given node, or 0 if the node is not part of
	 * the translation unit file or doesn't have a file-location.
	 * @see IASTNode#getFileLocation()
	 */
	public static int getEndingLineNumber(IASTNode node) {
		if (!node.isPartOfTranslationUnitFile())
			return 0;
		IASTFileLocation nodeLocation = node.getFileLocation();
		return nodeLocation != null ? nodeLocation.getEndingLineNumber() : 0;
	}

	/**
	 * Returns the offset of the beginning of the next line after the node, or the end-of-file
	 * offset if there is no line delimiter after the node.
	 */
	public static int skipToNextLineAfterNode(String text, IASTNode node) {
		return TextUtil.skipToNextLine(text, endOffset(node));
	}

	/**
	 * Returns the whitespace preceding the given node. The newline character in not considered
	 * whitespace for the purpose of this method.
	 */
	public static String getPrecedingWhitespaceInLine(String text, IASTNode node) {
		int offset = offset(node);
		if (offset >= 0) {
			int i = offset;
			while (--i >= 0) {
				char c = text.charAt(i);
				if (c == '\n' || !Character.isWhitespace(c))
					break;
			}
			i++;
			return text.substring(i, offset);
		}
		return ""; //$NON-NLS-1$
	}
}
