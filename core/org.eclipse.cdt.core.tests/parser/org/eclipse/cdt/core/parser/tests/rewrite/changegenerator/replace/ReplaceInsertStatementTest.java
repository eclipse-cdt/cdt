/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class ReplaceInsertStatementTest extends ChangeGeneratorTest {

	ReplaceInsertStatementTest() {
		super("ReplaceInsertStatementTest");
	}

	public static Test suite() {		
		return new ReplaceInsertStatementTest();
	}

	@Override
	protected void setUp() throws Exception {
		source = "void main() {\r\n\tint i = 0;\r\n\t++i;\r\n}"; //$NON-NLS-1$
		expectedSource = "void main() {\r\n\tint i = 0;\r\n\ti = 42;\r\n\ti++;\r\n}"; //$NON-NLS-1$
		super.setUp();
	}

	@Override
	protected ASTVisitor createModificator(final ASTModificationStore modStore) {
		return new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTCompoundStatement) {
					IASTCompoundStatement compStmt = (IASTCompoundStatement) statement;
					IASTStatement stmt = compStmt.getStatements()[1];

					IASTIdExpression id = new CPPASTIdExpression(new CPPASTName("i".toCharArray()));
					IASTLiteralExpression value = new CPPASTLiteralExpression(
							IASTLiteralExpression.lk_integer_constant, "42".toCharArray());
					IASTExpressionStatement insertStmt = new CPPASTExpressionStatement(
							new CPPASTBinaryExpression(
									IASTBinaryExpression.op_assign, id, value));

					IASTIdExpression incId = new CPPASTIdExpression(
							new CPPASTName("i".toCharArray()));
					IASTUnaryExpression incExp = new CPPASTUnaryExpression(
							IASTUnaryExpression.op_postFixIncr, incId);
					IASTExpressionStatement replaceStatement = new CPPASTExpressionStatement(
							incExp);

					ASTModification replaceMod = new ASTModification(
							ASTModification.ModificationKind.REPLACE, stmt, replaceStatement, null);
					modStore.storeModification(null, replaceMod);

					ASTModification insertMod = new ASTModification(
							ASTModification.ModificationKind.INSERT_BEFORE, stmt, insertStmt, null);
					modStore.storeModification(null, insertMod);
				}

				return PROCESS_CONTINUE;
			}
		};
	}

}
