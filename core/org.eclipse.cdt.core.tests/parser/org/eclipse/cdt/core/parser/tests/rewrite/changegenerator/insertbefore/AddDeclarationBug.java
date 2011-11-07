/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.insertbefore;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

/**
 * @author Emanuel Graf IFS
 */
public class AddDeclarationBug extends ChangeGeneratorTest {

	AddDeclarationBug() {
		super("AddDeclarationBug");
	}

	public static Test suite() {		
		return new AddDeclarationBug();
	}

	@Override
	protected void setUp() throws Exception {
		source = "class A\n{\npublic:\n	A();\n	virtual ~A();\n	int foo();\n	\nprivate:\n	int help();\n};"; //$NON-NLS-1$
		expectedSource = "class A\n{\npublic:\n	A();\n	virtual ~A();\n	int foo();\n	\nprivate:\n	int help();\n\tint exp(int i);\n};"; //$NON-NLS-1$
		super.setUp();
	}

	@Override
	protected ASTVisitor createModificator(final ASTModificationStore modStore) {
		return new ASTVisitor() {
			{
				shouldVisitDeclSpecifiers = true;
			}

			@Override
			public int visit(IASTDeclSpecifier declSpec) {
				if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
					ICPPASTCompositeTypeSpecifier classNode = (ICPPASTCompositeTypeSpecifier) declSpec;
					IASTSimpleDeclaration newDecl = new CPPASTSimpleDeclaration();
					
					IASTSimpleDeclSpecifier returnTyp = new CPPASTSimpleDeclSpecifier();
					returnTyp.setType(IASTSimpleDeclSpecifier.t_int);
					newDecl.setDeclSpecifier(returnTyp);
					
					IASTStandardFunctionDeclarator declarator = new CPPASTFunctionDeclarator(new CPPASTName("exp".toCharArray())); //$NON-NLS-1$
					IASTSimpleDeclSpecifier paramTyp = new CPPASTSimpleDeclSpecifier();
					paramTyp.setType(IASTSimpleDeclSpecifier.t_int);
					IASTDeclarator decl = new CPPASTDeclarator(new CPPASTName("i".toCharArray())); //$NON-NLS-1$
					ICPPASTParameterDeclaration param = new CPPASTParameterDeclaration(paramTyp, decl);
					declarator.addParameterDeclaration(param);
					newDecl.addDeclarator(declarator);
					
					ASTModification mod = new ASTModification(ModificationKind.APPEND_CHILD, classNode, newDecl, null);
					modStore.storeModification(null, mod);
				}
				return PROCESS_CONTINUE;
			}
		};
	}
}
