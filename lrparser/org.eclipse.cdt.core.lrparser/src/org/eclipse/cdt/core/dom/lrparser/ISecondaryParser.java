/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
package org.eclipse.cdt.core.dom.lrparser;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;

import lpg.lpgjavaruntime.IToken;

public interface ISecondaryParser<N extends IASTNode> extends IParser<N> {

	/**
	 * Set the list of tokens that will be parsed.
	 *
	 * The given list does not need to contain dummy and EOF tokens,
	 * these will be added automatically.
	 *
	 * This method causes any tokens already contained in the parser
	 * to be removed.
	 *
	 * This method is mainly used by secondary parsers that are called
	 * from a main parser.
	 *
	 * @throws NullPointerException if tokens is null
	 */
	public void setTokens(List<IToken> tokens);
}
