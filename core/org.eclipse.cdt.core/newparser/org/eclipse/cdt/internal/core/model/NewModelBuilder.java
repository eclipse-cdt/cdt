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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.newparser.NullParserCallback;
import org.eclipse.cdt.internal.core.newparser.Parser;
import org.eclipse.cdt.internal.core.newparser.Token;

public class NewModelBuilder extends NullParserCallback {

	private TranslationUnit translationUnit;
	private CElement currentElement;
	private CElement underConstruction;
	private String declSpecifier;
	private String declaratorId;
	private boolean isFunction;
	private int inclusionDepth = 0;
	private boolean inArguments = false;
	
	public NewModelBuilder(TranslationUnit tu) {
		translationUnit = tu;
		currentElement = tu;
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
		
		Structure elem = new Structure( translationUnit, kind, null );  
		currentElement.addChild(elem);
		return elem; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classSpecifierName() 
	 */
	public void classSpecifierName(Object classSpecifier) {

		String name = nameEndToken.getImage();
		Structure elem = ((Structure)classSpecifier);
		elem.setElementName( name );
		elem.setIdPos(nameEndToken.getOffset() - 2, nameEndToken.getImage().length());
		elem.setPos(nameEndToken.getOffset(), nameEndToken.getImage().length());
		currentElement = elem;

	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endClass()
	 */
	public void classSpecifierEnd(Object classSpecifier) {
		currentElement = (CElement)currentElement.getParent();
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginDeclarator()
	 */
	public Object declaratorBegin(Object container) {
		if (!inArguments) {
			declaratorId = null;
			isFunction = false;
		}
		return null; 
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#declSpecifier(String)
	 */
	public void declSpecifier(Token specifier) {
		declSpecifier = "";
		if (specifier != null) {
			try {
				declSpecifier = Parser.generateName(specifier);
			} catch (Exception e) {
			}
		}
	}

	private int startIdPos;
	private int idLength;

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#declaratorId(List)
	 */
	public void declaratorId(Token id) {
		if (!inArguments)
			try {
				declaratorId = Parser.generateName(id);
				startIdPos = id.getOffset() - 2;
				idLength = declaratorId.length();
			} catch (Exception e) {
				// Hard to see why we would get here
				declaratorId = "";
			}
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginArguments()
	 */
	public void argumentsBegin() {
		isFunction = true;
		inArguments = true;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endArguments()
	 */
	public void argumentsEnd() {
		inArguments = false;
	}

	private CElement elem;
		
	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endDeclarator()
	 */
	public void declaratorEnd( Object declarator) {
		elem = null;
		
		if (isFunction) {
			elem = new FunctionDeclaration(currentElement, declaratorId);
		} else {
			if (currentElement instanceof TranslationUnit) {
				elem = new Variable(currentElement, declaratorId);

			} else if (currentElement instanceof Structure) {
				elem = new Field(currentElement, declaratorId);
			}
		}
		
		if (elem != null) {
			elem.setIdPos(startIdPos, idLength);
			elem.setPos(startPos, idLength);
			currentElement.addChild(elem);
		}
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginFunctionBody()
	 */
	public void functionBodyBegin() {
		// Oops, the last function declaration was really supposed to be
		// a function definition.
		//((CElement)elem.getParent()).add
		elem = new Function(currentElement, declaratorId);
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#macro(String)
	 */
	public void macro(String macroName) {
		Macro elem = new Macro(translationUnit, macroName);
		translationUnit.addChild(elem);
	}

	private int startPos;

	/**
	 * @see 
org.eclipse.cdt.internal.core.newparser.IParserCallback#beginSimpleDeclaration(Token)
	 */
	public Object simpleDeclarationBegin(Object Container, Token firstToken) {
		if (inclusionDepth == 0)
			startPos = firstToken.getOffset();
		return null; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginInclusion(String)
	 */
	public void inclusionBegin(String includeFile) {
		++inclusionDepth;
		Include elem = new Include(translationUnit, includeFile);
		translationUnit.addChild(elem);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endInclusion()
	 */
	public void inclusionEnd() {
		--inclusionDepth;
	}

	private Token nameBeginToken;
	private Token nameEndToken;
	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameBegin(Token firstToken) {
		nameBeginToken = firstToken;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameEnd(Token lastToken) {
		nameEndToken = lastToken;
	}

}
