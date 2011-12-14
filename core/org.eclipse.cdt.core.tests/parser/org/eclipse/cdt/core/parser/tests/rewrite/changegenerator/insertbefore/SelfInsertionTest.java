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
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.insertbefore;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class SelfInsertionTest extends ChangeGeneratorTest {

	SelfInsertionTest() {
		super("SelfInsertionTest");
	}

	public static Test suite() {		
		return new SelfInsertionTest();
	}

	@Override
	protected void setUp() throws Exception {
		source = "void foo() {\r\n\r\n\tfor (int i = 0; i < 10; i++) {\r\n\t}\r\n}\r\n"; //$NON-NLS-1$
		expectedSource = "void foo() {\r\n\r\n\tfor (int i = 0; i < 10; i++) {\r\n\t\tfor (int i = 0; i < 10; i++) {\r\n\t\t}\r\n\t}\r\n}\r\n"; //$NON-NLS-1$
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
				if (statement instanceof IASTForStatement) {
					IASTForStatement forStatement = (IASTForStatement) statement;
					IASTCompoundStatement compoundStatement = (IASTCompoundStatement) forStatement.getBody();

					ASTModification modification = new ASTModification(ASTModification.ModificationKind.APPEND_CHILD,
							compoundStatement, forStatement, null);
					modStore.storeModification(null, modification);
				}

				return PROCESS_CONTINUE;
			}
		};
	}
}
