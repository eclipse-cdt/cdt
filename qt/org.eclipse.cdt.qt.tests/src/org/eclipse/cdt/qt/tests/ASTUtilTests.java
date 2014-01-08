/*
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2Tests;
import org.eclipse.cdt.internal.qt.core.ASTUtil;

public class ASTUtilTests extends AST2Tests {

	// class T
	// {
	//     void callee() { }
	//     void caller() { this->callee(); callee(); T::callee(); }
	// };
	// void T::callee()  { this->caller(); caller(); T::caller(); }
	public void testGetReceiverType() throws Exception {
		IASTTranslationUnit tu = parse();
		assertNotNull(tu);

		// Find the callee function call.
		ArrayList<IASTFunctionCallExpression> fnCalls = new ArrayList<IASTFunctionCallExpression>();
		collectChildren(fnCalls, tu, IASTFunctionCallExpression.class);
		assertEquals(6, fnCalls.size());

		assertNotNull(fnCalls.get(0));
		assertNotNull(fnCalls.get(1));
		assertNotNull(fnCalls.get(2));
		assertNotNull(fnCalls.get(3));
		assertNotNull(fnCalls.get(4));
		assertNotNull(fnCalls.get(5));

		ICPPClassType recvr0 = ASTUtil.getReceiverType(fnCalls.get(0));
		ICPPClassType recvr1 = ASTUtil.getReceiverType(fnCalls.get(1));
		ICPPClassType recvr2 = ASTUtil.getReceiverType(fnCalls.get(2));
		ICPPClassType recvr3 = ASTUtil.getReceiverType(fnCalls.get(3));
		ICPPClassType recvr4 = ASTUtil.getReceiverType(fnCalls.get(4));
		ICPPClassType recvr5 = ASTUtil.getReceiverType(fnCalls.get(5));

		assertNotNull(recvr0);
		assertNotNull(recvr1);
		assertNotNull(recvr2);
		assertNotNull(recvr3);
		assertNotNull(recvr4);
		assertNotNull(recvr5);
		assertSame(recvr0, recvr1);
		assertSame(recvr1, recvr2);
		assertSame(recvr3, recvr4);
		assertSame(recvr4, recvr5);
	}

	private IASTTranslationUnit parse() throws Exception {
		String[] contents = BaseQtTestCase.getContentsForTest(getClass(), 1);
		return parse(contents[0], ParserLanguage.CPP);
	}

	private static <T> void collectChildren(List<T> list, IASTNode node, Class<T> cls) {
		if (cls.isAssignableFrom(node.getClass()))
			list.add(cls.cast(node));

		for(IASTNode child : node.getChildren())
			collectChildren(list, child, cls);
	}
}
