/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * Specialization of a typedef in the context of a class-specialization.
 */
public class CPPTypedefSpecialization extends CPPSpecialization implements ITypedef, ITypeContainer {
	public static final int MAX_RESOLUTION_DEPTH = 5;
	public static final int MAX_TYPE_NESTING = 60;

	private IType fType;

    public CPPTypedefSpecialization(IBinding specialized, ICPPClassType owner, ICPPTemplateParameterMap tpmap, IType type) {
        super(specialized, owner, tpmap);
        fType= type;
    }

    @Override
	public IType getType() {
    	return fType;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
	public Object clone() {
    	IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
            // not going to happen
        }
        return t;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    @Override
	public boolean isSameType(IType o) {
        if (o == this)
            return true;
	    if (o instanceof ITypedef) {
            IType t = getType();
			if (t != null)
			    return t.isSameType(((ITypedef) o).getType());
			return false;
	    }
	        
        IType t = getType();
		if (t != null)
		    return t.isSameType(o);
	    return false;
    }

	@Override
	public void setType(IType type) {
		fType = type;
	}
}
