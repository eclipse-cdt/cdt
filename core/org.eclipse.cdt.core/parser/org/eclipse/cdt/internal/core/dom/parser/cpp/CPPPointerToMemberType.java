/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

/**
 * Models pointer to members.
 */
public class CPPPointerToMemberType extends CPPPointerType implements ICPPPointerToMemberType {
	private ICPPASTPointerToMember operator;
	private IType classType;  // Can be either ICPPClassType or ICPPTemplateTypeParameter

	/**
	 * @param type
	 * @param operator
	 */
	public CPPPointerToMemberType(IType type, ICPPASTPointerToMember operator) {
		super(type, operator);
		this.operator = operator;
	}

	public CPPPointerToMemberType(IType type, IType thisType, boolean isConst, boolean isVolatile, boolean isRestrict) {
		super(type, isConst, isVolatile, isRestrict);
		this.classType = thisType;
	}

	@Override
	public boolean isSameType(IType o) {
	    if (o == this)
            return true;
        if (o instanceof ITypedef)
            return o.isSameType(this);

	    if (!(o instanceof ICPPPointerToMemberType)) 
	        return false;   

	    if (!super.isSameType(o))
	        return false;
	    
	    ICPPPointerToMemberType pt = (ICPPPointerToMemberType) o;
	    IType cls = pt.getMemberOfClass();
	    if (cls != null)
	        return cls.isSameType(getMemberOfClass());
	    return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType#getMemberOfClass()
	 */
	public IType getMemberOfClass() {
		if (classType == null) {
			IASTName name;
			IBinding binding= null;
			ICPPASTPointerToMember pm = operator;
			if (pm == null) {
				name= new CPPASTName();
			} else {
				name = pm.getName();
				if (name instanceof ICPPASTQualifiedName) {
					IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
					if (ns.length > 1)
						name = ns[ns.length - 2];
					else 
						name = ns[ns.length - 1]; 
				}
				binding = name.resolvePreBinding();
			}
			if (binding instanceof IType) {
				classType = (IType) binding;
			} else {
				classType = new CPPClassType.CPPClassTypeProblem(name, IProblemBinding.SEMANTIC_INVALID_TYPE, name.toCharArray());
			}
		}
		return classType;
	}
	
	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		int firstByte= ITypeMarshalBuffer.POINTER_TO_MEMBER;
		if (isConst()) firstByte |= ITypeMarshalBuffer.FLAG1;
		if (isVolatile()) firstByte |= ITypeMarshalBuffer.FLAG2;
		if (isRestrict()) firstByte |= ITypeMarshalBuffer.FLAG3;
		buffer.putByte((byte) firstByte);
		buffer.marshalType(getType());
		buffer.marshalType(getMemberOfClass());
	}
	
	public static IType unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		IType nested= buffer.unmarshalType();
		IType memberOf= buffer.unmarshalType();
		return new CPPPointerToMemberType(nested, memberOf, (firstByte & ITypeMarshalBuffer.FLAG1) != 0,
				(firstByte & ITypeMarshalBuffer.FLAG2) != 0,
				(firstByte & ITypeMarshalBuffer.FLAG3) != 0);
	}
}
