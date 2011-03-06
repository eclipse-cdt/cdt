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
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.replace;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;





public class SameNameTest extends ChangeGeneratorTest {

	public SameNameTest(){
		super("Replace Name Node Same Name"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {

		source = "#ifndef A_H_\r\n#define A_H_\r\n\r\nclass A {\r\n\r\nprivate:\r\n	int c;\r\n};\r\n\r\n#endif /*A_H_*/\r\n\r\n"; //$NON-NLS-1$
		expectedSource = "#ifndef A_H_\r\n#define A_H_\r\n\r\nclass A {\r\n\r\nprivate:\r\n	int c;\r\n};\r\n\r\n#endif /*A_H_*/\r\n\r\n"; //$NON-NLS-1$
		super.setUp();
	}

	public static Test suite() {
		return new SameNameTest();
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
				char[] newName = new char[]{'c'};
				IASTName name = new CPPASTName(newName);
				ASTModification modification = new ASTModification(ASTModification.ModificationKind.REPLACE, declarator.getName(), name, null);
				modStore.storeModification(null, modification);
				return PROCESS_CONTINUE;
			}
			
		};
	}
	
	
}
