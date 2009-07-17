/*******************************************************************************
 *  Copyright (c) 2004, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Devin Steffler (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.index.IIndexType;

public class CArrayType implements ICArrayType, ITypeContainer {
	IType type;
	ICASTArrayModifier mod;
	
	public CArrayType(IType type) {
		this.type = type;
	}
	
    public boolean isSameType(IType obj) {
        if (obj == this)
            return true;
        if (obj instanceof ITypedef || obj instanceof IIndexType)
            return obj.isSameType(this);
        if (obj instanceof ICArrayType) {
        	ICArrayType at = (ICArrayType) obj;
        	try {
        		if (isConst() != at.isConst()) return false;
        		if (isRestrict() != at.isRestrict()) return false;
        		if (isStatic() != at.isStatic()) return false;
        		if (isVolatile() != at.isVolatile()) return false;
        		if (isVariableLength() != at.isVariableLength()) return false;

        		return at.getType().isSameType(type) && hasSameSize(at);
        	} catch (DOMException e) {
        		return false;
        	}
        }
    	return false;
    }
    
	private boolean hasSameSize(IArrayType rhs) {
		IValue s1 = getSize();
		IValue s2 = rhs.getSize();
		if (s1 == s2)
			return true;
		if (s1 == null || s2 == null)
			return false;
		return CharArrayUtils.equals(s1.getSignature(), s2.getSignature());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IArrayType#getType()
	 */
	public IType getType() {
		return type;
	}
	
	public void setType(IType t) {
	    this.type = t;
	}
	
	public void setModifier(ICASTArrayModifier mod) {
		this.mod = mod;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isConst()
	 */
	public boolean isConst() {
		if (mod == null) return false;
		return mod.isConst();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isRestrict()
	 */
	public boolean isRestrict() {
		if (mod == null) return false;
		return mod.isRestrict();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isVolatile()
	 */
	public boolean isVolatile() {
		if (mod == null) return false;
		return mod.isVolatile();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isStatic()
	 */
	public boolean isStatic() {
		if (mod == null) return false;
		return mod.isStatic();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isVariableLength()
	 */
	public boolean isVariableLength() {
		if (mod == null) return false;
		return mod.isVariableSized();
	}

    public ICASTArrayModifier getModifier() {
        return mod;
    }

    public IValue getSize() {
    	if (mod != null) {
    		IASTExpression sizeExpression = mod.getConstantExpression();
    		if (sizeExpression != null) {
    			return Value.create(sizeExpression, Value.MAX_RECURSION_DEPTH);
    		}
    	}
    	return null;
    }

	@Deprecated
    public IASTExpression getArraySizeExpression() {
        if (mod != null)
            return mod.getConstantExpression();
        return null;
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
