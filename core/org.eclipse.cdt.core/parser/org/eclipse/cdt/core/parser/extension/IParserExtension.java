/*******************************************************************************
 * Copyright (c) 2000 - 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.extension;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.internal.core.parser.DeclarationWrapper;
import org.eclipse.cdt.internal.core.parser.IParserData;
import org.eclipse.cdt.internal.core.parser.Parser;


/**
 * @author jcamelon
 */
public interface IParserExtension {
	
	public boolean isValidCVModifier( ParserLanguage language, int tokenType );
	public ASTPointerOperator getPointerOperator( ParserLanguage language, int tokenType );
	
	public boolean isValidUnaryExpressionStart( int tokenType );
	public IASTExpression parseUnaryExpression( IASTScope scope, IParserData data, CompletionKind kind, KeywordSetKey key );
	
	public boolean isValidRelationalExpressionStart( ParserLanguage language, int tokenType );
	public IASTExpression parseRelationalExpression( IASTScope scope, IParserData data, CompletionKind kind, KeywordSetKey key, IASTExpression lhsExpression );
	/**
	 * @param i
	 * @return
	 */
	public boolean canHandleDeclSpecifierSequence(int tokenType );
	public interface IDeclSpecifierExtensionResult
	{
		public IToken			  getFirstToken();
		public IToken			  getLastToken();
		public Parser.Flags 	  getFlags();
	}
	
	/**
	 * @param parser
	 * @param flags
	 * @param sdw
	 * @param key TODO
	 * @return TODO
	 */
	public IDeclSpecifierExtensionResult parseDeclSpecifierSequence(IParserData parser, Parser.Flags flags, DeclarationWrapper sdw, CompletionKind kind, KeywordSetKey key );
	/**
	 * @param i
	 * @return
	 */
	public boolean canHandleCDesignatorInitializer(int tokenType);
	/**
	 * @param parserData
	 * @param scope TODO
	 * @return
	 */
	public IASTDesignator parseDesignator(IParserData parserData, IASTScope scope);
}
