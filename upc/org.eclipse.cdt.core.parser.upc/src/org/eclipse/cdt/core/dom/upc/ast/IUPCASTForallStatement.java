/*******************************************************************************
 *  Copyright (c) 2006, 2011 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.upc.ast;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public interface IUPCASTForallStatement extends IASTForStatement {

	public static final ASTNodeProperty AFFINITY = new ASTNodeProperty(
			"IUPCASTForallStatement.AFFINITY - IASTExpression affinity for IUPCASTForallStatement"); //$NON-NLS-1$

	@Override
	public IASTStatement getInitializerStatement();

	@Override
	public void setInitializerStatement(IASTStatement statement);

	@Override
	public IASTExpression getConditionExpression();

	@Override
	public void setConditionExpression(IASTExpression condition);

	@Override
	public IASTExpression getIterationExpression();

	@Override
	public void setIterationExpression(IASTExpression iterator);

	public IASTExpression getAffinityExpresiion();

	public void setAffinityExpression(IASTExpression affinity);

	public boolean isAffinityContinue();

	public void setAffinityContinue(boolean affinityContinue);

	@Override
	public IASTStatement getBody();

	@Override
	public void setBody(IASTStatement statement);

	@Override
	public IUPCASTForallStatement copy();
}
