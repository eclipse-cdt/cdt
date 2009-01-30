/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;


/**
 * Represents a parser that can be used by BaseExtensibleLanguage.
 * 
 * @author Mike Kucera
 */
public interface IParser extends ITokenCollector {
	
	/**
	 * Options used by implementations of IParser. Some of the options
	 * may be duplicates of the options in ILanguage.
	 * @see ILanguage
	 */
	public enum Options {
		
		/**
		 * The LR parsers do not actually skip the parsing of function bodies, 
		 * but this option does have the effect of not generating AST nodes
		 * for function bodies.
		 * 
		 * TODO Implement this
		 */
		//OPTION_SKIP_FUNCTION_BODIES,
		
		/**
		 * Instructs the parser not to create AST nodes for expressions 
		 * within aggregate initializers when they do not contain names.
		 * 
		 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=253690
		 */
		OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS,
	}
	
	
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
	 * @param tu An IASTTranslationUnit instance that will have its declarations filled in.
	 * @param options a Set of parser options, use an EnumSet
	 * @return a completion node if a completion token is encountered during the parser, null otherwise.
	 * @throws NullPointerException if either parameter is null
	 * @see EnumSet
	 */
	public IASTCompletionNode parse(IASTTranslationUnit tu, Set<Options> options);
	
	
	/**
	 * Returns the result of a secondary parser.
	 */
	public IASTNode getSecondaryParseResult();
	
}
