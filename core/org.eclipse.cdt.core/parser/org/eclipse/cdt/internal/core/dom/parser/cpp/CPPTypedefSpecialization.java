/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * @author aniefer
 */
public class CPPTypedefSpecialization extends CPPSpecialization implements ITypedef, ITypeContainer {
    private IType type;

    /**
     * @param specialized
     * @param scope
     * @param argumentMap
     */
    public CPPTypedefSpecialization(IBinding specialized, ICPPScope scope, ObjectMap argumentMap) {
        super(specialized, scope, argumentMap);
    }

    private ITypedef getTypedef() {
        return (ITypedef) getSpecializedBinding();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.ITypedef#getType()
     */
    public IType getType() throws DOMException {
        if (type == null) {
            type = CPPTemplates.instantiateType(getTypedef().getType(), argumentMap);
	    }
		return type;
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
    public boolean isSameType(IType o) {
        if (o == this)
            return true;
	    if (o instanceof ITypedef) {
            try {
                IType t = getType();
                if (t != null)
                    return t.isSameType(((ITypedef) o).getType());
                return false;
            } catch (DOMException e) {
                return false;
            }
	    }
	        
        try {
		    IType t = getType();
		    if (t != null)
		        return t.isSameType(o);
        } catch (DOMException e) {
            return false;
        }
	    return false;
    }

	public void setType(IType type) {
		this.type = type;
	}
}
