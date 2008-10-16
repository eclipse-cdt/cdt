/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * Binding for a specialization of a field.
 */
public class CPPFieldSpecialization extends CPPSpecialization implements ICPPField {
	private IType type = null;
	private IValue value= null;

	public CPPFieldSpecialization( IBinding orig, ICPPClassType owner, ObjectMap argMap ) {
		super(orig, owner, argMap);
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
		if (type == null) {
			type= specializeType(getField().getType());
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

    public boolean isExternC() {
    	return false;
    }

	public ICompositeType getCompositeTypeOwner() throws DOMException {
		return getClassOwner();
	}

	public IValue getInitialValue() {
		if (value == null) {
			value= specializeValue(getField().getInitialValue());
		}
		return value;
	}
}
