/*******************************************************************************
 * Copyright (c) 2009, 2012 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx;

import java.io.IOException;

import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.tests.CodanFastCxxAstTestCase;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;

/**
 * Test CxxAstUtils
 */
public class CxxAstUtilsTest extends CodanFastCxxAstTestCase {
	@Override
	public IChecker getChecker() {
		return null; // not testing checker
	}

	// typedef int A;
	// typedef A B;
	// void main() {
	//    B x;
	// }
	public void testUnwindTypedef() throws IOException {
		String code = getAboveComment();
		IASTTranslationUnit tu = parse(code);
		final Object result[] = new Object[1];
		ASTVisitor astVisitor = new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration decl) {
				if (decl instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) decl;
					IASTDeclSpecifier spec = sdecl.getDeclSpecifier();
					if (spec instanceof IASTNamedTypeSpecifier) {
						IASTName tname = ((IASTNamedTypeSpecifier) spec).getName();
						IType typeName = (IType) tname.resolveBinding();
						result[0] = CxxAstUtils.unwindTypedef(typeName);
					}
				}
				return PROCESS_CONTINUE;
			}
		};
		tu.accept(astVisitor);
		assertNotNull(result[0]);
		ICBasicType type = (ICBasicType) result[0];
		assertEquals(Kind.eInt, type.getKind());
	}

	// #define AAA a
	// void main (){
	//    AAA;
	//    b;
	//}
	public void testIsInMacro() throws IOException {
		String code = getAboveComment();
		IASTTranslationUnit tu = parse(code);
		final Object result[] = new Object[2];
		ASTVisitor astVisitor = new ASTVisitor() {
			int i;
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement stmt) {
				if (stmt instanceof IASTExpressionStatement) {
					boolean check = CxxAstUtils.isInMacro(((IASTExpressionStatement) stmt).getExpression());
					result[i] = check;
					i++;
				}
				return PROCESS_CONTINUE;
			}
		};
		tu.accept(astVisitor);
		assertNotNull("Stmt not found", result[0]); //$NON-NLS-1$
		assertTrue((Boolean) result[0]);
		assertFalse((Boolean) result[1]);
	}

	//void f() __attribute__((noreturn));
	//
	//int test() {
	//  a();
	//  f();
	//  exit(0);
	//}
	public void testExitStatement() throws IOException {
		String code = getAboveComment();
		IASTTranslationUnit tu = parse(code);
		final Object result[] = new Object[4];
		ASTVisitor astVisitor = new ASTVisitor() {
			int i;
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement stmt) {
				boolean check = CxxAstUtils.isExitStatement(stmt);
				result[i] = check;
				i++;
				return PROCESS_CONTINUE;
			}
		};
		tu.accept(astVisitor);
		assertNotNull("Stmt not found", result[0]); //$NON-NLS-1$
		assertFalse((Boolean) result[0]); // compound body
		assertFalse((Boolean) result[1]);
		assertTrue((Boolean) result[2]);
		assertTrue((Boolean) result[3]);
	}
}
