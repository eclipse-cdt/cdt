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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification;

/**
 * @author jcamelon
 *
 */
public class ASTExceptionSpecification implements IASTExceptionSpecification
{
	private final List typeIds; 
    /**
     * @param typeIds
     */
    public ASTExceptionSpecification(List typeIds)
    {
    	Iterator i = typeIds.iterator();
    	this.typeIds = new ArrayList();
    	while( i.hasNext() )
    		this.typeIds.add( ((ITokenDuple)i.next()).toString() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification#getTypeIds()
     */
    public Iterator getTypeIds()
    {
        return typeIds.iterator();
    }
}
