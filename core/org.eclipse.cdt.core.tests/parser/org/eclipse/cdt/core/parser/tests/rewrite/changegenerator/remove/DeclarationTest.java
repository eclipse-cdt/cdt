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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;





public class DeclarationTest extends ChangeGeneratorTest {

	public DeclarationTest(){
		super("Remove Declaration Node"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "#ifndef A_H_\n#define A_H_\n\nclass A {\n\nprivate:\n	int b;\n	int c;\n};\n\n#endif /*A_H_*/\n\n"; //$NON-NLS-1$
		expectedSource = "#ifndef A_H_\n#define A_H_\n\nclass A {\n\nprivate:\n	int b;\n};\n\n#endif /*A_H_*/\n\n"; //$NON-NLS-1$
		super.setUp();
	}

	public static Test suite() {
		
		return new DeclarationTest();
	}

	@Override
	protected ASTVisitor createModificator(
			final ASTModificationStore modStore) {
		return new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}
			
			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof CPPASTSimpleDeclaration) {
					CPPASTSimpleDeclaration simpleDeclaration = (CPPASTSimpleDeclaration) declaration;
					if(simpleDeclaration.getDeclarators().length > 0){
						String name = String.valueOf(simpleDeclaration.getDeclarators()[0].getName().toCharArray());
						if(name.equals("c")){ //$NON-NLS-1$
							ASTModification modification = new ASTModification(ASTModification.ModificationKind.REPLACE, declaration, null, null);
							modStore.storeModification(null, modification);
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		};
	}
}
