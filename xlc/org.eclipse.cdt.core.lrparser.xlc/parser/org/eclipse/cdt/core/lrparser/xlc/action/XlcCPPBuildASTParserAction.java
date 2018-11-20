/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
package org.eclipse.cdt.core.lrparser.xlc.action;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStaticAssertDeclaration;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenMap;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;
import org.eclipse.cdt.core.dom.lrparser.action.TokenMap;
import org.eclipse.cdt.core.dom.lrparser.action.cpp.ICPPSecondaryParserFactory;
import org.eclipse.cdt.core.dom.lrparser.action.gnu.GPPBuildASTParserAction;
import org.eclipse.cdt.core.lrparser.xlc.ast.IXlcCPPASTModifiedArrayModifier;
import org.eclipse.cdt.core.lrparser.xlc.ast.IXlcCPPASTVectorTypeSpecifier;
import org.eclipse.cdt.core.lrparser.xlc.ast.IXlcCPPNodeFactory;
import org.eclipse.cdt.internal.core.lrparser.xlc.cpp.XlcCPPParsersym;

import lpg.lpgjavaruntime.IToken;

public class XlcCPPBuildASTParserAction extends GPPBuildASTParserAction {

	private IXlcCPPNodeFactory nodeFactory;
	private final ITokenMap tokenMap;

	public XlcCPPBuildASTParserAction(ITokenStream parser, ScopedStack<Object> astStack, IXlcCPPNodeFactory nodeFactory,
			ICPPSecondaryParserFactory parserFactory) {
		super(parser, astStack, nodeFactory, parserFactory);
		this.nodeFactory = nodeFactory;
		this.tokenMap = new TokenMap(XlcCPPParsersym.orderedTerminalSymbols, parser.getOrderedTerminalSymbols());
	}

	/*
	 * vector_type
	 *     ::= <openscope-ast> sqlist_op 'vector' vector_type_specifier all_specifier_qualifier_list
	 */
	public void consumeVectorTypeSpecifier() {
		IXlcCPPASTVectorTypeSpecifier declSpec = nodeFactory.newVectorTypeSpecifier();

		for (Object specifier : astStack.closeScope()) {
			if (specifier instanceof IToken) {
				switch (tokenMap.mapKind(((IToken) specifier).getKind())) {
				case XlcCPPParsersym.TK_pixel:
					declSpec.setPixel(true);
					continue;
				case XlcCPPParsersym.TK_vector:
					continue;
				}
			}

			setSpecifier(declSpec, specifier);
		}

		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}

	public void consumeDirectDeclaratorModifiedArrayModifier(boolean isStatic, boolean isVarSized,
			boolean hasTypeQualifierList, boolean hasAssignmentExpr) {
		assert isStatic || isVarSized || hasTypeQualifierList;

		IXlcCPPASTModifiedArrayModifier arrayModifier = nodeFactory.newModifiedArrayModifier(null);

		// consume all the stuff between the square brackets into an array modifier
		arrayModifier.setStatic(isStatic);
		arrayModifier.setVariableSized(isVarSized);

		if (hasAssignmentExpr)
			arrayModifier.setConstantExpression((IASTExpression) astStack.pop());

		if (hasTypeQualifierList)
			collectArrayModifierTypeQualifiers(arrayModifier);

		setOffsetAndLength(arrayModifier);
		astStack.push(arrayModifier);
	}

	private void collectArrayModifierTypeQualifiers(IXlcCPPASTModifiedArrayModifier arrayModifier) {
		for (Object o : astStack.closeScope()) {
			switch (tokenMap.mapKind(((IToken) o).getKind())) {
			case XlcCPPParsersym.TK_const:
				arrayModifier.setConst(true);
				break;
			case XlcCPPParsersym.TK_restrict:
				arrayModifier.setRestrict(true);
				break;
			case XlcCPPParsersym.TK_volatile:
				arrayModifier.setVolatile(true);
				break;
			}
		}
	}

	/**
	 * staticAssertDeclaration ::= '__static_assert'  '(' expression ',' literal ')' ';'
	 */
	public void consumeCPPASTStaticAssertDeclaration() {
		ICPPASTLiteralExpression message = (ICPPASTLiteralExpression) astStack.pop();
		IASTExpression condition = (IASTExpression) astStack.pop();

		ICPPASTStaticAssertDeclaration assertDeclaration = nodeFactory.newStaticAssertion(condition, message);
		setOffsetAndLength(assertDeclaration);
		astStack.push(assertDeclaration);
	}
}
