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

public interface IParserCallback {

	public void translationUnitBegin();
	public void translationUnitEnd();
	
	public void inclusionBegin(String includeFile);
	public void inclusionEnd();
	
	public void macro(String macroName);
	
	public void simpleDeclarationBegin(Token firstToken);
	public void simpleDeclarationEnd(Token lastToken);
	
	public void simpleDeclSpecifier(Token specifier);
	
	public void nameBegin(Token firstToken);
	public void nameEnd(Token lastToken);
	
	public void declaratorBegin();
	public void declaratorId();
	public void argumentsBegin();
	public void argumentsEnd();
	public void declaratorEnd();
	
	public void functionBodyBegin();
	public void functionBodyEnd();
	
	public void classBegin(String classKey, Token name);
	public void classEnd();
	
	public void expressionOperator(Token operator) throws Exception;
	public void expressionTerminal(Token terminal) throws Exception;
}
