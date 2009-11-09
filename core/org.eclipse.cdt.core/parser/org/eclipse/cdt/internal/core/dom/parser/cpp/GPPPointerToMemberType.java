/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPPointerToMemberType;

public class GPPPointerToMemberType extends CPPPointerToMemberType implements IGPPPointerToMemberType {
    private boolean isRestrict;
    
    public GPPPointerToMemberType(IType type, IGPPASTPointerToMember operator) {
        super(type, operator);
        this.isRestrict = operator.isRestrict();
    }

    @Override
	public IType stripQualifiers() {
		GPPPointerToMemberType result = (GPPPointerToMemberType) super.stripQualifiers();
		
		if (isRestrict) {
			if (result == this) {
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

    @Override
	public boolean isSameType(IType o) {
		if (o == this) {
			return true;
		}
		if (o instanceof ITypedef) {
			return o.isSameType(this);
		}

		if (!super.isSameType(o))
			return false;
		if (o instanceof IGPPPointerToMemberType) {
			return (isRestrict == ((IGPPPointerToMemberType) o).isRestrict());
		}
		return (isRestrict == false);
	}
}
