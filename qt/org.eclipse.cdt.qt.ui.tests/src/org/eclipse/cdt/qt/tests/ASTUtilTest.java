/*
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.qt.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2TestBase;
import org.eclipse.cdt.internal.qt.core.ASTUtil;

public class ASTUtilTests extends AST2TestBase {

	// class Type { public: void function() { } };
	// class T
	// {
	//     void callee() { Type instance; instance.function(); }
	//     void caller() { this->callee(); callee(); T::callee(); }
	// };
	// void T::callee()  { this->caller(); caller(); T::caller(); }
	public void testGetReceiverType() throws Exception {
		IASTTranslationUnit tu = parse();
		assertNotNull(tu);

		// Find the callee function call.
		ArrayList<IASTFunctionCallExpression> fnCalls = new ArrayList<>();
		collectChildren(fnCalls, tu, IASTFunctionCallExpression.class);
		assertEquals(7, fnCalls.size());

		assertNotNull(fnCalls.get(0));
		assertNotNull(fnCalls.get(1));
		assertNotNull(fnCalls.get(2));
		assertNotNull(fnCalls.get(3));
		assertNotNull(fnCalls.get(4));
		assertNotNull(fnCalls.get(5));
		assertNotNull(fnCalls.get(6));

		// explicit type
		IASTFunctionCallExpression call = fnCalls.get(0);
		ICPPClassType recvr0 = ASTUtil.getReceiverType(call);
		assertNotNull(recvr0);
		assertEquals("Type", recvr0.getName());

		// implicit this
		ICPPClassType recvr1 = ASTUtil.getReceiverType(fnCalls.get(1));
		ICPPClassType recvr2 = ASTUtil.getReceiverType(fnCalls.get(2));
		ICPPClassType recvr3 = ASTUtil.getReceiverType(fnCalls.get(3));
		ICPPClassType recvr4 = ASTUtil.getReceiverType(fnCalls.get(4));
		ICPPClassType recvr5 = ASTUtil.getReceiverType(fnCalls.get(5));
		ICPPClassType recvr6 = ASTUtil.getReceiverType(fnCalls.get(6));

		assertNotNull(recvr1);
		assertNotNull(recvr2);
		assertNotNull(recvr3);
		assertNotNull(recvr4);
		assertNotNull(recvr5);
		assertNotNull(recvr6);
		assertSame(recvr1, recvr2);
		assertSame(recvr3, recvr4);
		assertSame(recvr4, recvr5);
		assertSame(recvr5, recvr6);
	}

	// class C1
	// {
	// public:
	//     void f( C1 * ) { }
	//     C1 * g() { return this; }
	// };
	// void h() { C1 c; c.f( c.g() ); }
	public void testBaseTypeOfFunctionCall() throws Exception {
		IASTTranslationUnit tu = parse();
		assertNotNull(tu);

		// Find the C1 type.
		ArrayList<ICPPASTCompositeTypeSpecifier> specs = new ArrayList<>();
		collectChildren(specs, tu, ICPPASTCompositeTypeSpecifier.class);
		assertEquals(1, specs.size());

		ICPPASTCompositeTypeSpecifier spec = specs.get(0);
		assertNotNull(spec);

		IASTName specName = spec.getName();
		assertNotNull(specName);
		assertEquals("C1", specName.getRawSignature());

		// Find the function call expression "c.get()".
		ArrayList<IASTFunctionCallExpression> fnCalls = new ArrayList<>();
		collectChildren(fnCalls, tu, IASTFunctionCallExpression.class);
		assertEquals(2, fnCalls.size());

		IASTFunctionCallExpression c_f = fnCalls.get(0);
		IASTFunctionCallExpression c_g = fnCalls.get(1);
		assertNotNull(c_f);
		assertNotNull(c_g);

		IASTExpression nameExpr = c_f.getFunctionNameExpression();
		assertNotNull(nameExpr);
		assertEquals("c.f", nameExpr.getRawSignature());

		IType recvType = ASTUtil.getReceiverType(c_f);
		assertTrue(recvType instanceof ICPPClassType);

		nameExpr = c_g.getFunctionNameExpression();
		assertNotNull(nameExpr);
		assertEquals("c.g", nameExpr.getRawSignature());

		recvType = ASTUtil.getBaseType(c_g);
		assertTrue(recvType instanceof ICPPClassType);
	}

	private IASTTranslationUnit parse() throws Exception {
		String[] contents = BaseQtTestCase.getContentsForTest(getClass(), 1);
		return parse(contents[0], ParserLanguage.CPP);
	}

	private static <T> void collectChildren(List<T> list, IASTNode node, Class<T> cls) {
		if (cls.isAssignableFrom(node.getClass()))
			list.add(cls.cast(node));

		for (IASTNode child : node.getChildren())
			collectChildren(list, child, cls);
	}
}
