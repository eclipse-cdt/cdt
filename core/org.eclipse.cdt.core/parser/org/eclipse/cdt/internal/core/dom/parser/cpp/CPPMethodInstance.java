/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;

/**
 * The result of instantiating a method template.
 */
public class CPPMethodInstance extends CPPFunctionInstance implements ICPPMethod {

	public CPPMethodInstance(ICPPMethod orig, ICPPClassType owner, CPPTemplateParameterMap tpmap, ICPPTemplateArgument[] args) {
		super(orig, owner, tpmap, args);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPMember#getVisibility()
	 */
	public int getVisibility() throws DOMException {
		return ((ICPPMethod)getTemplateDefinition()).getVisibility();
	}

	public ICPPClassType getClassOwner() throws DOMException {
		return (ICPPClassType) getOwner();
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod#isVirtual()
     */
    public boolean isVirtual() throws DOMException {
        return ((ICPPMethod)getTemplateDefinition()).isVirtual();
    }

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod#isPureVirtual()
     */
	public boolean isPureVirtual() throws DOMException {
        return ((ICPPMethod)getTemplateDefinition()).isPureVirtual();
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod#isDestructor()
     */
	public boolean isDestructor() {
		char[] name = getNameCharArray();
		if (name.length > 1 && name[0] == '~')
			return true;
		
		return false;
	}

	public boolean isImplicit() {
		return false;
	}
}
