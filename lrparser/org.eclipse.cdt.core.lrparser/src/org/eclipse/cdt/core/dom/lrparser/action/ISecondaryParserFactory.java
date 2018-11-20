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
package org.eclipse.cdt.core.dom.lrparser.action;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.lrparser.ISecondaryParser;

/**
 * Some ambiguities are resolved by spawning a secondary parser
 * to re-parse a sequence of tokens using a modified grammar.
 * This factory is used to create these secondary parsers.
 *
 * @author Mike Kucera
 */
public interface ISecondaryParserFactory {

	/**
	 * Get the parser that will recognize expressions.
	 */
	ISecondaryParser<IASTExpression> getExpressionParser(ITokenStream stream, Map<String, String> properties);

	/**
	 * Expression parser that does not recognize cast expressions,
	 * used to disambiguate casts.
	 */
	ISecondaryParser<IASTExpression> getNoCastExpressionParser(ITokenStream stream, Map<String, String> properties);

	/**
	 * Expression parser that treats all sizeof and typeid expressions
	 * as unary expressions.
	 */
	ISecondaryParser<IASTExpression> getSizeofExpressionParser(ITokenStream stream, Map<String, String> properties);

}
