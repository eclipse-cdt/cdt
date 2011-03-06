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
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;





public class IdenticalTest extends ChangeGeneratorTest {

	public IdenticalTest(){
		super("Replace Node Same Node"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "#ifndef A_H_\n#define A_H_\n\nclass A {\n\nprivate:\n	int c;\n};\n\n#endif /*A_H_*/\n\n"; //$NON-NLS-1$
		expectedSource = "#ifndef A_H_\n#define A_H_\n\nclass A {\n\nprivate:\n	int c;\n};\n\n#endif /*A_H_*/\n\n"; //$NON-NLS-1$
		super.setUp();
	}

	public static Test suite() {
		return new IdenticalTest();	
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
				ASTModification modification = new ASTModification(ASTModification.ModificationKind.REPLACE, declarator.getName(), declarator.getName(), null);
				modStore.storeModification(null, modification);
				return PROCESS_CONTINUE;
			}
		};
	}
}
