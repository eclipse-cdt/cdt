/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * @author jcamelon
 */
public interface ICPPASTTypenameExpression extends IASTExpression {

    /**
     * @param templateTokenConsumed
     */
    public void setIsTemplate(boolean templateTokenConsumed);
    public boolean isTemplate();

    public static final ASTNodeProperty TYPENAME = new ASTNodeProperty( "Typename" ); //$NON-NLS-1$
    /**
     * @param name
     */
    public void setName(IASTName name);
    public IASTName getName();

    public static final ASTNodeProperty INITIAL_VALUE = new ASTNodeProperty( "Initial Value"); //$NON-NLS-1$
    /**
     * @param expressionList
     */
    public void setInitialValue(IASTExpression expressionList);
    public IASTExpression getInitialValue();

}
