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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class StatementTest extends ChangeGeneratorTest {
	
	StatementTest() {
		super("StatementTest");
	}

	public static Test suite() {		
		return new StatementTest();
	}

	@Override
	protected void setUp() throws Exception {
		source = "int f()\r\n{\r\n\tint i = 0;\r\n\tif (i < 1) {\r\n\t\t++i;\r\n\t}\r\n}\r\n"; //$NON-NLS-1$
		expectedSource = "int f()\r\n{\r\n\tint i = 0;\r\n\tif (i < 1) {\r\n\t\ti++;\r\n\t}\r\n}\r\n"; //$NON-NLS-1$
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
				if (statement instanceof IASTIfStatement) {
					IASTIfStatement ifStatement = (IASTIfStatement) statement;
					CPPASTCompoundStatement compound = new CPPASTCompoundStatement();
					
					CPPASTExpressionStatement expr = new CPPASTExpressionStatement(new CPPASTUnaryExpression(IASTUnaryExpression.op_postFixIncr, new CPPASTIdExpression(new CPPASTName("i".toCharArray())))); //$NON-NLS-1$
					compound.addStatement(expr);
					ASTModification modification = new ASTModification(ASTModification.ModificationKind.REPLACE, ifStatement.getThenClause(), compound, null);
					modStore.storeModification(null, modification);
				}

				return PROCESS_CONTINUE;
			}
		};
	}
}
