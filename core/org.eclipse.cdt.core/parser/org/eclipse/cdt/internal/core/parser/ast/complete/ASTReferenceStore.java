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

import java.util.Collections;
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
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTReferenceStore#processReferences()
     */
    public static void processReferences(List references, ISourceElementRequestor requestor)
    {
    	if( references == null || references == Collections.EMPTY_LIST || references.isEmpty() )
    		return;
        Iterator i = references.iterator(); 
        while( i.hasNext() )
        	((IASTReference)i.next()).acceptElement(requestor);
        references.clear();
    }
}
