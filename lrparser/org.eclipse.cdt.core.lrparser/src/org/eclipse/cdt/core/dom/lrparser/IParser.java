/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;


/**
 * Represents a parser that can be used by BaseExtensibleLanguage.
 * 
 * @author Mike Kucera
 */
public interface IParser extends ITokenCollector {
	
	/**
	 * Performs the actual parse.
	 * 
	 * The given translation unit is assumed to not have any children, during the parse
	 * it will have its declaration fields filled in, resulting in a complete AST.
	 * 
	 * If there were any errors during the parse these will be represented in the
	 * AST as problem nodes.
	 * 
	 * If the parser encounters a completion token then a completion node
	 * is returned, null is returned otherwise.
	 * 
	 * @param tu An IASTTranslationUnit instance that will have its declarators filled in.
	 * @return a completion node if a completion token is encountered during the parser, null otherwise.
	 */
	public IASTCompletionNode parse(IASTTranslationUnit tu);
	
	
	/**
	 * Returns the result of a secondary parser.
	 */
	public IASTNode getSecondaryParseResult();
	
}
