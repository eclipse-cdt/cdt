/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * The specialization of a method template in the context of a class specialization.
 */
public class CPPMethodTemplateSpecialization extends CPPFunctionTemplateSpecialization 
		implements ICPPMethod {
	
	private ICPPTemplateParameter[] fTemplateParameters;

	public CPPMethodTemplateSpecialization(ICPPMethod specialized, ICPPClassSpecialization owner, 
			ICPPTemplateParameterMap ctmap, ICPPFunctionType type, IType[] exceptionSpecs) {
		super(specialized, owner, ctmap, type, exceptionSpecs);
	}
	
	public void setTemplateParameters(ICPPTemplateParameter[] templateParameters) {
		fTemplateParameters = templateParameters;
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		return fTemplateParameters;
	}

	@Override
	public boolean isVirtual() {
		IBinding m = getSpecializedBinding();
		if (m instanceof ICPPMethod)
			return ((ICPPMethod) m).isVirtual();
		return false;
	}

	@Override
	public int getVisibility() {
		IBinding m = getSpecializedBinding();
		if (m instanceof ICPPMethod)
			return ((ICPPMethod) m).getVisibility();
		return 0;
	}
	
	@Override
	public ICPPClassType getClassOwner() {
		return getOwner();
	}

	@Override
	public boolean isDestructor() {
		char[] name = getNameCharArray();
		if (name.length > 1 && name[0] == '~')
			return true;
		
		return false;
	}

	@Override
	public boolean isImplicit() {
		IBinding m = getSpecializedBinding();
		if (m instanceof ICPPMethod)
			return ((ICPPMethod) m).isImplicit();
		return false;
	}
	
	@Override
	public boolean isExplicit() {
		IBinding m = getSpecializedBinding();
		if (m instanceof ICPPMethod)
			return ((ICPPMethod) m).isExplicit();
		return false;
	}

	@Override
	public boolean isPureVirtual() {
		IBinding m = getSpecializedBinding();
		if (m instanceof ICPPMethod)
			return ((ICPPMethod) m).isPureVirtual();
		return false;
	}

	@Override
	public boolean isOverride() {
		return false;
	}

	@Override
	public boolean isFinal() {
		return false;
	}
	
	@Override
	public ICPPClassSpecialization getOwner() {
		return (ICPPClassSpecialization) super.getOwner();
	}
}
