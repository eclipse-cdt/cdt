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

	public Object translationUnitBegin();
	public void translationUnitEnd(Object unit);
	
	public void inclusionBegin(String includeFile);
	public void inclusionEnd();
	public void macro(String macroName);
	
	public Object simpleDeclarationBegin(Object Container);
	public void simpleDeclarationEnd(Object declaration);
	
	public Object parameterDeclarationBegin( Object Container ); 
	public void  parameterDeclarationEnd( Object declaration ); 
	
	public void simpleDeclSpecifier(Object Container, Token specifier);
	
	public void nameBegin(Token firstToken);
	public void nameEnd(Token lastToken);
	
	public Object declaratorBegin(Object container);
	public void declaratorId(Object declarator);
	public Object argumentsBegin( Object declarator );
	public void argumentsEnd(Object parameterDeclarationClause);
	public void declaratorEnd(Object declarator);
	
	public void functionBodyBegin();
	public void functionBodyEnd();
	
	public Object classSpecifierBegin(Object container, Token classKey);
	public void classSpecifierName(Object classSpecifier);
	public void classSpecifierEnd(Object classSpecifier);
	
	public Object	baseSpecifierBegin( Object containingClassSpec );
	public void	baseSpecifierName( Object baseSpecifier );
	public void 	baseSpecifierVisibility( Object baseSpecifier, Token visibility );
	public void 	baseSpecifierVirtual( Object baseSpecifier, boolean virtual );
	public void  	baseSpecifierEnd( Object baseSpecifier );
	
	public void expressionOperator(Token operator) throws Exception;
	public void expressionTerminal(Token terminal) throws Exception;
}
