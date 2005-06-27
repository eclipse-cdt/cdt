/*******************************************************************************
 * Copyright (c) 2002, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jcamelon
 *
 */
public class ParameterCollection implements IParameterCollection
{
	private List list = new ArrayList(); 
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IParameterCollection#getParameters()
     */
    public List getParameters()
    {
        return list;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IParameterCollection#addParameter(org.eclipse.cdt.internal.core.parser.DeclarationWrapper)
     */
    public void addParameter(DeclarationWrapper param)
    {
        list.add( param ); 
    }
}
