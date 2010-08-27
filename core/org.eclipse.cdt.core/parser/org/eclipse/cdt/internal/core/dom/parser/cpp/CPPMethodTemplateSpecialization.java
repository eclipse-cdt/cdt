/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * The specialization of a method template in the context of a class specialization.
 */
public class CPPMethodTemplateSpecialization extends CPPFunctionTemplateSpecialization 
		implements ICPPMethod {

	public CPPMethodTemplateSpecialization(ICPPMethod specialized, ICPPClassType owner, 
			ICPPTemplateParameterMap ctmap) {
		super(specialized, owner, ctmap);
	}

	public boolean isVirtual() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getVisibility() {
		IBinding m = getSpecializedBinding();
		if( m instanceof ICPPMethod )
			return ((ICPPMethod)m).getVisibility();
		return 0;
	}
	
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	public boolean isDestructor() {
		char[] name = getNameCharArray();
		if (name.length > 1 && name[0] == '~')
			return true;
		
		return false;
	}

	public boolean isImplicit() {
		return false;
	}

	public boolean isPureVirtual() {
		return false;
	}

}
