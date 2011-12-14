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
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.append;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTPointer;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;

public class PointerToPointerParameterTest extends ChangeGeneratorTest {

	public PointerToPointerParameterTest(){
		super("Append Pointer to Pointer Parameter"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "void foo(int *parameter) {\n}\n\n"; //$NON-NLS-1$
		expectedSource = "void foo(int **parameter) {\n}\n\n"; //$NON-NLS-1$
		super.setUp();
	}
	
	@Override
	protected ASTVisitor createModificator(final ASTModificationStore modStore) {
		return new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}
			
			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof CPPASTFunctionDeclarator) {
					CPPASTFunctionDeclarator functionDeclarator = (CPPASTFunctionDeclarator) declarator;
					IASTParameterDeclaration[] parameters = functionDeclarator.getParameters();
					for (IASTParameterDeclaration curParam : parameters){
						if (String.valueOf(curParam.getDeclarator().getName().toCharArray()).equals("parameter")){ //$NON-NLS-1$
							CPPASTPointer addedPointer = new CPPASTPointer();
							ASTModification modification = new ASTModification(ModificationKind.APPEND_CHILD,
									curParam.getDeclarator(), addedPointer, null);
							modStore.storeModification(null, modification);
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		};
	}
	
	public static Test suite() {
		return new PointerToPointerParameterTest();
	}
}
