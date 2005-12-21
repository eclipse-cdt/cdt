/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
/*
 * Created on Mar 29, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField.CPPFieldDelegate;

/**
 * @author aniefer
 */
public class CPPFieldSpecialization extends CPPSpecialization implements ICPPField {
	private IType type = null;
	/**
	 * @param orig
	 * @param args
	 * @param args
	 */
	public CPPFieldSpecialization( IBinding orig, ICPPScope scope, ObjectMap argMap ) {
		super(orig, scope, argMap);
	}

	private ICPPField getField() {
		return (ICPPField) getSpecializedBinding();
	}
	
	public int getVisibility() throws DOMException {
		return getField().getVisibility();
	}

	public ICPPClassType getClassOwner() throws DOMException {
		return getField().getClassOwner();
	}
	
	public IType getType() throws DOMException {
		if( type == null ){
			type = CPPTemplates.instantiateType( getField().getType(), argumentMap );
		}
		return type;
	}

	public boolean isStatic() throws DOMException {
		return getField().isStatic();
	}

    public boolean isExtern() throws DOMException {
        return getField().isExtern();
    }

    public boolean isAuto() throws DOMException {
        return getField().isAuto();
    }

    public boolean isRegister() throws DOMException {
        return getField().isRegister();
    }

    public boolean isMutable() throws DOMException {
        return getField().isMutable();
    }

	public ICPPDelegate createDelegate(IASTName name) {
		return new CPPFieldDelegate( name, this );
	}

}
