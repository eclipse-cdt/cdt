/*******************************************************************************
 * Copyright (c) 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.replace;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class NestedReplaceTest extends ChangeGeneratorTest {

	public NestedReplaceTest(){
		super("Nested Replace"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "void foo(int x) {\n"
				+ "	x += 1;\n"
				+ "}"; //$NON-NLS-1$
		expectedSource = "void foo(int x) {\n"
				+ "	x++;\n"
				+ "}"; //$NON-NLS-1$
		super.setUp();
	}

	public static Test suite() {
		return new NestedReplaceTest();
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
					IASTCompoundStatement compoundStatement = (IASTCompoundStatement) statement;
					INodeFactory factory = statement.getTranslationUnit().getASTNodeFactory();

					IASTCompoundStatement newCompoundStatement = factory.newCompoundStatement();
					IASTNullStatement dummyStatement = factory.newNullStatement();
					newCompoundStatement.addStatement(dummyStatement);
					ASTModification compoundReplacement = new ASTModification(ModificationKind.REPLACE, compoundStatement, newCompoundStatement, null);
					modStore.storeModification(null, compoundReplacement);

					IASTName emptyName = factory.newName();
					IASTExpression idExpression = factory.newIdExpression(emptyName);
					IASTExpression incrementExpression = factory.newUnaryExpression(IASTUnaryExpression.op_postFixIncr, idExpression);
					IASTExpressionStatement newStatement = factory.newExpressionStatement(incrementExpression);
					IASTStatement replacedStatement = compoundStatement.getStatements()[0];
					ASTModification statementModification = new ASTModification(ModificationKind.REPLACE, dummyStatement, newStatement, null);
					modStore.storeModification(compoundReplacement, statementModification);

					IASTName xName = factory.newName("x".toCharArray());
					ASTModification nameModification = new ASTModification(ModificationKind.REPLACE, emptyName, xName, null);
					modStore.storeModification(statementModification, nameModification);
				}
				return PROCESS_ABORT;
			}
		};
	}
}
