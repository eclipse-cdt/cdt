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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPBaseClause implements ICPPBase {
    static public class CPPBaseProblem extends ProblemBinding implements ICPPBase {
        public CPPBaseProblem( int id, char[] arg ) {
            super( id, arg );
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
    ICPPASTBaseSpecifier base = null;
    
    public CPPBaseClause( ICPPASTBaseSpecifier base ){
        this.base = base;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBase#getBaseClass()
     */
    public ICPPClassType getBaseClass() {
    	IBinding baseClass = base.getName().resolveBinding();
    	if( baseClass instanceof ICPPClassType )
    		return (ICPPClassType) baseClass;
    	else if( baseClass instanceof IProblemBinding ){
    		return new CPPClassType.CPPClassTypeProblem( ((IProblemBinding)baseClass).getID(), base.getName().toCharArray() );
    	}
    	
        return new CPPClassType.CPPClassTypeProblem( IProblemBinding.SEMANTIC_NAME_NOT_FOUND, base.getName().toCharArray() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBase#getVisibility()
     */
    public int getVisibility() {
        return base.getVisibility();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBase#isVirtual()
     */
    public boolean isVirtual() {
        return base.isVirtual();
    }

}
