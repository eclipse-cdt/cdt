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
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginClass(String, String)
	 */
	public void classBegin(String classKey, Token className) {
		int kind;
		if (classKey.equals("class"))
			kind = ICElement.C_CLASS;
		else if (classKey.equals("struct"))
			kind = ICElement.C_STRUCT;
		else
			kind = ICElement.C_UNION;
		
		String name = "";
		if (className != null) {
			try {
				name = Parser.generateName(className);
			} catch (Exception e) {
			}
		}
		
		Structure elem = new Structure(translationUnit, kind, name);
		
		currentElement.addChild(elem);
		elem.setIdPos(className.getOffset() - 2, className.getImage().length());
		elem.setPos(className.getOffset(), className.getImage().length());
		currentElement = elem;
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endClass()
	 */
	public void classEnd() {
		currentElement = (CElement)currentElement.getParent();
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginDeclarator()
	 */
	public void declaratorBegin() {
		if (!inArguments) {
			declaratorId = null;
			isFunction = false;
		}
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
	public void declaratorEnd() {
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
	public void simpleDeclarationBegin(Token firstToken) {
		if (inclusionDepth == 0)
			startPos = firstToken.getOffset();
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

}
