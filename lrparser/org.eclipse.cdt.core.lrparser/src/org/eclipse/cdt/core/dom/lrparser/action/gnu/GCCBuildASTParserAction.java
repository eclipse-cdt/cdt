/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action.gnu;

import static org.eclipse.cdt.core.parser.util.CollectionUtils.findFirstAndRemove;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICNodeFactory;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.core.dom.lrparser.action.ISecondaryParserFactory;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;
import org.eclipse.cdt.core.dom.lrparser.action.c99.C99BuildASTParserAction;

public class GCCBuildASTParserAction extends C99BuildASTParserAction {

	private final ICNodeFactory nodeFactory;

	public GCCBuildASTParserAction(ITokenStream parser, ScopedStack<Object> astStack, ICNodeFactory nodeFactory,
			ISecondaryParserFactory parserFactory) {
		super(parser, astStack, nodeFactory, parserFactory);
		this.nodeFactory = nodeFactory;
	}

	/**
	 * designator_base
	 *     ::= identifier_token ':'
	 */
	public void consumeDesignatorFieldGCC() {
		IASTName name = createName(stream.getLeftIToken());
		ICASTFieldDesignator designator = nodeFactory.newFieldDesignator(name);
		setOffsetAndLength(designator);
		astStack.push(designator);
	}

	/**
	 * designator ::= '[' constant_expression '...' constant_expression']'
	 */
	public void consumeDesignatorArrayRange() {
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
		for (Object token : topScope) {
			setSpecifier(declSpec, token);
		}

		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}
}
