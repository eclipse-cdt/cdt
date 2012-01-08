/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.append;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class ExpressionTest extends ChangeGeneratorTest {

	public ExpressionTest(){
		super("Append Expression"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "void main() {\n\tint s = 0, c = 0, h = 0;\n\ts = 3, h = 5;\n}"; //$NON-NLS-1$
		expectedSource = "void main() {\n\tint s = 0, c = 0, h = 0;\n\ts = 3, h = 5, c = 9;\n}"; //$NON-NLS-1$
		super.setUp();
	}
	@Override
	protected ASTVisitor createModificator(final ASTModificationStore modStore) {
		return new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}
			
			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTExpressionList) {
					IASTExpressionList expressionList = (IASTExpressionList) expression;
					expressionList.getExpressions();
					CPPASTIdExpression idExpression = new CPPASTIdExpression(new CPPASTName("c".toCharArray()));
					CPPASTBinaryExpression binEx = new CPPASTBinaryExpression(IASTBinaryExpression.op_assign,
							idExpression, new CPPASTLiteralExpression(0, "9".toCharArray())); //$NON-NLS-1$ 
					ASTModification modification = new ASTModification(ASTModification.ModificationKind.APPEND_CHILD,
							expressionList, binEx, null);
					modStore.storeModification(null, modification);
				}

				return PROCESS_CONTINUE;
			}
		};
	}
	public static Test suite() {
		return new ExpressionTest();
	}
}
