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
package org.eclipse.cdt.internal.core.model;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.parser.IParserCallback;
import org.eclipse.cdt.internal.core.parser.Token;
import org.eclipse.cdt.internal.core.parser.util.DeclSpecifier;
import org.eclipse.cdt.internal.core.parser.util.DeclarationSpecifier;
import org.eclipse.cdt.internal.core.parser.util.Name;

public class NewModelBuilder implements IParserCallback {

	private TranslationUnitWrapper translationUnit = new TranslationUnitWrapper();
	
	
	public NewModelBuilder(TranslationUnit tu) {
		translationUnit.setElement( tu );
	}
	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginTranslationUnit()
	 */
	public Object translationUnitBegin() {
		return translationUnit;
	}
	
	
	private Token classKey;
	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginClass(String, String)
	 */
	public Object classSpecifierBegin(Object container, Token classKey) {
		
		SimpleDeclarationWrapper c = (SimpleDeclarationWrapper)container; 
		
		int kind;
		switch (classKey.getType()) {
			case Token.t_class:
				kind = ICElement.C_CLASS;
				break;
			case Token.t_struct:
				kind = ICElement.C_STRUCT;
				break;
			default:
				kind = ICElement.C_UNION;
		}
		this.classKey = classKey;
		
		Structure elem = new Structure( c.getParent(), kind, null );
		c.getParent().addChild(elem); 
		return new SimpleDeclarationWrapper( elem ); 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classSpecifierName() 
	 */
	public void classSpecifierName(Object classSpecifier) {

		SimpleDeclarationWrapper container = (SimpleDeclarationWrapper)classSpecifier; 
		String name = currName.toString(); 
		Structure elem = ((Structure)container.getElement());
		elem.setElementName( name );
		elem.setIdPos(currName.getEndOffset(), name.length());
		elem.setPos(currName.getEndOffset(), name.length());
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endClass()
	 */
	public void classSpecifierEnd(Object classSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginDeclarator()
	 */
	public Object declaratorBegin(Object container) {
		DeclarationSpecifier.Container declSpec = (DeclarationSpecifier.Container)container;
		List declarators = declSpec.getDeclarators();
		Declarator declarator =new Declarator();
		declarators.add( declarator );  
		return declarator; 
	}


	private int startIdPos;
	private int idLength;

	private CElement elem;
		
	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endDeclarator()
	 */
	public void declaratorEnd( Object declarator) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginFunctionBody()
	 */
	public void functionBodyBegin() {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#macro(String)
	 */
	public void macro(String macroName, int offset) {
		Macro elem = new Macro((TranslationUnit)translationUnit.getElement(), macroName);
		elem.setIdPos(offset, macroName.length());
		elem.setPos(offset, macroName.length());

		((TranslationUnit)translationUnit.getElement()).addChild(elem);
		
	}

	private int startPos;

	/**
	 * @see 
org.eclipse.cdt.internal.core.newparser.IParserCallback#beginSimpleDeclaration(Token)
	 */
	public Object simpleDeclarationBegin(Object container) {
		ICElementWrapper wrapper = (ICElementWrapper)container; 
		Object parent = wrapper.getElement();
		SimpleDeclarationWrapper result = new SimpleDeclarationWrapper();
		if( wrapper instanceof SimpleDeclarationWrapper )
			result.setParent( (CElement)wrapper.getElement() );
		else if ( wrapper instanceof TranslationUnitWrapper )
			result.setParent( (TranslationUnit)wrapper.getElement());
		return result;  	
	}
	
	

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginInclusion(String)
	 */
	public void inclusionBegin(String includeFile, int offset) {
		Include elem = new Include(((TranslationUnit)translationUnit.getElement()), includeFile);
		((TranslationUnit)translationUnit.getElement()).addChild(elem);
		elem.setIdPos(offset, includeFile.length());
		elem.setPos(offset, includeFile.length());

	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endInclusion()
	 */
	public void inclusionEnd() {
	}
	
	private Name currName;
	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameBegin(Token firstToken) {
		currName = new Name(firstToken);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameEnd(Token lastToken) {
		currName.setEnd(lastToken);
	}


	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationEnd(java.lang.Object)
	 */
	public void simpleDeclarationEnd(Object declaration) {
		SimpleDeclarationWrapper wrapper = (SimpleDeclarationWrapper)declaration; 
		wrapper.createElements();
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclSpecifier(java.lang.Object, org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclSpecifier(Object declSpec, Token specifier) { 
		DeclSpecifier declSpecifier = (DeclSpecifier)declSpec;  
		declSpecifier.setType( specifier ); 				
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorId(java.lang.Object)
	 */
	public void declaratorId(Object declarator) {
		Declarator decl = (Declarator)declarator; 
		decl.setName( currName ); 
	}


	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsBegin(java.lang.Object)
	 */
	public Object argumentsBegin(Object declarator) {
		Declarator decl = (Declarator)declarator;
		List parameterDeclarationClause = new LinkedList();
		decl.setParameterDeclarationClause( parameterDeclarationClause );
		return parameterDeclarationClause;  
		
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#baseSpecifierBegin(java.lang.Object)
	 */
	public Object baseSpecifierBegin(Object containingClassSpec) {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#baseSpecifierEnd(java.lang.Object)
	 */
	public void baseSpecifierEnd(Object baseSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#baseSpecifierName(java.lang.Object)
	 */
	public void baseSpecifierName(Object baseSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#baseSpecifierVirtual(java.lang.Object, boolean)
	 */
	public void baseSpecifierVirtual(Object baseSpecifier, boolean virtual) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#baseSpecifierVisibility(java.lang.Object, org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void baseSpecifierVisibility(
		Object baseSpecifier,
		Token visibility) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionOperator(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionOperator(Token operator) throws Exception {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionTerminal(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionTerminal(Token terminal) throws Exception {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyEnd()
	 */
	public void functionBodyEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#parameterDeclarationBegin(java.lang.Object, org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public Object parameterDeclarationBegin(
		Object container ) {
		List parameterDeclarationClause = (List)container; 
		Parameter p = new Parameter();
		parameterDeclarationClause.add( p );
		return p; 
		
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#parameterDeclarationEnd(java.lang.Object)
	 */
	public void parameterDeclarationEnd(Object declaration) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#translationUnitEnd(java.lang.Object)
	 */
	public void translationUnitEnd(Object unit) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsEnd()
	 */
	public void argumentsEnd(Object parameterDeclarationClause) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorAbort(java.lang.Object, java.lang.Object)
	 */
	public void declaratorAbort(Object container, Object declarator) {
		DeclarationSpecifier.Container declSpec = (DeclarationSpecifier.Container)container;
		Declarator toBeRemoved =(Declarator)declarator; 
		declSpec.removeDeclarator( toBeRemoved ); 
		toBeRemoved = null; 
		currName = null;   		
	}

}
