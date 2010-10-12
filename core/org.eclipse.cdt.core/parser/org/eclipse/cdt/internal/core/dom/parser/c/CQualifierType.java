/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Devin Steffler (IBM Rational Software) - Initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.core.runtime.CoreException;

public class CQualifierType implements ICQualifierType, ITypeContainer, ISerializableType {
	private boolean isConst;
	private boolean isVolatile;
	private boolean isRestrict;
	private IType type;

	/**
	 * CQualifierType has an IBasicType to keep track of the basic type information.
	 */
	public CQualifierType(ICASTDeclSpecifier declSpec) {
		this.type = resolveType(declSpec);
		this.isConst = declSpec.isConst();
		this.isVolatile = declSpec.isVolatile();
		this.isRestrict = declSpec.isRestrict();
	}
	
	public CQualifierType(IType type, boolean isConst, boolean isVolatile, boolean isRestrict) {
		this.type = type;
		this.isConst = isConst;
		this.isVolatile = isVolatile;
		this.isRestrict = isRestrict;
	}
	
	public boolean isSameType(IType obj) {
	    if (obj == this)
	        return true;
	    if (obj instanceof ITypedef)
	        return obj.isSameType(this);
	    
	    if (obj instanceof ICQualifierType) {
	        ICQualifierType qt = (ICQualifierType) obj;
            if (isConst() != qt.isConst()) return false;
			if (isRestrict() != qt.isRestrict()) return false;
			if (isVolatile() != qt.isVolatile()) return false;
         
			if (type == null)
				return false;
			return type.isSameType(qt.getType());
        }
    	return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isConst()
	 */
	public boolean isConst() {
		return isConst;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isVolatile()
	 */
	public boolean isVolatile() {
		return isVolatile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICQualifierType#isRestrict()
	 */
	public boolean isRestrict() {
		return isRestrict; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#getType()
	 */
	private IType resolveType(ICASTDeclSpecifier declSpec) {
		IBinding b = null;
		if (declSpec instanceof ICASTTypedefNameSpecifier) {
			ICASTTypedefNameSpecifier nameSpec = (ICASTTypedefNameSpecifier) declSpec;
			b = nameSpec.getName().resolveBinding();			
		} else if (declSpec instanceof IASTElaboratedTypeSpecifier) {
			IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) declSpec;
			b = elabTypeSpec.getName().resolveBinding();
		} else if (declSpec instanceof IASTCompositeTypeSpecifier) {
			IASTCompositeTypeSpecifier compTypeSpec = (IASTCompositeTypeSpecifier) declSpec;
			b = compTypeSpec.getName().resolveBinding();
		} else if (declSpec instanceof IASTEnumerationSpecifier) {
			return new CEnumeration(((IASTEnumerationSpecifier) declSpec).getName());
		} else {
		    return new CBasicType((ICASTSimpleDeclSpecifier) declSpec);
		}
		
		if (b instanceof IType && !(b instanceof IProblemBinding))
			return (IType) b;
		
		return new ProblemType(ISemanticProblem.TYPE_UNRESOLVED_NAME);
	}
	
	public IType getType() {
		return type;
	}

	public void setType(IType t) {
	    type = t;
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

	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		int firstByte= ITypeMarshalBuffer.CVQUALIFIER;
		if (isConst()) firstByte |= ITypeMarshalBuffer.FLAG1;
		if (isVolatile()) firstByte |= ITypeMarshalBuffer.FLAG2;
		if (isRestrict()) firstByte |= ITypeMarshalBuffer.FLAG3;
		buffer.putByte((byte) firstByte);
		buffer.marshalType(getType());
	}
	
	public static IType unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		IType nested= buffer.unmarshalType();
		return new CQualifierType(nested, (firstByte & ITypeMarshalBuffer.FLAG1) != 0,
				(firstByte & ITypeMarshalBuffer.FLAG2) != 0, (firstByte & ITypeMarshalBuffer.FLAG3) != 0);
	}
}
