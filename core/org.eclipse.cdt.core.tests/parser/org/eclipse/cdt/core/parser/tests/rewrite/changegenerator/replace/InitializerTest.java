/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.replace;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTEqualsInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;

public class InitializerTest extends ChangeGeneratorTest {

	public InitializerTest(){
		super("Replace Initializer"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "int hs = 5;"; //$NON-NLS-1$
		expectedSource = "int hs = 999;"; //$NON-NLS-1$
		super.setUp();
	}

	public static Test suite() {
		return new InitializerTest();
	}


	@Override
	protected ASTVisitor createModificator(
			final ASTModificationStore modStore) {
		return new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}
			
			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof CPPASTDeclarator) {
					CPPASTDeclarator fieldDeclarator = (CPPASTDeclarator)declarator;
					IASTInitializer initializer = fieldDeclarator.getInitializer();
					
					CPPASTLiteralExpression litEx = new CPPASTLiteralExpression(0, "999"); //$NON-NLS-1$
					CPPASTEqualsInitializer initExpr = new CPPASTEqualsInitializer(litEx);
					
					ASTModification modification = new ASTModification(ModificationKind.REPLACE, initializer, initExpr, null);
					modStore.storeModification(null, modification);
				}
				return PROCESS_CONTINUE;
			}
		};
	}
}
