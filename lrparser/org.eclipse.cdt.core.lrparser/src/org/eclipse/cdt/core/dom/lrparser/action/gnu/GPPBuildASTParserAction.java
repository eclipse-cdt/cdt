/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action.gnu;

import static org.eclipse.cdt.core.parser.util.CollectionUtils.findFirstAndRemove;

import java.util.List;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenMap;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.core.dom.lrparser.action.ParserUtil;
import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;
import org.eclipse.cdt.core.dom.lrparser.action.TokenMap;
import org.eclipse.cdt.core.dom.lrparser.action.cpp.CPPBuildASTParserAction;
import org.eclipse.cdt.core.dom.lrparser.action.cpp.ICPPSecondaryParserFactory;
import org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym;

public class GPPBuildASTParserAction extends CPPBuildASTParserAction {
	
	private final ICPPNodeFactory nodeFactory;
	
	private final ITokenMap gppTokenMap;
	
	public GPPBuildASTParserAction(ITokenStream stream, ScopedStack<Object> astStack, ICPPNodeFactory nodeFactory, ICPPSecondaryParserFactory parserFactory) {
		super(stream, astStack, nodeFactory, parserFactory);
		this.nodeFactory = nodeFactory;
		this.gppTokenMap = new TokenMap(GPPParsersym.orderedTerminalSymbols, stream.getOrderedTerminalSymbols());
	}

	
	/**
	 * typeof_type_specifier
     *     ::= 'typeof' unary_expression
     *   
     * typeof_declaration_specifiers
     *     ::= typeof_type_specifier
     *       | no_type_declaration_specifiers  typeof_type_specifier
     *       | typeof_declaration_specifiers no_type_declaration_specifier
     *
     * declaration_specifiers
     *     ::= <openscope-ast> typeof_declaration_specifiers
	 */
	public void consumeDeclarationSpecifiersTypeof() {
		List<Object> topScope = astStack.closeScope();
		
		// There's an expression somewhere on the stack, find it		
		IASTExpression expr = findFirstAndRemove(topScope, IASTExpression.class);
		//CDT_70_FIX_FROM_50-#7
		ICPPASTSimpleDeclSpecifier declSpec = nodeFactory.newSimpleDeclSpecifier();
		declSpec.setDeclTypeExpression(expr);
		
		// now apply the rest of the specifiers
		for(Object token : topScope) {
			setSpecifier(declSpec, token);
		}

		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}
	
	
	/**
	 * Replacement for the same method in CPPBuildASTParserAction
	 */
	@Override
	public void consumeDeclarationSpecifiersSimple() {
		boolean isComplex = false;
		boolean isImaginary = false;
		int numLong = 0;
		
		List<Object> tokens = astStack.closeScope();
		
		for(Object o : tokens) {
			if(o instanceof IToken) {
				IToken token = (IToken)o;
				switch(gppTokenMap.mapKind(token.getKind())) {
					case GPPParsersym.TK__Complex:   isComplex = true;   break;
					case GPPParsersym.TK__Imaginary: isImaginary = true; break;
					case GPPParsersym.TK_long : numLong++; break;
				}
			}
		}
		//CDT_70_FIX_FROM_50-#7
		ICPPASTSimpleDeclSpecifier declSpec = nodeFactory.newSimpleDeclSpecifier();
		if(isComplex || isImaginary || numLong > 1) {
			// IGPPASTSimpleDeclSpecifier gppDeclSpec = nodeFactory.newSimpleDeclSpecifierGPP();
			declSpec.setComplex(isComplex);
			declSpec.setImaginary(isImaginary);
			declSpec.setLongLong(numLong > 1);
			declSpec.setLong(numLong == 1);
			//declSpec = gppDeclSpec; 
		}
		else {
			declSpec = nodeFactory.newSimpleDeclSpecifier();
		}
		
		for(Object token : tokens) {
			setSpecifier(declSpec, token);
		}
		
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}

	

	private boolean hasRestrict(List<Object> tokens) {
		for(Object o : tokens) {
			IToken t = (IToken)o;
			if(gppTokenMap.mapKind(t.getKind()) == GPPParsersym.TK_restrict) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Restrict is allowed as a keyword.
	 */
	@Override
	public void consumePointer() {
		boolean hasRestrict = hasRestrict(astStack.topScope());
		super.consumePointer();
		
		if(hasRestrict) {
			IGPPASTPointer gppPointer = nodeFactory.newPointerGPP();
			initializeGPPPointer((IASTPointer)astStack.pop(), gppPointer);
			astStack.push(gppPointer);
		}
	}

	
	private static void initializeGPPPointer(IASTPointer pointer, IGPPASTPointer gppPointer) {
		gppPointer.setConst(pointer.isConst());
		gppPointer.setVolatile(pointer.isVolatile());
		gppPointer.setRestrict(true);
		ParserUtil.setOffsetAndLength(gppPointer, pointer);
	}
		
	

	@Override
	public void consumePointerToMember() {
		boolean hasRestrict = hasRestrict(astStack.topScope());
		super.consumePointerToMember();
		
		if(hasRestrict) {
			ICPPASTPointerToMember pointer = (ICPPASTPointerToMember) astStack.pop();
			IGPPASTPointerToMember gppPointer = nodeFactory.newPointerToMemberGPP(pointer.getName());
			initializeGPPPointer(pointer, gppPointer);
			astStack.push(gppPointer);
		}
		
	}
	
	
	public void consumeTemplateExplicitInstantiationGCC(int modifier) {
		IASTDeclaration declaration = (IASTDeclaration) astStack.pop();
		IGPPASTExplicitTemplateInstantiation instantiation = nodeFactory.newExplicitTemplateInstantiationGPP(declaration);
		instantiation.setModifier(modifier);
		setOffsetAndLength(instantiation);
		astStack.push(instantiation);
	}
	

	/**
	 * postfix_expression ::= '(' type_id ')' initializer_list      
	 */
	public void consumeExpressionTypeIdInitializer() {
		IASTInitializerList list = (IASTInitializerList) astStack.pop();
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		IASTTypeIdInitializerExpression expr = nodeFactory.newTypeIdInitializerExpression(typeId, list);
		setOffsetAndLength(expr);
		astStack.push(expr);
	}
}
