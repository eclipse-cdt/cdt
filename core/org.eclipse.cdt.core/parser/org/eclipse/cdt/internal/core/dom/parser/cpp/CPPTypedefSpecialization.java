/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.core.runtime.Assert;

/**
 * Specialization of a typedef in the context of a class-specialization.
 */
public class CPPTypedefSpecialization extends CPPSpecialization implements ITypedef, ITypeContainer {
	final static class RecursionResolvingBinding extends ProblemBinding {
		public RecursionResolvingBinding(IASTNode node, char[] arg) {
			super(node, IProblemBinding.SEMANTIC_RECURSION_IN_LOOKUP, arg);
			Assert.isTrue(CPPASTName.sAllowRecursionBindings, getMessage());
		}
	}
	
	public static final int MAX_RESOLUTION_DEPTH = 5;

	private IType type;
    private int fResolutionDepth;

    public CPPTypedefSpecialization(IBinding specialized, ICPPClassType owner, 
    		ICPPTemplateParameterMap tpmap) {
        super(specialized, owner, tpmap);
    }

    private ITypedef getTypedef() {
        return (ITypedef) getSpecializedBinding();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.ITypedef#getType()
     */
    public IType getType() throws DOMException {
        if (type == null) {
        	try {
	        	if (++fResolutionDepth > MAX_RESOLUTION_DEPTH) {
	        		type = new RecursionResolvingBinding(getDefinition(), getNameCharArray());
	        	} else {
		            type= specializeType(getTypedef().getType());
		        	// A typedef pointing to itself is a sure recipe for an infinite loop -- replace
		            // with a problem binding.
		            if (type instanceof ITypedef && type instanceof ICPPSpecialization) {
		            	ITypedef td= (ITypedef) type;
		            	if (CharArrayUtils.equals(td.getNameCharArray(), getNameCharArray())) {
			            	IBinding owner= ((ICPPSpecialization) type).getOwner();
			            	if (owner instanceof IType) {
			            		if (((IType) owner).isSameType((ICPPClassType) getOwner())) {
					        		type = new RecursionResolvingBinding(getDefinition(), getNameCharArray());
			            		}
			            	}
		            	}
		            }
	        	}
        	} finally {
        		--fResolutionDepth;
        	}
	    }
		return type;
    }

	public int incResolutionDepth(int increment) {
		fResolutionDepth += increment;
		return fResolutionDepth;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
	public Object clone() {
    	IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
            // not going to happen
        }
        return t;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    public boolean isSameType(IType o) {
        if (o == this)
            return true;
	    if (o instanceof ITypedef) {
            try {
                IType t = getType();
                if (t != null)
                    return t.isSameType(((ITypedef) o).getType());
                return false;
            } catch (DOMException e) {
                return false;
            }
	    }
	        
        try {
		    IType t = getType();
		    if (t != null)
		        return t.isSameType(o);
        } catch (DOMException e) {
            return false;
        }
	    return false;
    }

	public void setType(IType type) {
		this.type = type;
	}
}
