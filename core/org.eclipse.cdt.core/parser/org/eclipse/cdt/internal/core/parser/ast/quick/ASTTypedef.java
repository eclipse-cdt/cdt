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

import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTypedef;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;

/**
 * @author jcamelon
 *
 */
public class ASTTypedef extends ASTDeclaration implements IASTTypedef
{
    private final String name;
    private final IASTAbstractDeclaration mapping;
    private NamedOffsets offsets = new NamedOffsets(); 
    /**
     * @param scope
     * @param name
     * @param mapping
     */
    public ASTTypedef(IASTScope scope, String name, IASTAbstractDeclaration mapping, int startingOffset, int nameOffset)
    {
        super( scope );
        this.name = name; 
        this.mapping = mapping;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypedef#getName()
     */
    public String getName()
    {
        return name;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypedef#getAbstractDeclarator()
     */
    public IASTAbstractDeclaration getAbstractDeclarator()
    {
        return mapping;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getElementNameOffset()
     */
    public int getElementNameOffset()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
     */
    public void setNameOffset(int o)
    {
        // TODO Auto-generated method stub
        
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
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementStartingOffset()
     */
    public int getElementStartingOffset()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementEndingOffset()
     */
    public int getElementEndingOffset()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
