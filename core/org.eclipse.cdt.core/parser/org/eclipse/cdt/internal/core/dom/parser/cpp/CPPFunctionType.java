/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Represents c++ function types. Note that we keep typedefs as part of the function type.
 */
public class CPPFunctionType implements ICPPFunctionType {
    private IType[] parameters;
    private IType returnType;
    private boolean isConst;
    private boolean isVolatile;
    
    /**
     * @param returnType
     * @param types
     */
    public CPPFunctionType(IType returnType, IType[] types) {
        this.returnType = returnType;
        this.parameters = types;
    }

	public CPPFunctionType(IType returnType, IType[] types, boolean isConst, boolean isVolatile) {
        this.returnType = returnType;
        this.parameters = types;
        this.isConst = isConst;
        this.isVolatile= isVolatile;
    }

    public boolean isSameType(IType o) {
        if (o instanceof ITypedef)
            return o.isSameType(this);
        if (o instanceof ICPPFunctionType) {
            ICPPFunctionType ft = (ICPPFunctionType) o;
            IType[] fps;
            fps = ft.getParameterTypes();
			//constructors & destructors have null return type
			if ((returnType == null) ^ (ft.getReturnType() == null))
			    return false;
			else if (returnType != null && ! returnType.isSameType(ft.getReturnType()))
			    return false;
			
			if (parameters.length == 1 && fps.length == 0) {
				IType p0= SemanticUtil.getNestedType(parameters[0], SemanticUtil.TDEF);
				if (!(p0 instanceof IBasicType) || ((IBasicType) p0).getKind() != Kind.eVoid)
					return false;
			} else if (fps.length == 1 && parameters.length == 0) {
				IType p0= SemanticUtil.getNestedType(fps[0], SemanticUtil.TDEF);
				if (!(p0 instanceof IBasicType) || ((IBasicType) p0).getKind() != Kind.eVoid)
					return false;
			} else if (parameters.length != fps.length) {
			    return false;
			} else {
				for (int i = 0; i < parameters.length; i++) {
			        if (parameters[i] == null || ! parameters[i].isSameType(fps[i]))
			            return false;
			    }
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

    @Deprecated
    public IPointerType getThisType() {
        return null;
    }

	public final boolean isConst() {
		return isConst;
	}

	public final boolean isVolatile() {
		return isVolatile;
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
