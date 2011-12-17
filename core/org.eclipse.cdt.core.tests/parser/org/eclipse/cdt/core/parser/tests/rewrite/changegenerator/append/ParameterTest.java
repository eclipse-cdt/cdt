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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class ParameterTest extends ChangeGeneratorTest {

	public ParameterTest(){
		super("Append Parameter to List"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "void foo(int existing) {\n}\n"; //$NON-NLS-1$
		expectedSource = "void foo(int existing, int newParameter) {\n}\n"; //$NON-NLS-1$
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
					
					CPPASTParameterDeclaration insertedParameter = new CPPASTParameterDeclaration();
					CPPASTDeclarator parameterDeclarator = new CPPASTDeclarator();
					CPPASTName parameterName = new CPPASTName("newParameter".toCharArray()); //$NON-NLS-1$
					parameterDeclarator.setName(parameterName);
					insertedParameter.setDeclarator(parameterDeclarator);
					CPPASTSimpleDeclSpecifier parameterDeclSpec = new CPPASTSimpleDeclSpecifier();
					parameterDeclSpec.setType(IASTSimpleDeclSpecifier.t_int);
					insertedParameter.setDeclSpecifier(parameterDeclSpec);
					ASTModification modification = new ASTModification(ModificationKind.APPEND_CHILD,
							functionDeclarator, insertedParameter, null);
					modStore.storeModification(null, modification);	
				}
				return PROCESS_CONTINUE;
			}
		};
	}
	
	public static Test suite() {
		return new ParameterTest();
	}
}
