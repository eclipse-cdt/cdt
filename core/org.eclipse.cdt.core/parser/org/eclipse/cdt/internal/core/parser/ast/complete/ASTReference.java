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

import org.eclipse.cdt.core.parser.ast.IASTReference;


/**
 * @author jcamelon
 *
 */
public abstract class ASTReference implements IASTReference
{
    protected final String name;
    protected final int offset;
    /**
     * 
     */
    public ASTReference(int offset, String name)
    {
        this.offset = offset; 
        this.name = name;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTReference#getOffset()
     */
    public int getOffset()
    {
        return offset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTReference#getName()
     */
    public String getName()
    {
        return name;
    }
    
	public boolean equals(Object obj)
	{
		if( obj == null )
			return false;
		if( ! (obj instanceof IASTReference ) )
			return false;
		
		if( ((IASTReference)obj).getName().equals( getName() ) && 
			((IASTReference)obj).getOffset() == getOffset()  )
			return true;
		return false;
	}
}
