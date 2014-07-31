/*******************************************************************************
 * Copyright (c) 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS)- initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.insertbefore;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class InsertStatementTest extends ChangeGeneratorTest {
	public InsertStatementTest() {
		super("InsertStatementTest");
	}

	public static Test suite() {
		return new InsertStatementTest();
	}

	@Override
	protected void setUp() throws Exception {
		source =
				"void function() {\n" +
				"	int i;\n" +
				"	int j;\n" +
				"}";
		expectedSource =
				"void function() {\n" +
				"	int i;\n" +
				"	int j;\n" +
				"	int j;\n" +
				"}";
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
					ASTModification compoundReplacement = new ASTModification(ModificationKind.REPLACE, statement, statement, null);
					modStore.storeModification(null, compoundReplacement);
					IASTNode secondStatement = statement.getChildren()[1];
					ASTModification modification = new ASTModification(ModificationKind.INSERT_BEFORE, secondStatement, secondStatement, null);
					modStore.storeModification(compoundReplacement, modification);
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		};
	}
}
