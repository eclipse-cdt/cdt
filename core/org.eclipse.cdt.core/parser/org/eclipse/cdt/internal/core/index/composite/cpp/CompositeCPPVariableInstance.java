/*******************************************************************************
 * Copyright (c) 2015 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableInstance;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPVariableInstance extends CompositeCPPVariable implements ICPPVariableInstance {
	private ICPPVariableInstance delegate;

	public CompositeCPPVariableInstance(ICompositesFactory cf, ICPPVariableInstance delegate) {
		super(cf, delegate);
		this.delegate = delegate;
	}

	@Override
	public ICPPTemplateDefinition getTemplateDefinition() {
		return TemplateInstanceUtil.getTemplateDefinition(cf, rbinding);
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return TemplateInstanceUtil.getTemplateArguments(cf, delegate);
	}

	@Override
	public boolean isExplicitSpecialization() {
		return delegate.isExplicitSpecialization();
	}

	@Override
	@Deprecated
	public IType[] getArguments() {
		return TemplateInstanceUtil.getArguments(cf, delegate);
	}

	@Override
	public IBinding getSpecializedBinding() {
		return TemplateInstanceUtil.getSpecializedBinding(cf, rbinding);
	}

	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		IBinding owner= getOwner();
		if (owner instanceof ICPPSpecialization)
			return ((ICPPSpecialization) owner).getTemplateParameterMap();

		return CPPTemplateParameterMap.EMPTY;
	}

	@Override
	@Deprecated
	public ObjectMap getArgumentMap() {
		return TemplateInstanceUtil.getArgumentMap(cf, rbinding);
	}
}
