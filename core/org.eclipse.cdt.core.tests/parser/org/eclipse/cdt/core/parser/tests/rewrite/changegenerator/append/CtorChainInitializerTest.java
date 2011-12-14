/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class CtorChainInitializerTest extends ChangeGeneratorTest {

	public CtorChainInitializerTest(){
		super("Append Ctor Initializer"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "TestClass::TestClass(int a, int b) :\n\t\tbeta(b) {\n}\n"; //$NON-NLS-1$
		expectedSource = "TestClass::TestClass(int a, int b) :\n\t\tbeta(b), alpha(a) {\n}\n"; //$NON-NLS-1$
		super.setUp();
	}

	@Override
	protected ASTVisitor createModificator(final ASTModificationStore modStore) {
		return new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}
			
			@Override
			public int visit(IASTDeclaration decl) {
				if (decl instanceof CPPASTFunctionDefinition) {
					CPPASTFunctionDefinition fdef = (CPPASTFunctionDefinition) decl;
					CPPASTIdExpression initExpr = new CPPASTIdExpression(new CPPASTName("a".toCharArray())); //$NON-NLS-1$
					CPPASTName initName = new CPPASTName("alpha".toCharArray()); //$NON-NLS-1$
					ICPPASTConstructorChainInitializer newInitializer = new CPPASTConstructorChainInitializer(initName, null);
					newInitializer.setInitializerValue(initExpr);
					ASTModification modification = new ASTModification(ModificationKind.APPEND_CHILD,
							fdef, newInitializer, null);
					modStore.storeModification(null, modification);
				}
				return PROCESS_CONTINUE;
			}
		};
	}

	public static Test suite() {
		return new CtorChainInitializerTest();
	}
}
