/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Feb 11, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;

/**
 * @author aniefer
 */
public class CPPPointerToMemberType extends CPPPointerType implements
		ICPPPointerToMemberType {
    private ICPPASTPointerToMember operator = null;
	private ICPPClassType clsType = null;
	/**
	 * @param type
	 * @param operator
	 */
	public CPPPointerToMemberType(IType type, ICPPASTPointerToMember operator) {
		super(type, operator);
		this.operator = operator;
	}

	public boolean isSameType( IType o ){
	    if( !super.isSameType( o ) )
	        return false;
	    
	    if( !( o instanceof CPPPointerToMemberType ) ) 
	        return false;   
	    
	    CPPPointerToMemberType pt = (CPPPointerToMemberType) o;
	    ICPPClassType cls = pt.getMemberOfClass();
	    if( cls != null )
	        return cls.isSameType( getMemberOfClass() );
	    return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType#getMemberOfClass()
	 */
	public ICPPClassType getMemberOfClass() {
		if( clsType == null ){ 
			ICPPASTPointerToMember pm = operator;
			IASTName name = pm.getName();
			if( name instanceof ICPPASTQualifiedName ){
				IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
				if( ns.length > 1 )
					name = ns[ ns.length - 2 ];
				else 
					name = ns[ ns.length - 1 ]; 
			}
			
			IBinding binding = name.resolveBinding();
			if( binding instanceof ICPPClassType ){
				clsType = (ICPPClassType) binding;
			} else {
				clsType = new CPPClassType.CPPClassTypeProblem( name, IProblemBinding.SEMANTIC_INVALID_TYPE, name.toCharArray() );
			}
		}
		return clsType;
	}

}
