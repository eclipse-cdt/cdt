/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.newparser;

import java.util.List;

public interface IParserCallback {

	public void beginTranslationUnit();
	public void endTranslationUnit();
	
	public void beginInclusion(String includeFile);
	public void endInclusion();
	
	public void macro(String macroName);
	
	public void beginSimpleDeclaration(Token firstToken);
	public void endSimpleDeclaration(Token lastToken);
	
	public void declSpecifier(Token specifier);
	
	public void beginDeclarator();
	public void declaratorId(Token id);
	public void beginArguments();
	public void endArguments();
	public void endDeclarator();
	
	public void beginFunctionBody();
	public void endFunctionBody();
	
	public void beginClass(String classKey, Token name);
	public void endClass();
	
	public void expressionOperator(Token operator) throws Exception;
	public void expressionTerminal(Token terminal) throws Exception;
}
