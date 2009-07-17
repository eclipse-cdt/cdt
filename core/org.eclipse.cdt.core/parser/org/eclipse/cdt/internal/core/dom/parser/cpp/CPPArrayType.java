/*******************************************************************************
 *  Copyright (c) 2004, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.Value;

public class CPPArrayType implements IArrayType, ITypeContainer {
    private IType type;
    private IASTExpression sizeExpression;
    private IValue value= Value.NOT_INITIALIZED;

    public CPPArrayType(IType type) {
        this.type = type;
    }
    
    public CPPArrayType(IType type, IValue value) {
    	this.type= type;
    	this.value= value;
    }
    
    public CPPArrayType(IType type, IASTExpression sizeExp) {
        this.type = type;
        this.sizeExpression = sizeExp;
    }
    
    public IType getType() {
        return type;
    }
    
    public void setType(IType t) {
        this.type = t;
    }
    
    public boolean isSameType(IType obj) {
        if (obj == this)
            return true;
        if (obj instanceof ITypedef)
            return ((ITypedef) obj).isSameType(this);
        
        if (obj instanceof IArrayType) {
            final IArrayType rhs = (IArrayType) obj;
			IType objType = rhs.getType();
			if (objType != null) {
				if (objType.isSameType(type)) {
					IValue s1= getSize();
					IValue s2= rhs.getSize();
					if (s1 == s2)
						return true;
					if (s1 == null || s2 == null)
						return false;
					return CharArrayUtils.equals(s1.getSignature(), s2.getSignature());
				}
			}
        }
    	return false;
    }

    public IValue getSize() {
    	if (value != Value.NOT_INITIALIZED)
    		return value;
    	
    	if (sizeExpression == null)
    		return value= null;

    	return value= Value.create(sizeExpression, Value.MAX_RECURSION_DEPTH);
    }
    
    @Deprecated
    public IASTExpression getArraySizeExpression() {
        return sizeExpression;
    }

    @Override
	public Object clone() {
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
            // Not going to happen
        }
        return t;
    }

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
