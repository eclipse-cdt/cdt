package org.eclipse.cdt.internal.core.dom;

import java.util.Stack;

import org.eclipse.cdt.internal.core.newparser.IParserCallback;
import org.eclipse.cdt.internal.core.newparser.Token;

/**
 * This is the parser callback that creates objects in the DOM.
 */
public class DOMBuilder implements IParserCallback {

	private TranslationUnit translationUnit;
	
	public TranslationUnit getTranslationUnit() {
		return translationUnit;
	}

	private Stack stack = new Stack();
		
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsBegin()
	 */
	public void argumentsBegin() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsEnd()
	 */
	public void argumentsEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classBegin(java.lang.String, org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void classBegin(String classKey, Token name) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classEnd()
	 */
	public void classEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorBegin()
	 */
	public void declaratorBegin() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorEnd()
	 */
	public void declaratorEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorId(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void declaratorId(Token id) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declSpecifier(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclSpecifier(Token specifier) {
		SimpleDeclaration decl = (SimpleDeclaration)stack.peek();
		
		switch (specifier.getType()) {
			case Token.t_auto:
				decl.setAuto(true);
				break;
			case Token.t_register:
				decl.setRegister(true);
				break;
			case Token.t_static:
				decl.setStatic(true);
				break;
			case Token.t_extern:
				decl.setExtern(true);
				break;
			case Token.t_mutable:
				decl.setMutable(true);
				break;
			case Token.t_inline:
				decl.setInline(true);
				break;
			case Token.t_virtual:
				decl.setVirtual(true);
				break;
			case Token.t_explicit:
				decl.setExplicit(true);
				break;
			case Token.t_typedef:
				decl.setTypedef(true);
				break;
			case Token.t_friend:
				decl.setFriend(true);
				break;
			case Token.t_const:
				decl.setConst(true);
				break;
			case Token.t_volatile:
				decl.setVolatile(true);
				break;
			case Token.t_char:
				decl.setType(SimpleDeclaration.t_char);
				break;
			case Token.t_wchar_t:
				decl.setType(SimpleDeclaration.t_wchar_t);
				break;
			case Token.t_bool:
				decl.setType(SimpleDeclaration.t_bool);
				break;
			case Token.t_short:
				decl.setShort(true);
				break;
			case Token.t_int:
				decl.setType(SimpleDeclaration.t_int);
				break;
			case Token.t_long:
				decl.setLong(true);
				break;
			case Token.t_signed:
				decl.setUnsigned(false);
				break;
			case Token.t_unsigned:
				decl.setUnsigned(true);
				break;
			case Token.t_float:
				decl.setType(SimpleDeclaration.t_float);
				break;
			case Token.t_double:
				decl.setType(SimpleDeclaration.t_double);
				break;
			case Token.t_void:
				decl.setType(SimpleDeclaration.t_void);
				break;
		}
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
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyBegin()
	 */
	public void functionBodyBegin() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyEnd()
	 */
	public void functionBodyEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#inclusionBegin(java.lang.String)
	 */
	public void inclusionBegin(String includeFile) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#inclusionEnd()
	 */
	public void inclusionEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#macro(java.lang.String)
	 */
	public void macro(String macroName) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclarationBegin(Token firstToken) {
		SimpleDeclaration decl = new SimpleDeclaration();
		((TranslationUnit)stack.peek()).addDeclaration(decl);
		stack.push(decl);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclarationEnd(Token lastToken) {
		stack.pop();
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#translationUnitBegin()
	 */
	public void translationUnitBegin() {
		translationUnit = new TranslationUnit();
		stack.push(translationUnit);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#translationUnitEnd()
	 */
	public void translationUnitEnd() {
		stack.pop();
	}

}
