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
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class MultilineWhitespaceHandlingTest extends ChangeGeneratorTest {
	
	public MultilineWhitespaceHandlingTest(){
		super("Whitespace Handling in Replace"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "void foo(){\r\n\r\n  for(int i = 0; i < 10; i++){\r\n\r\n  }\r\n\r\n"; //$NON-NLS-1$
		expectedSource = "void foo(){\r\n\r\n  for(int i = 0; i < 10; i++){    int i;\r\n    int j;\r\n\r\n  }\r\n\r\n"; //$NON-NLS-1$
		super.setUp();
	}

	public static Test suite() {		
		return new MultilineWhitespaceHandlingTest();
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
					CPPNodeFactory nf = CPPNodeFactory.getDefault();
					
					
					
					ASTModification modification = new ASTModification(ASTModification.ModificationKind.APPEND_CHILD, compoundStatement, null, null);
					modStore.storeModification(null, modification);
				}

				return PROCESS_CONTINUE;
			}
		};
	}
}
