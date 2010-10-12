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
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * Binding for a specialization of a parameter.
 */
public class CPPParameterSpecialization extends CPPSpecialization implements ICPPParameter {
	private IType fType;
	
	public CPPParameterSpecialization(ICPPParameter orig, IBinding owner, IType type, ICPPTemplateParameterMap tpmap) {
		super(orig, owner, tpmap);
		fType= type;
	}

	private ICPPParameter getParameter(){
		return (ICPPParameter) getSpecializedBinding();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	public IType getType() {
		return fType;
	}
	
	public boolean isParameterPack() {
		return fType instanceof ICPPParameterPackType;
	}

	@Override
	public IType specializeType(IType type) {
		assert false;
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
    public boolean isAuto() {
        return getParameter().isAuto();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isRegister()
     */
    public boolean isRegister() {
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
