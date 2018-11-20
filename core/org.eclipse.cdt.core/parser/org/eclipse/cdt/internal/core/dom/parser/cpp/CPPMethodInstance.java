/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * The result of instantiating a method template.
 */
public class CPPMethodInstance extends CPPFunctionInstance implements ICPPMethod {

	public CPPMethodInstance(ICPPMethod orig, ICPPClassType owner, ICPPTemplateParameterMap tpmap,
			ICPPTemplateArgument[] args, ICPPFunctionType type, IType[] exceptionSpecs) {
		super(orig, owner, tpmap, args, type, exceptionSpecs);
	}

	@Override
	public int getVisibility() {
		return ((ICPPMethod) getTemplateDefinition()).getVisibility();
	}

	@Override
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	@Override
	public boolean isVirtual() {
		return ((ICPPMethod) getTemplateDefinition()).isVirtual();
	}

	@Override
	public boolean isPureVirtual() {
		return ((ICPPMethod) getTemplateDefinition()).isPureVirtual();
	}

	@Override
	public boolean isExplicit() {
		return ((ICPPMethod) getTemplateDefinition()).isExplicit();
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
		return false;
	}

	@Override
	public boolean isOverride() {
		return ((ICPPMethod) getTemplateDefinition()).isOverride();
	}

	@Override
	public boolean isFinal() {
		return ((ICPPMethod) getTemplateDefinition()).isFinal();
	}
}
