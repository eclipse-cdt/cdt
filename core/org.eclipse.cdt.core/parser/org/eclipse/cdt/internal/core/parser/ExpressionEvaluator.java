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
package org.eclipse.cdt.internal.core.parser;

import java.util.EmptyStackException;
import java.util.Stack;

public class ExpressionEvaluator implements IParserCallback {

	public class ExpressionException extends Exception {
		public ExpressionException(String msg) {
			super(msg);
		}
	}
	
	private Stack stack = new Stack();
	
	private int popInt() {
		return ((Integer)stack.pop()).intValue();
	}
	
	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#expressionOperator(Token)
	 */
	public void expressionOperator(Object expression, Token operator) throws Exception {
		
		int second = popInt(); 
		int first; 
		switch (operator.getType()) {
			
			case Token.tPLUS:
				first = popInt(); 
				stack.push(new Integer(first + second));
				break;
			case Token.tMINUS:
				first = popInt(); 
				stack.push(new Integer(first - second));
				break;
			case Token.tSTAR:
				first = popInt(); 			
				stack.push(new Integer(first * second));
				break;
			case Token.tDIV:
				first = popInt(); 
				stack.push(new Integer(first / second));
				break;
			case Token.tLT:
				first = popInt(); 			
				stack.push(new Integer(first < second ? 1 : 0));
				break;
			case Token.tLTEQUAL:
				first = popInt(); 			
				stack.push(new Integer(first <= second ? 1 : 0));
				break;
			case Token.tGT:
				first = popInt(); 			
				stack.push(new Integer(first > second  ? 1 : 0));
				break;
			case Token.tGTEQUAL:
				first = popInt(); 			
				stack.push(new Integer(first >= second  ? 1 : 0));
				break;
			case Token.tEQUAL:
				first = popInt(); 			
				stack.push(new Integer(first == second  ? 1 : 0));
				break;
			case Token.tNOTEQUAL:
				first = popInt(); 			
				stack.push(new Integer(first != second  ? 1 : 0));
				break;
			case Token.tAND:
				first = popInt(); 			
				stack.push( new Integer( ( ( first != 0 ) && ( second != 0 ) ) ? 1 : 0  ) ); 
				break; 
			case Token.tOR:
				first = popInt(); 			
				stack.push( new Integer( ( ( first != 0 ) || ( second != 0 ) ) ? 1 : 0  ) ); 
				break;
			case Token.tNOT: 
				stack.push( new Integer( ( second == 0 ) ? 1 : 0 ) ); 
				break;
			default:
				throw new ExpressionException("Unhandled operator: " + operator );
		}
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#expressionTerminal(Token)
	 */
	public void expressionTerminal(Object expression, Token terminal) throws Exception {
		switch (terminal.getType()) {
			case Token.tINTEGER:
				stack.push(new Integer(terminal.getImage()));
				break;
			default:
				throw new ExpressionException("Unhandled terminal: " + terminal.getImage());
		}
	}
	
	public Object getResult() throws EmptyStackException {
		return stack.peek();
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#translationUnitBegin()
	 */
	public Object translationUnitBegin() {
		return null;
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#translationUnitEnd(java.lang.Object)
	 */
	public void translationUnitEnd(Object unit) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#inclusionBegin(java.lang.String, int)
	 */
	public void inclusionBegin(String includeFile, int offset) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#inclusionEnd()
	 */
	public void inclusionEnd() {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#macro(java.lang.String, int)
	 */
	public void macro(String macroName, int offset) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclarationBegin(java.lang.Object)
	 */
	public Object simpleDeclarationBegin(Object Container) {
		return null;
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclarationEnd(java.lang.Object)
	 */
	public void simpleDeclarationEnd(Object declaration) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#parameterDeclarationBegin(java.lang.Object)
	 */
	public Object parameterDeclarationBegin(Object Container) {
		return null;
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#parameterDeclarationEnd(java.lang.Object)
	 */
	public void parameterDeclarationEnd(Object declaration) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclSpecifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void simpleDeclSpecifier(Object Container, Token specifier) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#nameBegin(org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void nameBegin(Token firstToken) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#nameEnd(org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void nameEnd(Token lastToken) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorBegin(java.lang.Object)
	 */
	public Object declaratorBegin(Object container) {
		return null;
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorId(java.lang.Object)
	 */
	public void declaratorId(Object declarator) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorAbort(java.lang.Object, java.lang.Object)
	 */
	public void declaratorAbort(Object container, Object declarator) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorEnd(java.lang.Object)
	 */
	public void declaratorEnd(Object declarator) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#argumentsBegin(java.lang.Object)
	 */
	public Object argumentsBegin(Object declarator) {
		return null;
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#argumentsEnd(java.lang.Object)
	 */
	public void argumentsEnd(Object parameterDeclarationClause) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#functionBodyBegin()
	 */
	public Object functionBodyBegin(Object declaration) {
		return null;
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#functionBodyEnd()
	 */
	public void functionBodyEnd(Object functionBody) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object classSpecifierBegin(Object container, Token classKey) {
		return null;
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierName(java.lang.Object)
	 */
	public void classSpecifierName(Object classSpecifier) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierEnd(java.lang.Object)
	 */
	public void classSpecifierEnd(Object classSpecifier) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#baseSpecifierBegin(java.lang.Object)
	 */
	public Object baseSpecifierBegin(Object containingClassSpec) {
		return null;
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#baseSpecifierName(java.lang.Object)
	 */
	public void baseSpecifierName(Object baseSpecifier) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#baseSpecifierVisibility(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void baseSpecifierVisibility(
		Object baseSpecifier,
		Token visibility) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#baseSpecifierVirtual(java.lang.Object, boolean)
	 */
	public void baseSpecifierVirtual(Object baseSpecifier, boolean virtual) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#baseSpecifierEnd(java.lang.Object)
	 */
	public void baseSpecifierEnd(Object baseSpecifier) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionBegin(java.lang.Object)
	 */
	public Object expressionBegin(Object container) {
		return null;
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionEnd(java.lang.Object)
	 */
	public void expressionEnd(Object expression) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierAbort(java.lang.Object)
	 */
	public void classSpecifierAbort(Object classSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierSafe(java.lang.Object)
	 */
	public void classSpecifierSafe(Object classSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#elaboratedTypeSpecifierBegin(java.lang.Object)
	 */
	public Object elaboratedTypeSpecifierBegin(Object container, Token classKey) {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#elaboratedTypeSpecifierEnd(java.lang.Object)
	 */
	public void elaboratedTypeSpecifierEnd(Object elab) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#elaboratedTypeSpecifierName(java.lang.Object)
	 */
	public void elaboratedTypeSpecifierName(Object container) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclSpecifierName(java.lang.Object)
	 */
	public void simpleDeclSpecifierName(Object declaration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionAbort(java.lang.Object)
	 */
	public void expressionAbort(Object expression) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classMemberVisibility(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void classMemberVisibility(Object classSpecifier, Token visibility) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object pointerOperatorBegin(Object container) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorEnd(java.lang.Object)
	 */
	public void pointerOperatorEnd(Object ptrOperator) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorName(java.lang.Object)
	 */
	public void pointerOperatorName(Object ptrOperator) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorType(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void pointerOperatorType(Object ptrOperator, Token type) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void pointerOperatorCVModifier(Object ptrOperator, Token modifier) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void declaratorCVModifier(Object declarator, Token modifier) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#arrayBegin(java.lang.Object)
	 */
	public Object arrayDeclaratorBegin(Object declarator) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#arrayEnd(java.lang.Object)
	 */
	public void arrayDeclaratorEnd(Object arrayQualifier ) {
		// TODO Auto-generated method stub;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#exceptionSpecificationTypename(java.lang.Object)
	 */
	public void declaratorThrowExceptionName(Object declarator) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorThrowsException(java.lang.Object)
	 */
	public void declaratorThrowsException(Object declarator) {
		// TODO Auto-generated method stub
		
	}

}
