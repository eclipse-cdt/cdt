/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

/**
 * Models pointer to a composite type member.
 */
public class CPPPointerToMemberType extends CPPPointerType implements ICPPPointerToMemberType {
	private ICPPASTPointerToMember operator;
	private IType classType; // Can be either ICPPClassType or ICPPTemplateTypeParameter

	/**
	 * @param type
	 * @param operator
	 */
	public CPPPointerToMemberType(IType type, ICPPASTPointerToMember operator) {
		super(type, operator);
		this.operator = operator;
	}

	public CPPPointerToMemberType(IType type, IType classType, boolean isConst, boolean isVolatile,
			boolean isRestrict) {
		super(type, isConst, isVolatile, isRestrict);
		this.classType = classType;
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

	@Override
	public IType getMemberOfClass() {
		if (classType == null) {
			ICPPASTNameSpecifier nameSpec;
			IBinding binding = null;
			ICPPASTPointerToMember pm = operator;
			if (pm == null) {
				nameSpec = new CPPASTName();
			} else {
				nameSpec = (ICPPASTName) pm.getName();
				if (nameSpec instanceof ICPPASTQualifiedName) {
					ICPPASTQualifiedName qname = ((ICPPASTQualifiedName) nameSpec);
					ICPPASTNameSpecifier[] qualifier = qname.getQualifier();
					if (qualifier.length > 0) {
						nameSpec = qualifier[qualifier.length - 1];
					} else {
						nameSpec = (ICPPASTName) qname.getLastName();
					}
				}
				binding = nameSpec.resolvePreBinding();
			}
			if (binding instanceof IType) {
				classType = (IType) binding;
			} else {
				classType = new CPPClassType.CPPClassTypeProblem(nameSpec, IProblemBinding.SEMANTIC_INVALID_TYPE,
						nameSpec.toCharArray());
			}
		}
		return classType;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.POINTER_TO_MEMBER_TYPE;
		if (isConst())
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		if (isVolatile())
			firstBytes |= ITypeMarshalBuffer.FLAG2;
		if (isRestrict())
			firstBytes |= ITypeMarshalBuffer.FLAG3;
		buffer.putShort(firstBytes);
		buffer.marshalType(getType());
		buffer.marshalType(getMemberOfClass());
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		IType nested = buffer.unmarshalType();
		IType memberOf = buffer.unmarshalType();
		return new CPPPointerToMemberType(nested, memberOf, (firstBytes & ITypeMarshalBuffer.FLAG1) != 0,
				(firstBytes & ITypeMarshalBuffer.FLAG2) != 0, (firstBytes & ITypeMarshalBuffer.FLAG3) != 0);
	}
}
