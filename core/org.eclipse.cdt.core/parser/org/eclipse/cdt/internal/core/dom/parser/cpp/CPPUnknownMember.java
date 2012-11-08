/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPUnknownField;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPUnknownMemberClass;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPUnknownMethod;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a binding that is unknown because it depends on template arguments.
 */
public class CPPUnknownMember extends CPPUnknownBinding	implements ICPPUnknownMember, ISerializableType {
    protected IType fOwner;

    protected CPPUnknownMember(IType owner, char[] name) {
        super(name);
        fOwner= owner;
    }

	@Override
	public IBinding getOwner() {
		if (fOwner instanceof IBinding)
			return (IBinding) fOwner;
		return null;
	}
	
	@Override
	public IType getOwnerType() {
		return fOwner;
	}
	
	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		int firstByte= ITypeMarshalBuffer.UNKNOWN_MEMBER;
		if (this instanceof ICPPField) {
			firstByte |= ITypeMarshalBuffer.FLAG1;
		} else if (this instanceof ICPPMethod) {
			firstByte |= ITypeMarshalBuffer.FLAG2;
		}
		
		buffer.putByte((byte) firstByte);
		buffer.marshalType(getOwnerType());
		buffer.putCharArray(getNameCharArray());
	}

	public static IBinding unmarshal(IIndexFragment fragment, int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		IType owner= buffer.unmarshalType();
		char[] name = buffer.getCharArray();
		if ((firstByte & ITypeMarshalBuffer.FLAG1) != 0) {
			return new PDOMCPPUnknownField(fragment, owner, name);
		} else if ((firstByte & ITypeMarshalBuffer.FLAG2) != 0) {
			return new PDOMCPPUnknownMethod(fragment, owner, name);
		}
		return new PDOMCPPUnknownMemberClass(fragment, owner, name);
	}
}
