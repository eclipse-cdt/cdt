/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

/*
 * Created on Mar 11, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPPointerToMemberType;
import org.eclipse.cdt.internal.core.index.IIndexType;

/**
 * @author aniefer
 */
public class GPPPointerToMemberType extends CPPPointerToMemberType implements
        IGPPPointerToMemberType {

    private boolean isRestrict = false;
    /**
     * @param type
     * @param operator
     */
    public GPPPointerToMemberType( IType type, IGPPASTPointerToMember operator ) {
        super( type, operator );
        this.isRestrict = operator.isRestrict();
    }

    public IType stripQualifiers(){
		GPPPointerToMemberType result = (GPPPointerToMemberType) super.stripQualifiers();
		
		if( isRestrict ){
			if( result == this ){
				result = (GPPPointerToMemberType) clone();
				result.isRestrict = false;
			} else {
				result.isRestrict = false;
			}
		} 
		return result;
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPPointerType#isRestrict()
     */
    public boolean isRestrict() {
        return isRestrict;
    }

    public boolean isSameType( IType o ){
    	if (o==this) {
    		return true;
    	}
    	if (o instanceof ITypedef || o instanceof IIndexType) {
    		return o.isSameType(this);
    	}
    		
        if( !super.isSameType( o ) ) return false;
        if( o instanceof IGPPPointerToMemberType ){
            return (isRestrict == ((IGPPPointerToMemberType) o).isRestrict());
        }
        return (isRestrict == false);
    }
}
