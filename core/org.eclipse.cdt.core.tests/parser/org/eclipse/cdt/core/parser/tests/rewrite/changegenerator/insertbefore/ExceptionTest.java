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
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.insertbefore;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class ExceptionTest extends ChangeGeneratorTest {

	ExceptionTest() {
		super("ExceptionTest");
	}

	public static Test suite() {		
		return new ExceptionTest();
	}
	
	@Override
	protected void setUp() throws Exception {
		source = "void foo(int parameter) throw (/*Test*/float) /*Test2*/{\n}\n\n"; //$NON-NLS-1$
		expectedSource = "void foo(int parameter) throw (int, /*Test*/float) /*Test2*/{\n}\n\n"; //$NON-NLS-1$
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
					CPPASTFunctionDeclarator functionDeclarator = (CPPASTFunctionDeclarator)declarator;
					IASTTypeId existingException = functionDeclarator.getExceptionSpecification()[0];
					
					IASTTypeId exception = new CPPASTTypeId();
					CPPASTDeclarator exceptionDeclarator = new CPPASTDeclarator();
					exceptionDeclarator.setName(new CPPASTName());
					CPPASTSimpleDeclSpecifier exDeclSpec = new CPPASTSimpleDeclSpecifier();
					exDeclSpec.setType(IASTSimpleDeclSpecifier.t_int);
					exception.setDeclSpecifier(exDeclSpec);
					exception.setAbstractDeclarator(exceptionDeclarator);
					ASTModification modification = new ASTModification(ModificationKind.INSERT_BEFORE, existingException, exception, null);
					modStore.storeModification(null, modification);
					
				}
				return PROCESS_CONTINUE;
			}
		};
	}
}
