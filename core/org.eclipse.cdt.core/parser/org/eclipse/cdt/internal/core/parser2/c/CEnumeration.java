/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Nov 23, 2004
 */
package org.eclipse.cdt.internal.core.parser2.c;

import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;

/**
 * @author aniefer
 */
public class CEnumeration implements IEnumeration {

    private final ICASTEnumerationSpecifier enumSpec;
    public CEnumeration( ICASTEnumerationSpecifier spec ){
		spec = checkForDefinition( spec );
		this.enumSpec = spec;
	}
	
	private ICASTEnumerationSpecifier checkForDefinition( ICASTEnumerationSpecifier spec ){
	    return spec;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        return enumSpec.getName().toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        // TODO Auto-generated method stub
        return null;
    }

}
