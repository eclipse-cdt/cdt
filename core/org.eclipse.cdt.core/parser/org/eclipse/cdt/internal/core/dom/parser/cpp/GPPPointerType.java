/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Mar 11, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPPointerType;

/**
 * @author aniefer
 */
public class GPPPointerType extends CPPPointerType implements IGPPPointerType {
    private boolean isRestrict = false;
    /**
     * @param type
     * @param operator
     */
    public GPPPointerType( IType type, IGPPASTPointer operator ) {
        super( type, operator );
        isRestrict = operator.isRestrict();
    }

    public GPPPointerType( IType type ){
        super( type );
    }
    
    /**
     * @param type
     */
    public GPPPointerType( IType type, boolean isConst, boolean isVolatile, boolean isRestrict ) {
        super( type, isConst, isVolatile );
        this.isRestrict = isRestrict;
    }
    
    public IType stripQualifiers(){
    	GPPPointerType result = (GPPPointerType) super.stripQualifiers();
		
		if( isRestrict ){
			if( result == this ){
				result = (GPPPointerType) clone();
				result.isRestrict = false;
			} else {
				result.isRestrict = false;
			}
		} 
		return result;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer#isRestrict()
     */
    public boolean isRestrict() {
        return isRestrict;
        
    }
    
    public boolean isSameType( IType o ){
        if( !super.isSameType( o ) ) return false;
        
        if( o instanceof IGPPPointerType ){
            return (isRestrict == ((IGPPPointerType) o).isRestrict());
        }
        return (isRestrict == false);
    }
}
