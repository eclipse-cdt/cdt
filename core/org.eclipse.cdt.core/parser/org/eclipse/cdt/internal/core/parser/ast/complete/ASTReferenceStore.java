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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTReference;

/**
 * @author jcamelon
 *
 */
public class ASTReferenceStore
{
	private List references = new ArrayList();
	
	public ASTReferenceStore( List assortedReferences )
	{
		references.addAll( assortedReferences );
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTReferenceStore#processReferences()
     */
    public void processReferences(ISourceElementRequestor requestor)
    {
        Iterator i = references.iterator(); 
        while( i.hasNext() )
        	((IASTReference)i.next()).acceptElement(requestor);
        references.clear();
    }
}
