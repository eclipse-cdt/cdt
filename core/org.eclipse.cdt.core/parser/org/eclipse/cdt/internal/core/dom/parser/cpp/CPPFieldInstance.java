/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Mar 29, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */
public class CPPFieldInstance extends CPPInstance implements ICPPField {
	private IType type = null;
	/**
	 * @param orig
	 * @param args
	 */
	public CPPFieldInstance(ICPPScope scope, IBinding orig, ObjectMap argMap ) {
		super(scope, orig, argMap);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPMember#getVisibility()
	 */
	public int getVisibility() throws DOMException {
		return ((ICPPField)getOriginalBinding()).getVisibility();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	public IType getType() throws DOMException {
		if( type == null ){
			type = CPPTemplates.instantiateType( ((ICPPField)getOriginalBinding()).getType(), getArgumentMap() );
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#isStatic()
	 */
	public boolean isStatic() throws DOMException {
		return ((ICPPField)getOriginalBinding()).isStatic();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
	 */
	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
	 */
	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
	 */
	public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while( scope != null ){
            if( scope instanceof ICPPBlockScope || 
            	scope.getScopeName() == null || 
				scope.getScopeName().toCharArray().length == 0 )
            {
                return false;
            }
            scope = scope.getParent();
        }
        return true;
    }

}
