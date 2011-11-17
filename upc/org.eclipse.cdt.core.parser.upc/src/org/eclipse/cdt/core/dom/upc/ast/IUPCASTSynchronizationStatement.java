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
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public interface IUPCASTSynchronizationStatement extends IASTStatement {


	public static final ASTNodeProperty BARRIER_EXPRESSION = new ASTNodeProperty(
		"IUPCASTSynchronizationStatement.BARRIER_EXPRESSION - IASTExpression barrier for IUPCASTSynchronizationStatement"); //$NON-NLS-1$


	public final int st_upc_notify = 1;

	public final int st_upc_wait = 2;

	public final int st_upc_barrier = 3;

	public final int st_upc_fence = 4;


	public IASTExpression getBarrierExpression();

	public void setBarrierExpression(IASTExpression expr);

	public int getStatementKind();

	public void setStatementKind(int kind);


	@Override
	public IUPCASTSynchronizationStatement copy();
}
