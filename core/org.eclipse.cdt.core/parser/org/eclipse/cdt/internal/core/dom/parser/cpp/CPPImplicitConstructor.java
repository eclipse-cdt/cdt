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
 * Created on Jan 19, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;

/**
 * @author aniefer
 */
public class CPPImplicitConstructor extends CPPImplicitMethod implements ICPPConstructor {

    /**
     * @param name
     * @param params
     */
    public CPPImplicitConstructor( ICPPClassScope scope, char [] name, IParameter[] params ) {
        super( scope, name, new CPPBasicType( IBasicType.t_unspecified, 0 ), params );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor#isExplicit()
     */
    public boolean isExplicit() {
        return false;
    }
}
