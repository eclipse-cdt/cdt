/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Apr 13, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;

/**
 * @author aniefer
 */
public class CPPTemplateTypeParameter extends CPPTemplateParameter implements
		ICPPTemplateTypeParameter, IType {

	/**
	 * @param name
	 */
	public CPPTemplateTypeParameter(IASTName name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter#getDefault()
	 */
	public IType getDefault() {
		IASTNode [] nds = getDeclarations();
		if( nds == null || nds.length == 0 )
		    return null;
		IASTName name = (IASTName) nds[0];
		ICPPASTSimpleTypeTemplateParameter simple = (ICPPASTSimpleTypeTemplateParameter) name.getParent();
		IASTTypeId typeId = simple.getDefaultType();
		if( typeId != null )
		    return CPPVisitor.createType( typeId );
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
	 */
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    public boolean isSameType( IType type ) {
        if( type == this )
            return true;
        if( type instanceof ITypedef )
            return ((ITypedef)type).isSameType( this );
        return false;
    }
}
