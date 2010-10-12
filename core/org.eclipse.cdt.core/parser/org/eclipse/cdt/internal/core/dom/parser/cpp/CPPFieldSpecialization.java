/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.IInternalVariable;
import org.eclipse.cdt.internal.core.dom.parser.Value;

/**
 * Binding for a specialization of a field.
 */
public class CPPFieldSpecialization extends CPPSpecialization implements ICPPField, IInternalVariable {
	private IType type = null;
	private IValue value= null;

	public CPPFieldSpecialization( IBinding orig, ICPPClassType owner, ICPPTemplateParameterMap tpmap) {
		super(orig, owner, tpmap);
	}

	private ICPPField getField() {
		return (ICPPField) getSpecializedBinding();
	}
	
	public int getVisibility() {
		return getField().getVisibility();
	}

	public ICPPClassType getClassOwner() {
		return getField().getClassOwner();
	}
	
	public IType getType() {
		if (type == null) {
			type= specializeType(getField().getType());
		}
		return type;
	}

	public boolean isStatic() {
		return getField().isStatic();
	}

    public boolean isExtern() {
        return getField().isExtern();
    }

    public boolean isAuto() {
        return getField().isAuto();
    }

    public boolean isRegister() {
        return getField().isRegister();
    }

    public boolean isMutable() {
        return getField().isMutable();
    }

    public boolean isExternC() {
    	return false;
    }

	public ICompositeType getCompositeTypeOwner() {
		return getClassOwner();
	}

	public IValue getInitialValue() {
		return getInitialValue(Value.MAX_RECURSION_DEPTH);
	}

	public IValue getInitialValue(int maxRecursionDepth) {
		if (value == null) {
			ICPPField field= getField();
			IValue v;
			if (field instanceof IInternalVariable) {
				v= ((IInternalVariable) field).getInitialValue(maxRecursionDepth);
			} else {
				v= getField().getInitialValue();
			}
			value= specializeValue(v, maxRecursionDepth);
		}
		return value;
	}
}
