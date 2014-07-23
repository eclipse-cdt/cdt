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
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.append;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class NestedElseifStatementTest extends ChangeGeneratorTest {

	public NestedElseifStatementTest(){
		super("Append Nested Elseif Statement"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "void foo(bool cond1, bool cond2) {\n}\n"; //$NON-NLS-1$
		expectedSource = "void foo(bool cond1, bool cond2) {\n\tif (cond1) {\n\t} else if (cond2) {\n\t}\n}\n"; //$NON-NLS-1$
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
					IASTCompoundStatement compound = (IASTCompoundStatement) statement;
					INodeFactory factory = statement.getTranslationUnit().getASTNodeFactory();

					IASTIdExpression elseIfCondition = factory.newIdExpression(factory.newName("cond2".toCharArray()));
					IASTStatement elseIfThen = factory.newCompoundStatement();
					IASTIfStatement elseIfStatement = factory.newIfStatement(elseIfCondition, elseIfThen, null);

					IASTIdExpression ifCondition = factory.newIdExpression(factory.newName("cond1".toCharArray()));
					IASTStatement ifThen = factory.newCompoundStatement();
					IASTIfStatement ifStatement = factory.newIfStatement(ifCondition, ifThen, elseIfStatement);

					ASTModification modification = new ASTModification(ModificationKind.APPEND_CHILD, compound, ifStatement, null);
					modStore.storeModification(null, modification);
				}
				return PROCESS_ABORT;
			}
		};
	}

	public static Test suite() {
		return new NestedElseifStatementTest();
	}
}
