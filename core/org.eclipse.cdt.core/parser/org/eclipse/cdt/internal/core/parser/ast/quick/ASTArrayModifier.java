/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.internal.core.parser.ast.IASTArrayModifier;

/**
 * @author jcamelon
 *
 */
public class ASTArrayModifier implements IASTArrayModifier
{
	private final IASTExpression expression;
    /**
     * @param exp
     */
    public ASTArrayModifier(IASTExpression exp)
    {
        expression = exp; 
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.ast.IASTArrayModifier#getExpression()
     */
    public IASTExpression getExpression()
    {
        return expression;
    }
}
