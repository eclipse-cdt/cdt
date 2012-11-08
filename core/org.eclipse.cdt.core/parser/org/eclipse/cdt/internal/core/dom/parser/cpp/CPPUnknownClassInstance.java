/*******************************************************************************
 * Copyright (c) 2008, 2010 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPUnknownMemberClassInstance;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a partially instantiated C++ class template, declaration of which is not yet available.
 *
 * @author Sergey Prigogin
 */
public class CPPUnknownClassInstance extends CPPUnknownMemberClass implements ICPPUnknownMemberClassInstance {
	private final ICPPTemplateArgument[] arguments;

	public CPPUnknownClassInstance(IType scopeBinding, char[] name, ICPPTemplateArgument[] arguments) {
		super(scopeBinding, name);
		this.arguments = arguments;
	}

	@Override
	public ICPPTemplateArgument[] getArguments() {
		return arguments;
	}

	@Override
	public String toString() {
		return getName() + " " + ASTTypeUtil.getArgumentListString(arguments, true); //$NON-NLS-1$ 
	}
	
	@Override
	public boolean isSameType(IType type) {
		if (this == type) 
			return true;
		
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}
		
		if (type instanceof ICPPUnknownMemberClassInstance) { 
			ICPPUnknownMemberClassInstance rhs= (ICPPUnknownMemberClassInstance) type;
			if (CharArrayUtils.equals(getNameCharArray(), rhs.getNameCharArray())) {
				ICPPTemplateArgument[] lhsArgs= getArguments();
				ICPPTemplateArgument[] rhsArgs= rhs.getArguments();
				if (lhsArgs != rhsArgs) {
					if (lhsArgs == null || rhsArgs == null)
						return false;
				
					if (lhsArgs.length != rhsArgs.length)
						return false;
				
					for (int i= 0; i < lhsArgs.length; i++) {
						if (!lhsArgs[i].isSameValue(rhsArgs[i])) 
							return false;
					}
				}
				final IType lhsContainer = getOwnerType();
				final IType rhsContainer = rhs.getOwnerType();
				if (lhsContainer != null && rhsContainer != null) {
					 return (lhsContainer.isSameType(rhsContainer));
				}
			}
		}
		return false;
	}
	
	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		int firstByte= ITypeMarshalBuffer.UNKNOWN_MEMBER_CLASS_INSTANCE;
		buffer.putByte((byte) firstByte);
		buffer.marshalType(getOwnerType());
		buffer.putCharArray(getNameCharArray());
		buffer.putShort((short) arguments.length);
		for (ICPPTemplateArgument arg : arguments) {
			buffer.marshalTemplateArgument(arg);
		}
	}
	
	public static ICPPUnknownMemberClassInstance unmarshal(IIndexFragment fragment, int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		IType owner= buffer.unmarshalType();
		char[] name = buffer.getCharArray();
		int argcount= buffer.getShort() & 0xffff;
		ICPPTemplateArgument[] args = new ICPPTemplateArgument[argcount];
		for (int i = 0; i < argcount; i++) {
			args[i]= buffer.unmarshalTemplateArgument();
		}
		return new PDOMCPPUnknownMemberClassInstance(fragment, owner, name, args);
	}
}
