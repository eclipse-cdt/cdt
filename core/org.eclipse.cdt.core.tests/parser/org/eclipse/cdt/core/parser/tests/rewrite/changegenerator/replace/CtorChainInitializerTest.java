/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik  
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
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;

public class CtorChainInitializerTest extends ChangeGeneratorTest {

	public CtorChainInitializerTest(){
		super("Replace Ctor Initializer"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "TestClass::TestClass(int a):beta(b){\n"
				+ "}\n"
				+ "\n"; //$NON-NLS-1$
		expectedSource = "TestClass::TestClass(int a) :\n"
				+ "		alpha(a) {\n"
				+ "}\n"
				+ "\n"; //$NON-NLS-1$
		super.setUp();
	}

	public static Test suite() {
		return new CtorChainInitializerTest();
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
				if (declarator instanceof CPPASTFunctionDeclarator) {
					CPPASTFunctionDeclarator functionDeclarator = (CPPASTFunctionDeclarator)declarator;
					ICPPASTConstructorChainInitializer[] ctorInitializers = functionDeclarator.getConstructorChain();
					for(ICPPASTConstructorChainInitializer curInitializer : ctorInitializers){		
						CPPASTIdExpression initExpr = new CPPASTIdExpression(new CPPASTName("a".toCharArray())); //$NON-NLS-1$
						CPPASTName initName = new CPPASTName("alpha".toCharArray()); //$NON-NLS-1$
						ICPPASTConstructorChainInitializer newInitializer = new CPPASTConstructorChainInitializer(initName, null);
						newInitializer.setInitializerValue(initExpr);
						ASTModification modification = new ASTModification(ModificationKind.REPLACE, curInitializer, newInitializer, null);
						modStore.storeModification(null, modification);
					}
				}
				return PROCESS_CONTINUE;
			}
		};
	}
}
