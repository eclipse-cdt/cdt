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

import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.newparser.NullParserCallback;

public class NewModelBuilder extends NullParserCallback {

	private TranslationUnit translationUnit;
	private CElement currentElement;
	private CElement underConstruction;
	private String declSpecifier;
	private String declaratorId;
	private boolean isFunction;
	
	public NewModelBuilder(TranslationUnit tu) {
		translationUnit = tu;
		currentElement = tu;
	}
	
	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginClass(String, List)
	 */
	public void beginClass(String classKey, List className) {
		beginClass(classKey, (String)className.get(0));
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginClass(String, String)
	 */
	public void beginClass(String classKey, String className) {
		int kind;
		if (classKey.equals("class"))
			kind = ICElement.C_CLASS;
		else if (classKey.equals("struct"))
			kind = ICElement.C_STRUCT;
		else
			kind = ICElement.C_UNION;
		
		Structure elem = new Structure(translationUnit, kind, className);
		currentElement.addChild(elem);
		currentElement = elem;
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endClass()
	 */
	public void endClass() {
		currentElement = (CElement)currentElement.getParent();
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginDeclarator()
	 */
	public void beginDeclarator() {
		declaratorId = null;
		isFunction = false;
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#declSpecifier(String)
	 */
	public void declSpecifier(String specifier) {
		declSpecifier = specifier;
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#declaratorId(List)
	 */
	public void declaratorId(List name) {
		declaratorId = (String)name.get(0);
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginArguments()
	 */
	public void beginArguments() {
		isFunction = true;
		
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endDeclarator()
	 */
	public void endDeclarator() {
		ICElement elem = null;
		
		if (isFunction) {
			if (currentElement instanceof TranslationUnit) {
				elem = new Function(currentElement, declaratorId);
			} else if (currentElement instanceof Structure) {
				elem = new Function(currentElement, declaratorId);
			}
		} else {
			if (currentElement instanceof TranslationUnit) {
				elem = new Variable(currentElement, declaratorId);

			} else if (currentElement instanceof Structure) {
				elem = new Field(currentElement, declaratorId);
			}
		}
		
		if (elem != null)
			currentElement.addChild(elem);
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#inclusion(String)
	 */
	public void inclusion(String includeFile) {
		Include elem = new Include(translationUnit, includeFile);
		translationUnit.addChild(elem);
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#macro(String)
	 */
	public void macro(String macroName) {
		Macro elem = new Macro(translationUnit, macroName);
		translationUnit.addChild(elem);
	}

}
