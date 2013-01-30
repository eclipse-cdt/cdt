/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *	   Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;

public class CPPBaseClause implements ICPPBase, ICPPInternalBase {
    private final ICPPASTBaseSpecifier base;
	private IType baseClass;
    
    public CPPBaseClause(ICPPASTBaseSpecifier base) {
        this.base = base;
    }
    
    @Override
	public IBinding getBaseClass() {
		IType type= getBaseClassType();
		type = getNestedType(type, TDEF);
		if (type instanceof IBinding)
			return (IBinding) type;
		return null;
    }

    @Override
    public IType getBaseClassType() {
		if (baseClass == null) {
	    	IBinding b = base.getName().resolveBinding();
	    	if (b instanceof IProblemBinding) {
	    		baseClass =  new CPPClassType.CPPClassTypeProblem(base.getName(), ((IProblemBinding) b).getID());
	    	} else if (!(b instanceof IType)) {
	    		baseClass =  new CPPClassType.CPPClassTypeProblem(base.getName(), ISemanticProblem.BINDING_NO_CLASS);
	    	} else {
	    		baseClass= (IType) b;
	    		IType check= getNestedType(baseClass, TDEF);
	    		if (!(check instanceof ICPPClassType || check instanceof ICPPUnknownType)) {
	    			baseClass = new CPPClassType.CPPClassTypeProblem(base.getName(), ISemanticProblem.BINDING_NO_CLASS);
	    		}
	    	}
		}
		return baseClass;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBase#getVisibility()
     */
    @Override
	public int getVisibility() {
		int vis = base.getVisibility();
		
		if (vis == 0) {
			ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) base.getParent();
			int key = compSpec.getKey();
			if (key == ICPPClassType.k_class)
				vis = ICPPBase.v_private;
			else
				vis = ICPPBase.v_public;
		}
        return vis;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBase#isVirtual()
     */
    @Override
	public boolean isVirtual() {
        return base.isVirtual();
    }

	@Override
	public void setBaseClass(IBinding cls) {
		if (cls instanceof IType)
			baseClass = (IType) cls;
	}

	@Override
	public void setBaseClass(IType cls) {
		baseClass = cls;
	}

	@Override
	public IName getBaseClassSpecifierName() {
		return base.getName();
	}

    /* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBase#getClassDefinitionName()
	 */
	@Override
	public IName getClassDefinitionName() {
		IASTNode parent = base.getParent();
		if (parent instanceof ICPPASTCompositeTypeSpecifier) {
			return ((ICPPASTCompositeTypeSpecifier) parent).getName();
		}
		return null;
	}

	@Override
	public ICPPBase clone() {
        ICPPBase t = null;
   		try {
            t = (ICPPBase) super.clone();
        } catch (CloneNotSupportedException e) {
            //not going to happen
        }
        return t;
    }
}
