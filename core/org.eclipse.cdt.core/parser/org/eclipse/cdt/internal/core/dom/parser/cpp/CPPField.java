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
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;

/**
 * @author aniefer
 */
public class CPPField extends CPPVariable implements ICPPField, ICPPInternalBinding {
    public static class CPPFieldDelegate extends CPPVariable.CPPVariableDelegate implements ICPPField {
        public CPPFieldDelegate( IASTName name, ICPPField binding ) {
            super( name, binding );
        }
        public int getVisibility() throws DOMException {
            return ((ICPPField)getBinding()).getVisibility();
        }
    }
    public static class CPPFieldProblem extends CPPVariable.CPPVariableProblem implements ICPPField {
        /**
         * @param id
         * @param arg
         */
        public CPPFieldProblem( int id, char[] arg ) {
            super( id, arg );
        }

        public int getVisibility() throws DOMException {
            throw new DOMException( this );
        }
        public boolean isStatic() throws DOMException {
            throw new DOMException( this );
        }
    }
    
	public CPPField( IASTName name ){
		super( name );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPMember#getVisibility()
	 */
	public int getVisibility() {
		// TODO Auto-generated method stub
		return 0;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public ICPPDelegate createDelegate( IASTName name ) {
        return new CPPFieldDelegate( name, this );
    }
}
