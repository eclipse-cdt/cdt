/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.upc.ast;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public interface IUPCASTForallStatement extends IASTForStatement {	

	public static final ASTNodeProperty CONDITION = new ASTNodeProperty(
			"IUPCASTForallStatement.CONDITION - IASTExpression condition of IUPCASTForallStatement"); //$NON-NLS-1$

	public static final ASTNodeProperty ITERATION = new ASTNodeProperty(
			"IUPCASTForallStatement.ITERATION - IASTExpression iteration of IUPCASTForallStatement"); //$NON-NLS-1$

	public static final ASTNodeProperty BODY = new ASTNodeProperty(
			"IUPCASTForallStatement.BODY - IASTStatement body of IUPCASTForallStatement"); //$NON-NLS-1$

    public static final ASTNodeProperty INITIALIZER = new ASTNodeProperty(
            "IUPCASTForallStatement.INITIALIZER - initializer for IUPCASTForallStatement"); //$NON-NLS-1$
    
    public static final ASTNodeProperty AFFINITY = new ASTNodeProperty(
    		"IUPCASTForallStatement.AFFINITY - IASTExpression affinity for IUPCASTForallStatement"); //$NON-NLS-1$
    
    
    public IASTStatement getInitializerStatement();

    public void setInitializerStatement( IASTStatement statement );
   
	public IASTExpression getConditionExpression();

	public void setConditionExpression(IASTExpression condition);

	public IASTExpression getIterationExpression();

	public void setIterationExpression(IASTExpression iterator);
	
	public IASTExpression getAffinityExpresiion();
	
	public void setAffinityExpression(IASTExpression affinity);
	
	public boolean isAffinityContinue();
	
	public void setAffinityContinue(boolean affinityContinue);

	public IASTStatement getBody();

	public void setBody(IASTStatement statement);
}
