/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Represents c++ function types. Note that we keep typedefs as part of the function type.
 */
public class CPPFunctionType implements ICPPFunctionType {
    private IType[] parameters;
    private IType returnType;
    private IPointerType thisType;
    
    /**
     * @param returnType
     * @param types
     */
    public CPPFunctionType(IType returnType, IType[] types) {
        this.returnType = returnType;
        this.parameters = types;
    }

	public CPPFunctionType(IType returnType, IType[] types, IPointerType thisType) {
        this.returnType = returnType;
        this.parameters = types;
        this.thisType = thisType;
    }

    public boolean isSameType(IType o) {
        if (o instanceof ITypedef)
            return o.isSameType(this);
        if (o instanceof ICPPFunctionType) {
            ICPPFunctionType ft = (ICPPFunctionType) o;
            IType[] fps;
            try {
                fps = ft.getParameterTypes();
            } catch (DOMException e) {
                return false;
            }
			try {
                //constructors & destructors have null return type
                if ((returnType == null) ^ (ft.getReturnType() == null))
                    return false;
                else if (returnType != null && ! returnType.isSameType(ft.getReturnType()))
                    return false;
            } catch (DOMException e1) {
                return false;
            }
			
			try {
				if (parameters.length == 1 && fps.length == 0) {
					IType p0= SemanticUtil.getUltimateTypeViaTypedefs(parameters[0]);
					if (!(p0 instanceof IBasicType) || ((IBasicType) p0).getType() != IBasicType.t_void)
						return false;
				} else if (fps.length == 1 && parameters.length == 0) {
					IType p0= SemanticUtil.getUltimateTypeViaTypedefs(fps[0]);
					if (!(p0 instanceof IBasicType) || ((IBasicType) p0).getType() != IBasicType.t_void)
						return false;
				} else if (parameters.length != fps.length) {
	                return false;
	            } else {
					for (int i = 0; i < parameters.length; i++) {
		                if (parameters[i] == null || ! parameters[i].isSameType(fps[i]))
		                    return false;
		            }
	            }
			} catch (DOMException e) {
				return false;
			}
           
            if (isConst() != ft.isConst() || isVolatile() != ft.isVolatile()) {
                return false;
            }
                
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunctionType#getReturnType()
     */
    public IType getReturnType() {
        return returnType;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunctionType#getParameterTypes()
     */
    public IType[] getParameterTypes() {
        return parameters;
    }

    @Override
	public Object clone() {
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
            //not going to happen
        }
        return t;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.ICPPFunctionType#getThisType()
     */
    public IPointerType getThisType() {
        return thisType;
    }

	public final boolean isConst() {
		return thisType != null && thisType.isConst();
	}

	public final boolean isVolatile() {
		return thisType != null && thisType.isVolatile();
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
