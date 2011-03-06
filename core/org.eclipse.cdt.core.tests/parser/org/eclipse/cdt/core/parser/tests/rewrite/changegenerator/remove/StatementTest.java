/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.remove;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;





public class StatementTest extends ChangeGeneratorTest {

	public StatementTest(){
		super("Remove Then-Statement"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "int f()\r\n{\r\n\tint i = 0;\r\n\tif(i < 1){\r\n\t\t++i;\r\n\t}\r\n}\r\n"; //$NON-NLS-1$
		expectedSource = "int f()\r\n{\r\n\tint i = 0;\r\n}\r\n"; //$NON-NLS-1$
		super.setUp();
	}

	public static Test suite() {		
		return new StatementTest();
	}


	@Override
	protected ASTVisitor createModificator(
			final ASTModificationStore modStore) {
		return new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}
			
			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTIfStatement) {
					IASTIfStatement ifStatement = (IASTIfStatement) statement;

					ASTModification modification = new ASTModification(ASTModification.ModificationKind.REPLACE, ifStatement, null, null);
					modStore.storeModification(null, modification);
				}

				return PROCESS_CONTINUE;
			}
		};
	}
}
