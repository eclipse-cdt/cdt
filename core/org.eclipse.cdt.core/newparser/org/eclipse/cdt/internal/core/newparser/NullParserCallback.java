package org.eclipse.cdt.internal.core.newparser;

public class NullParserCallback implements IParserCallback {
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginArguments()
	 */
	public void argumentsBegin() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginClass(String, Token)
	 */
	public void classBegin(String classKey, Token name) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginDeclarator()
	 */
	public void declaratorBegin() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginFunctionBody()
	 */
	public void functionBodyBegin() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginInclusion(String)
	 */
	public void inclusionBegin(String includeFile) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginSimpleDeclaration(Token)
	 */
	public void simpleDeclarationBegin(Token firstToken) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginTranslationUnit()
	 */
	public void translationUnitBegin() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorId(Token)
	 */
	public void declaratorId() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declSpecifier(Token)
	 */
	public void simpleDeclSpecifier(Token specifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endArguments()
	 */
	public void argumentsEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endClass()
	 */
	public void classEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endDeclarator()
	 */
	public void declaratorEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endFunctionBody()
	 */
	public void functionBodyEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endInclusion()
	 */
	public void inclusionEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endSimpleDeclaration(Token)
	 */
	public void simpleDeclarationEnd(Token lastToken) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endTranslationUnit()
	 */
	public void translationUnitEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionOperator(Token)
	 */
	public void expressionOperator(Token operator) throws Exception {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionTerminal(Token)
	 */
	public void expressionTerminal(Token terminal) throws Exception {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#macro(String)
	 */
	public void macro(String macroName) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameBegin(Token firstToken) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameEnd(Token lastToken) {
	}

}
