package org.eclipse.cdt.internal.core.newparser;

import java.util.List;

public class NullParserCallback implements IParserCallback {

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginTranslationUnit()
	 */
	public void beginTranslationUnit() {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endTranslationUnit()
	 */
	public void endTranslationUnit() {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginSimpleDeclaration()
	 */
	public void beginSimpleDeclaration() {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endSimpleDeclaration()
	 */
	public void endSimpleDeclaration() {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#declSpecifier(String)
	 */
	public void declSpecifier(String specifier) {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#declSpecifier(List)
	 */
	public void declSpecifier(List specifier) {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#declaratorId(List)
	 */
	public void declaratorId(List name) {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginClass(String, String)
	 */
	public void beginClass(String classKey, String className) {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginClass(String, List)
	 */
	public void beginClass(String classKey, List className) {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endClass()
	 */
	public void endClass() {
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

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginDeclarator()
	 */
	public void beginDeclarator() {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endDeclarator()
	 */
	public void endDeclarator() {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginArguments()
	 */
	public void beginArguments() {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endArguments()
	 */
	public void endArguments() {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#inclusion(String)
	 */
	public void inclusion(String includeFile) {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#macro(String)
	 */
	public void macro(String macroName) {
	}

}
