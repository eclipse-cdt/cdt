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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class MultilineWhitespaceHandlingTest extends ChangeGeneratorTest {

	MultilineWhitespaceHandlingTest() {
		super("MultilineWhitespaceHandlingTest");
	}

	public static Test suite() {		
		return new MultilineWhitespaceHandlingTest();
	}

	@Override
	protected void setUp() throws Exception {
		source = "void foo() {\n\tfor (int i = 0; i < 10; i++) {\n\n\n\t}\n}\n"; //$NON-NLS-1$
		expectedSource = "void foo() {\n\tfor (int i = 0; i < 10; i++) {\n\t\tint i;\n\t\tint j;\n\t}\n}\n"; //$NON-NLS-1$
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

					addIntDeclaration(modStore, compoundStatement, "i"); //$NON-NLS-1$
					addIntDeclaration(modStore, compoundStatement, "j"); //$NON-NLS-1$
				}

				return PROCESS_CONTINUE;
			}

			private void addIntDeclaration(final ASTModificationStore modStore,
					IASTCompoundStatement compoundStatement, String variableName) {
				CPPNodeFactory nf = CPPNodeFactory.getDefault();
				
				ICPPASTSimpleDeclSpecifier newSimpleDeclSpecifier = nf.newSimpleDeclSpecifier();
				newSimpleDeclSpecifier.setType(IASTSimpleDeclSpecifier.t_int);
				IASTSimpleDeclaration newSimpleDeclaration = nf.newSimpleDeclaration(newSimpleDeclSpecifier);
				newSimpleDeclaration.addDeclarator(nf.newDeclarator(nf.newName(variableName.toCharArray())));
				IASTDeclarationStatement newDeclaration = nf.newDeclarationStatement(newSimpleDeclaration);
									
				ASTModification modification = new ASTModification(ASTModification.ModificationKind.APPEND_CHILD,
						compoundStatement, newDeclaration, null);
				modStore.storeModification(null, modification);
			}
		};
	}
}
