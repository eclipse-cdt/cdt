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
package org.eclipse.cdt.core.dom;

import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.internal.core.newparser.NullParserCallback;
import org.eclipse.cdt.internal.core.newparser.Token;

public class DOMBuilder extends NullParserCallback {

	private Stack nodes = new Stack();
	private TranslationUnit translationUnit;
	
	public TranslationUnit getTranslationUnit() {
		return translationUnit;
	}
	
	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginTranslationUnit()
	 */
	public void translationUnitBegin() {
		translationUnit = new TranslationUnit();
		nodes.push(translationUnit);
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endTranslationUnit()
	 */
	public void translationUnitEnd() {
		if (!nodes.empty())
			nodes.pop();
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginSimpleDeclaration()
	 */
	public void beginSimpleDeclaration() {
		if (!nodes.empty() && nodes.peek() instanceof TranslationUnit) {
			SimpleDeclaration simpleDeclaration = new SimpleDeclaration();
			simpleDeclaration.setParent((TranslationUnit)nodes.peek());
			nodes.push(simpleDeclaration);
		}
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endSimpleDeclaration()
	 */
	public void endSimpleDeclaration() {
		if (!nodes.empty() && nodes.peek() instanceof SimpleDeclaration)
			nodes.pop();
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#declSpecifier(String)
	 */
	public void declSpecifier(String specifier) {
		if (!nodes.empty() && nodes.peek() instanceof SimpleDeclaration) {
			SimpleTypeSpecifier simpleTypeSpecifier = new SimpleTypeSpecifier();
			simpleTypeSpecifier.setParent((SimpleDeclaration)nodes.peek());
			simpleTypeSpecifier.getName().add(specifier);
		}
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#declSpecifier(List)
	 */
	public void declSpecifier(List specifier) {
		if (!nodes.empty() && nodes.peek() instanceof SimpleDeclaration) {
			SimpleTypeSpecifier simpleTypeSpecifier = new SimpleTypeSpecifier();
			simpleTypeSpecifier.setParent((SimpleDeclaration)nodes.peek());
			simpleTypeSpecifier.getName().addAll(specifier);
		}
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginDeclarator()
	 */
	public void declaratorBegin() {
		if (!nodes.empty() && nodes.peek() instanceof SimpleDeclaration) {
			Declarator declarator = new Declarator();
			declarator.setParent((SimpleDeclaration)nodes.peek());
			nodes.push(declarator);
		}
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#declaratorId(List)
	 */
	public void declaratorId(List name) {
		if (!nodes.empty() && nodes.peek() instanceof Declarator)
			((Declarator)nodes.peek()).getName().addAll(name);
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endDeclarator()
	 */
	public void declaratorEnd() {
		if (!nodes.empty() && nodes.peek() instanceof Declarator)
			nodes.pop();
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginClass(String, String)
	 */
	public void beginClass(String classKey, String className) {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginClass(String, List)
	 */
	public void classBegin(String classKey, List className) {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endClass()
	 */
	public void classEnd() {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#expressionOperator(Token)
	 */
	public void expressionOperator(Token operator) throws Exception {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#expressionTerminal(Token)
	 */
	public void expressionTerminal(Token terminal) throws Exception {
	}

}
