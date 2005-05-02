/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Dec 15, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPBaseClause implements ICPPBase {
    static public class CPPBaseProblem extends ProblemBinding implements ICPPBase {
        public CPPBaseProblem( IASTNode node, int id, char[] arg ) {
            super( node, id, arg );
        }
        public ICPPClassType getBaseClass() throws DOMException {
            throw new DOMException( this );
        }

        public int getVisibility() throws DOMException {
            throw new DOMException( this );
        }

        public boolean isVirtual() throws DOMException {
            throw new DOMException( this );
        }
    }
    private ICPPASTBaseSpecifier base = null;
	private ICPPClassType baseClass = null;
    
    public CPPBaseClause( ICPPASTBaseSpecifier base ){
        this.base = base;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBase#getBaseClass()
     */
    public ICPPClassType getBaseClass() {
		if( baseClass == null ){
	    	IBinding b = base.getName().resolveBinding();
	    	if( b instanceof ICPPClassType )
	    		baseClass = (ICPPClassType) b;
	    	else if( b instanceof IProblemBinding ){
	    		baseClass =  new CPPClassType.CPPClassTypeProblem( base.getName(), ((IProblemBinding)b).getID(), base.getName().toCharArray() );
	    	} else {
				baseClass = new CPPClassType.CPPClassTypeProblem( base.getName(), IProblemBinding.SEMANTIC_NAME_NOT_FOUND, base.getName().toCharArray() );
	    	}
		}
		return baseClass;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBase#getVisibility()
     */
    public int getVisibility() {
		int vis = base.getVisibility();
		
		if( vis == 0 ){
			ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) base.getParent();
			int key = compSpec.getKey();
			if( key == ICPPClassType.k_class )
				vis = ICPPBase.v_private;
			else
				vis = ICPPBase.v_public;
		}
        return vis;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBase#isVirtual()
     */
    public boolean isVirtual() {
        return base.isVirtual();
    }

	public void setBaseClass(ICPPClassType cls) {
		baseClass = cls;
	}

}
