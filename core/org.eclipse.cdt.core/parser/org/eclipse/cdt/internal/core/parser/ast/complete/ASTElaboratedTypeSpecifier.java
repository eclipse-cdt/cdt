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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;

/**
 * @author jcamelon
 *
 */
public class ASTElaboratedTypeSpecifier implements IASTElaboratedTypeSpecifier
{
    /**
     * 
     */
    public ASTElaboratedTypeSpecifier()
    {
        super();
        // TODO Auto-generated constructor stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier#getName()
     */
    public String getName()
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier#getClassKind()
     */
    public ASTClassKind getClassKind()
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public void setStartingOffset(int o)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public void setEndingOffset(int o)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public int getStartingOffset()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public int getEndingOffset()
    {
        // TODO Auto-generated method stub
        return 0;
    }
}
