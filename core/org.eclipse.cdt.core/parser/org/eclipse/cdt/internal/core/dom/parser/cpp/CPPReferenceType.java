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
 * Created on Dec 15, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;

/**
 * @author aniefer
 */
public class CPPReferenceType implements ICPPReferenceType {
    IType type = null;
    
    /**
     * @param type
     * @param operator
     */
    public CPPReferenceType( IType type ) {
        this.type = type;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType#getType()
     */
    public IType getType() {
        return type;
    }

}
