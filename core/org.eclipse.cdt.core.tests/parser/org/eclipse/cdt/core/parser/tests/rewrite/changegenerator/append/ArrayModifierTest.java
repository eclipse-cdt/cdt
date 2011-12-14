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
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;

public class ArrayModifierTest extends ChangeGeneratorTest {

	public ArrayModifierTest(){
		super("Replace Array Modifier"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "int *pi[5];"; //$NON-NLS-1$
		expectedSource = "int *pi[5][3];"; //$NON-NLS-1$
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
				if (declarator instanceof IASTArrayDeclarator) {
					IASTArrayDeclarator arrayDeclarator = (IASTArrayDeclarator) declarator;
					arrayDeclarator.getArrayModifiers();
					IASTArrayModifier newModifier = new CPPASTArrayModifier();
					IASTExpression expr = new CPPASTLiteralExpression(
							IASTLiteralExpression.lk_integer_constant, "3".toCharArray()); //$NON-NLS-1$
					newModifier.setConstantExpression(expr);
					ASTModification modification = new ASTModification(ModificationKind.APPEND_CHILD,
							declarator, newModifier, null);
					modStore.storeModification(null, modification);
				}
				return PROCESS_CONTINUE;
			}
		};
	}
	
	public static Test suite() {
		return new ArrayModifierTest();
	}
}
