/*******************************************************************************
 *  Copyright (c) 2006, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.upc.ast;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;

public interface IUPCASTDeclSpecifier extends ICASTDeclSpecifier {

	public static final ASTNodeProperty BLOCK_SIZE_EXPRESSION = new ASTNodeProperty(
		"IUPCASTDeclSpecifier.BLOCK_SIZE_EXPRESSION - IUPCASTDeclSpecifier block size expression"); //$NON-NLS-1$


	public static final int rt_unspecified = 0;

	public static final int rt_strict = 1;

	public static final int rt_relaxed = 2;



	public static final int sh_not_shared = 0;

	public static final int sh_shared_default_block_size = 1;

	public static final int sh_shared_pure_allocation = 2;

	public static final int sh_shared_indefinite_allocation = 3;

	public static final int sh_shared_constant_expression = 4;



	public int getReferenceType();

	public void setReferenceType(int referenceType);



	public int getSharedQualifier();

	public void setSharedQualifier(int shared);



	public IASTExpression getBlockSizeExpression();

	public void setBlockSizeExpression(IASTExpression expr);


	@Override
	public IUPCASTDeclSpecifier copy();
}
