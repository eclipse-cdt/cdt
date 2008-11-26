/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.internal.core.index.IIndexType;

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

	public CPPPointerToMemberType(IType type, ICPPClassType thisType, boolean isConst, boolean isVolatile) {
		super(type, isConst, isVolatile);
		this.classType = thisType;
	}

	@Override
	public boolean isSameType(IType o) {
	    if (o == this)
            return true;
        if (o instanceof ITypedef || o instanceof IIndexType)
            return o.isSameType(this);

	    if (!super.isSameType(o))
	        return false;
	    
	    if (!(o instanceof CPPPointerToMemberType)) 
	        return false;   
	    
	    CPPPointerToMemberType pt = (CPPPointerToMemberType) o;
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
			ICPPASTPointerToMember pm = operator;
			IASTName name = pm.getName();
			if (name instanceof ICPPASTQualifiedName) {
				IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
				if (ns.length > 1)
					name = ns[ns.length - 2];
				else 
					name = ns[ns.length - 1]; 
			}
			
			IBinding binding = CPPASTNameBase.resolvePreBinding(name);
			if (binding instanceof IType) {
				classType = (IType) binding;
			} else {
				classType = new CPPClassType.CPPClassTypeProblem(name, IProblemBinding.SEMANTIC_INVALID_TYPE, name.toCharArray());
			}
		}
		return classType;
	}

	@Override
	public boolean isConst() {
		return super.isConst() || (getType() instanceof ICPPFunctionType && ((ICPPFunctionType) getType()).isConst()); 
	}

	@Override
	public boolean isVolatile() {
		return super.isVolatile() || (getType() instanceof ICPPFunctionType && ((ICPPFunctionType) getType()).isVolatile()); 
	}
}
