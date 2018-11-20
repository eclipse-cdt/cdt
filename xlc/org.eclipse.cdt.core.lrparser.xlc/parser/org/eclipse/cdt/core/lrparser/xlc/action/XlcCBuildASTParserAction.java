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
package org.eclipse.cdt.core.lrparser.xlc.action;

import org.eclipse.cdt.core.dom.lrparser.action.ISecondaryParserFactory;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;
import org.eclipse.cdt.core.dom.lrparser.action.gnu.GCCBuildASTParserAction;
import org.eclipse.cdt.core.lrparser.xlc.ast.IXlcCASTVectorTypeSpecifier;
import org.eclipse.cdt.core.lrparser.xlc.ast.IXlcCNodeFactory;
import org.eclipse.cdt.internal.core.lrparser.xlc.c.XlcCParsersym;

import lpg.lpgjavaruntime.IToken;

public class XlcCBuildASTParserAction extends GCCBuildASTParserAction {

	private IXlcCNodeFactory nodeFactory;

	public XlcCBuildASTParserAction(ITokenStream parser, ScopedStack<Object> astStack, IXlcCNodeFactory nodeFactory,
			ISecondaryParserFactory parserFactory) {
		super(parser, astStack, nodeFactory, parserFactory);
		this.nodeFactory = nodeFactory;
	}

	/*
	 * vector_type
	 *     ::= <openscope-ast> sqlist_op 'vector' vector_type_specifier all_specifier_qualifier_list
	 */
	public void consumeVectorTypeSpecifier() {
		IXlcCASTVectorTypeSpecifier declSpec = nodeFactory.newVectorTypeSpecifier();

		for (Object specifier : astStack.closeScope()) {
			if (specifier instanceof IToken) {
				switch (((IToken) specifier).getKind()) {
				case XlcCParsersym.TK_pixel:
					declSpec.setPixel(true);
					continue;
				case XlcCParsersym.TK_bool:
					declSpec.setBool(true);
					continue;
				case XlcCParsersym.TK_vector:
					continue;
				}
			}

			setSpecifier(declSpec, specifier);
		}

		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}

}
