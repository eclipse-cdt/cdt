/*
 * Created on Feb 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CPPPointerToMemberType extends CPPPointerType implements
		ICPPPointerToMemberType {

	private ICPPClassType clsType = null;
	/**
	 * @param type
	 * @param operator
	 */
	public CPPPointerToMemberType(IType type, ICPPASTPointerToMember operator) {
		super(type, operator);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType#getMemberOfClass()
	 */
	public ICPPClassType getMemberOfClass() {
		if( clsType == null ){ 
			ICPPASTPointerToMember pm = (ICPPASTPointerToMember) operator;
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
				clsType = new CPPClassType.CPPClassTypeProblem( IProblemBinding.SEMANTIC_INVALID_TYPE, name.toCharArray() );
			}
		}
		return clsType;
	}

}
