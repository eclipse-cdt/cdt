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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICNodeFactory;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;

public class GCCBuildASTParserAction extends GNUBuildASTParserAction {

	private final ICNodeFactory nodeFactory;
	
	public GCCBuildASTParserAction(IParserActionTokenProvider parser, IASTTranslationUnit tu, ScopedStack<Object> astStack, ICNodeFactory nodeFactory) {
		super(parser, tu, astStack, nodeFactory);
		this.nodeFactory = nodeFactory;
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
}
