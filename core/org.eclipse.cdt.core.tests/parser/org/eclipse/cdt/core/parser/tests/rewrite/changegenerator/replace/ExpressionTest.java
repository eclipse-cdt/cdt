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
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.replace;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class ExpressionTest extends ChangeGeneratorTest {

	public ExpressionTest() {
		super("Replace Expression"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "void main() {\n\tint s = 0, c = 0, h = 0;\n\ts = 3, c = 4, h = 5;\n}"; //$NON-NLS-1$
		expectedSource = "void main() {\n\tint s = 0, c = 0, h = 0;\n\ts = 3, c = 9, h = 5;\n}"; //$NON-NLS-1$
		super.setUp();
	}

	public static Test suite() {		
		return new ExpressionTest();
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
					IASTExpression[] expressions = expressionList.getExpressions();
					CPPASTBinaryExpression binEx = new CPPASTBinaryExpression(IASTBinaryExpression.op_assign,
							new CPPASTIdExpression(new CPPASTName("c".toCharArray())), //$NON-NLS-1$
							new CPPASTLiteralExpression(0, "9".toCharArray())); //$NON-NLS-1$
					ASTModification modification = new ASTModification(ASTModification.ModificationKind.REPLACE,
							expressions[1], binEx, null);
					modStore.storeModification(null, modification);
				}
				return PROCESS_CONTINUE;
			}
		};
	}
}
