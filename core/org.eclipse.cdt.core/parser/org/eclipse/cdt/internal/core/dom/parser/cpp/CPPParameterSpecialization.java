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
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

/**
 * Binding for a specialization of a parameter.
 */
public class CPPParameterSpecialization extends CPPSpecialization implements ICPPParameter {
	private IType type = null;
	
	public CPPParameterSpecialization(ICPPParameter orig, IBinding owner, CPPTemplateParameterMap tpmap) {
		super(orig, owner, tpmap);
	}

	private ICPPParameter getParameter(){
		return (ICPPParameter) getSpecializedBinding();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	public IType getType() throws DOMException {
		if( type == null ){
			type= specializeType(getParameter().getType());
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#isStatic()
	 */
	public boolean isStatic() {
		return false;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isExtern()
     */
    public boolean isExtern() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isAuto()
     */
    public boolean isAuto() throws DOMException {
        return getParameter().isAuto();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isRegister()
     */
    public boolean isRegister() throws DOMException {
        return getParameter().isRegister();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable#isMutable()
     */
    public boolean isMutable() {
        return false;
    }

	public boolean hasDefaultValue() {
		return getParameter().hasDefaultValue();
	}

	public boolean isExternC() {
		return false;
	}

	public IValue getInitialValue() {
		return null;
	}
}
