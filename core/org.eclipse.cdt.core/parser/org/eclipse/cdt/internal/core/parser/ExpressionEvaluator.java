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
	public void expressionOperator(Object expression, Token operator) {
		
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
				// throw new ExpressionException("Unhandled operator: " + operator );
		}
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#expressionTerminal(Token)
	 */
	public void expressionTerminal(Object expression, Token terminal) {
		switch (terminal.getType()) {
			case Token.tINTEGER:
				stack.push(new Integer(terminal.getImage()));
				break;
			default:
				// throw new ExpressionException("Unhandled terminal: " + terminal.getImage());
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
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classMemberVisibility(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void classMemberVisibility(Object classSpecifier, Token visibility) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object pointerOperatorBegin(Object container) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorEnd(java.lang.Object)
	 */
	public void pointerOperatorEnd(Object ptrOperator) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorName(java.lang.Object)
	 */
	public void pointerOperatorName(Object ptrOperator) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorType(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void pointerOperatorType(Object ptrOperator, Token type) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void pointerOperatorCVModifier(Object ptrOperator, Token modifier) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void declaratorCVModifier(Object declarator, Token modifier) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#arrayBegin(java.lang.Object)
	 */
	public Object arrayDeclaratorBegin(Object declarator) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#arrayEnd(java.lang.Object)
	 */
	public void arrayDeclaratorEnd(Object arrayQualifier ) {
		;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#exceptionSpecificationTypename(java.lang.Object)
	 */
	public void declaratorThrowExceptionName(Object declarator) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorThrowsException(java.lang.Object)
	 */
	public void declaratorThrowsException(Object declarator) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationBegin(java.lang.Object)
	 */
	public Object namespaceDefinitionBegin(Object container) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationId(java.lang.Object)
	 */
	public void namespaceDefinitionId(Object namespace) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationAbort(java.lang.Object)
	 */
	public void namespaceDefinitionAbort(Object namespace) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationEnd(java.lang.Object)
	 */
	public void namespaceDefinitionEnd(Object namespace) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#linkageSpecificationBegin(java.lang.Object, java.lang.String)
	 */
	public Object linkageSpecificationBegin(Object container, String literal) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#linkageSpecificationEnd(java.lang.Object)
	 */
	public void linkageSpecificationEnd(Object linkageSpec) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveBegin(java.lang.Object)
	 */
	public Object usingDirectiveBegin(Object container) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveNamespaceId(java.lang.Object)
	 */
	public void usingDirectiveNamespaceId(Object container) {

		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveEnd(java.lang.Object)
	 */
	public void usingDirectiveEnd(Object directive) {

		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationBegin(java.lang.Object)
	 */
	public Object usingDeclarationBegin(Object container) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationMapping(java.lang.Object)
	 */
	public void usingDeclarationMapping(Object container, boolean isTypename) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationEnd(java.lang.Object)
	 */
	public void usingDeclarationEnd(Object directive) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveAbort(java.lang.Object)
	 */
	public void usingDirectiveAbort(Object directive) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationAbort(java.lang.Object)
	 */
	public void usingDeclarationAbort(Object declaration) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierBegin(java.lang.Object)
	 */
	public Object enumSpecifierBegin(Object container) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierId(java.lang.Object)
	 */
	public void enumSpecifierId(Object enumSpec) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierAbort(java.lang.Object)
	 */
	public void enumSpecifierAbort(Object enumSpec) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierEnd(java.lang.Object)
	 */
	public void enumSpecifierEnd(Object enumSpec) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumDefinitionBegin(java.lang.Object)
	 */
	public Object enumDefinitionBegin(Object enumSpec) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumDefinitionId(java.lang.Object)
	 */
	public void enumDefinitionId(Object enumDefn) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumDefinitionEnd(java.lang.Object)
	 */
	public void enumDefinitionEnd(Object enumDefn) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#asmDefinition(java.lang.String)
	 */
	public void asmDefinition(Object container, String assemblyCode) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainBegin(java.lang.Object)
	 */
	public Object constructorChainBegin(Object declarator) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainAbort(java.lang.Object)
	 */
	public void constructorChainAbort(Object ctor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainEnd(java.lang.Object)
	 */
	public void constructorChainEnd(Object ctor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementBegin(java.lang.Object)
	 */
	public Object constructorChainElementBegin(Object ctor) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementEnd(java.lang.Object)
	 */
	public void constructorChainElementEnd(Object element) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainId(java.lang.Object)
	 */
	public void constructorChainElementId(Object ctor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementExpressionListElementBegin(java.lang.Object)
	 */
	public Object constructorChainElementExpressionListElementBegin(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementExpressionListElementEnd(java.lang.Object)
	 */
	public void constructorChainElementExpressionListElementEnd(Object expression) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitInstantiationBegin(java.lang.Object)
	 */
	public Object explicitInstantiationBegin(Object container) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitInstantiationEnd(java.lang.Object)
	 */
	public void explicitInstantiationEnd(Object instantiation) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitSpecializationBegin(java.lang.Object)
	 */
	public Object explicitSpecializationBegin(Object container) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitSpecializationEnd(java.lang.Object)
	 */
	public void explicitSpecializationEnd(Object instantiation) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorPureVirtual(java.lang.Object)
	 */
	public void declaratorPureVirtual(Object declarator) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationBegin(java.lang.Object, boolean)
	 */
	public Object templateDeclarationBegin(Object container, boolean exported) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationAbort(java.lang.Object)
	 */
	public void templateDeclarationAbort(Object templateDecl) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationEnd(java.lang.Object)
	 */
	public void templateDeclarationEnd(Object templateDecl) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object templateTypeParameterBegin(Object templDecl, Token kind) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterName(java.lang.Object)
	 */
	public void templateTypeParameterName(Object typeParm) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeInitialTypeId(java.lang.Object)
	 */
	public void templateTypeParameterInitialTypeId(Object typeParm) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterEnd(java.lang.Object)
	 */
	public void templateTypeParameterEnd(Object typeParm) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterAbort(java.lang.Object)
	 */
	public void templateTypeParameterAbort(Object typeParm) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorAbort(java.lang.Object)
	 */
	public void pointerOperatorAbort(Object ptrOperator) {
	}

}
