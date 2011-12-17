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
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class ArraySizeExpressionTest extends ChangeGeneratorTest {

	ArraySizeExpressionTest() {
		super("ArraySizeExpressionTest");
	}

	public static Test suite() {		
		return new ArraySizeExpressionTest();
	}

	@Override
	protected void setUp() throws Exception {
		source = "int* values = new int[5];"; //$NON-NLS-1$
		expectedSource = "int* values = new int[6][5];"; //$NON-NLS-1$
		super.setUp();
	}

	@Override
	protected ASTVisitor createModificator(final ASTModificationStore modStore) {
		return new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}
			
			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof ICPPASTNewExpression) {
					ICPPASTNewExpression newExpression = (ICPPASTNewExpression) expression;
					IASTTypeId id= newExpression.getTypeId();
					IASTArrayDeclarator dtor= (IASTArrayDeclarator) id.getAbstractDeclarator();
					IASTArrayModifier[] mods= dtor.getArrayModifiers();
					IASTArrayModifier add= new CPPASTArrayModifier(
							new CPPASTLiteralExpression(IASTLiteralExpression.lk_integer_constant, "6".toCharArray()));
					ASTModification modification = new ASTModification(ASTModification.ModificationKind.INSERT_BEFORE,
							mods[0], add, null); 
					modStore.storeModification(null, modification);
				}
				return PROCESS_CONTINUE;
			}
		};
	}
}
