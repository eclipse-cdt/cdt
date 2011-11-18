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
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.replace;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class MoveTest extends ChangeGeneratorTest {

	MoveTest() {
		super("MoveTest");
	}

	public static Test suite() {		
		return new MoveTest();
	}

	@Override
	protected void setUp() throws Exception {
		source = "#ifndef A_H_\r\n#define A_H_\r\n\r\nclass A {\r\n\r\nprivate:\r\n\tint b;\r\n\tint a;\r\n};\r\n\r\n#endif /*A_H_*/\r\n\r\n"; //$NON-NLS-1$
		expectedSource = "#ifndef A_H_\r\n#define A_H_\r\n\r\nclass A {\r\n\r\nprivate:\r\n\tint a;\r\n\tint b;\r\n};\r\n\r\n#endif /*A_H_*/\r\n\r\n"; //$NON-NLS-1$
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
					CPPASTCompositeTypeSpecifier classSpecifier = (CPPASTCompositeTypeSpecifier) declSpec;
					IASTDeclaration[] members = classSpecifier.getMembers();
					ASTModification swap1 = new ASTModification(ASTModification.ModificationKind.REPLACE, members[1], members[2], null);
					ASTModification swap2 = new ASTModification(ASTModification.ModificationKind.REPLACE, members[2], members[1], null);
					modStore.storeModification(null, swap1);
					modStore.storeModification(null, swap2);
				}
				return super.visit(declSpec);
			}
		};
	}
}
