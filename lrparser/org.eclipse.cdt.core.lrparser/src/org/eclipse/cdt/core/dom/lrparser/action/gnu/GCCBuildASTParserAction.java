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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICNodeFactory;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;
import org.eclipse.cdt.core.dom.lrparser.action.c99.C99BuildASTParserAction;

public class GCCBuildASTParserAction extends GNUBuildASTParserAction {

	private final ICNodeFactory nodeFactory;

	private C99BuildASTParserAction baseAction;
	
	public GCCBuildASTParserAction(IParserActionTokenProvider parser, IASTTranslationUnit tu, ScopedStack<Object> astStack, ICNodeFactory nodeFactory) {
		super(parser, tu, astStack, nodeFactory);
		this.nodeFactory = nodeFactory;
	}
	
	
	public void setBaseAction(C99BuildASTParserAction baseAction) {
		this.baseAction = baseAction;
	}
	
	/**
	 * designator_base
     *     ::= identifier_token ':'		
	 */
	public void consumeDesignatorField() {
		IASTName name = createName(parser.getLeftIToken());
		ICASTFieldDesignator designator = nodeFactory.newFieldDesignator(name);
		setOffsetAndLength(designator);
		astStack.push(designator);
	}
	
	/**
	 * designator ::= '[' constant_expression '...' constant_expression']'
	 */
	public void consumeDesignatorArray() {
		IASTExpression ceiling = (IASTExpression) astStack.pop();
		IASTExpression floor = (IASTExpression) astStack.pop();
		IGCCASTArrayRangeDesignator designator = nodeFactory.newArrayRangeDesignatorGCC(floor, ceiling);
		setOffsetAndLength(designator);
		astStack.push(designator);
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
		ICASTSimpleDeclSpecifier declSpec = nodeFactory.newSimpleDeclSpecifierGCC(expr);
		
		// now apply the rest of the specifiers
		for(Object token : topScope) {
			baseAction.setSpecifier(declSpec, token);
		}

		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}
}
