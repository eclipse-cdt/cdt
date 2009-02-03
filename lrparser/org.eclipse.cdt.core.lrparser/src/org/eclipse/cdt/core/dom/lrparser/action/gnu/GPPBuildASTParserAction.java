/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;
import org.eclipse.cdt.core.dom.lrparser.action.cpp.CPPBuildASTParserAction;
import org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym;

public class GPPBuildASTParserAction extends GNUBuildASTParserAction {
	
	private final ICPPNodeFactory nodeFactory;
	
	private CPPBuildASTParserAction baseAction;
	
	
	public GPPBuildASTParserAction(IParserActionTokenProvider parser, ScopedStack<Object> astStack, ICPPNodeFactory nodeFactory) {
		super(parser, astStack, nodeFactory);
		this.nodeFactory = nodeFactory;
	}
	
	public void setBaseAction(CPPBuildASTParserAction baseAction) {
		this.baseAction = baseAction;
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
		IGPPASTSimpleDeclSpecifier declSpec = nodeFactory.newSimpleDeclSpecifierGPP();
		declSpec.setTypeofExpression(expr);
		
		// now apply the rest of the specifiers
		for(Object token : topScope) {
			baseAction.setSpecifier(declSpec, token);
		}

		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}
	
	
	/**
	 * Replacement for the method same method in CPPBuildASTParserAction
	 */
	public void consumeDeclarationSpecifiersSimple() {
		boolean isComplex = false;
		boolean isImaginary = false;
		int numLong = 0;
		
		List<Object> tokens = astStack.closeScope();
		
		for(Object o : tokens) {
			if(o instanceof IToken) {
				IToken token = (IToken)o;
				switch(token.getKind()) {
					case GPPParsersym.TK__Complex:   isComplex = true;   break;
					case GPPParsersym.TK__Imaginary: isImaginary = true; break;
					case GPPParsersym.TK_long : numLong++; break;
				} 
			}
		} 
		
		ICPPASTSimpleDeclSpecifier declSpec;
		if(isComplex || isImaginary || numLong > 1) {
			IGPPASTSimpleDeclSpecifier gppDeclSpec = nodeFactory.newSimpleDeclSpecifierGPP();
			gppDeclSpec.setComplex(isComplex);
			gppDeclSpec.setImaginary(isImaginary);
			gppDeclSpec.setLongLong(numLong > 1);
			gppDeclSpec.setLong(numLong == 1);
			declSpec = gppDeclSpec; 
		}
		else {
			declSpec = nodeFactory.newSimpleDeclSpecifier();
		}
		
		for(Object token : tokens) {
			baseAction.setSpecifier(declSpec, token);
		}
		
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}
	
	
}
