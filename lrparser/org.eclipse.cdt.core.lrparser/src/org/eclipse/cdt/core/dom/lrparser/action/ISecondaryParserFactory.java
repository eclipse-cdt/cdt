/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action;

import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;


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
	IParser getExpressionParser(IParserActionTokenProvider parser);
	
	
	/**
	 * Expression parser that does not recognize cast expressions,
	 * used to disambiguate casts. 
	 */
	IParser getNoCastExpressionParser(IParserActionTokenProvider parser);
	
	
	/**
	 * Expression parser that treats all sizeof and typeid expressions
	 * as unary expressions.
	 */
	IParser getSizeofExpressionParser(IParserActionTokenProvider parser);
	
}
