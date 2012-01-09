/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPDeferredClassInstance extends CompositeCPPClassType implements ICPPDeferredClassInstance {

	private ICPPScope unknownScope;

	public CompositeCPPDeferredClassInstance(ICompositesFactory cf,	ICPPDeferredClassInstance rbinding) {
		super(cf, rbinding);
	}

	@Override
	public ICPPTemplateDefinition getTemplateDefinition() {
		ICPPTemplateDefinition preresult= ((ICPPTemplateInstance)rbinding).getTemplateDefinition();
		return (ICPPTemplateDefinition) cf.getCompositeBinding((IIndexFragmentBinding)preresult);
	}
	
	@Override
	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		return TemplateInstanceUtil.getTemplateParameterMap(cf, (ICPPTemplateInstance) rbinding);
	}

	@Override
	public IBinding getSpecializedBinding() {
		return TemplateInstanceUtil.getSpecializedBinding(cf, rbinding);
	}

	@Override
	public IASTName getUnknownName() {
		return ((ICPPDeferredClassInstance) rbinding).getUnknownName();
	}

	@Override
	public ICPPScope getCompositeScope() {
		return asScope();
	}

	@Override
	public ICPPScope asScope() {
		if (unknownScope == null) {
			unknownScope= new CompositeCPPUnknownScope(this, getUnknownName());
		}
		return unknownScope;
	}

	@Override
	public ICPPClassTemplate getClassTemplate() {
		return (ICPPClassTemplate) cf.getCompositeBinding((IIndexFragmentBinding) ((ICPPDeferredClassInstance) rbinding).getClassTemplate());
	}
	
	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return TemplateInstanceUtil.getTemplateArguments(cf, (ICPPTemplateInstance) rbinding);
	}

	@Override
	public boolean isExplicitSpecialization() {
		return false;
	}

	@Override
	@Deprecated
	public IType[] getArguments() {
		return TemplateInstanceUtil.getArguments(cf, (ICPPTemplateInstance) rbinding);
	}

	@Override
	@Deprecated
	public ObjectMap getArgumentMap() {
		return TemplateInstanceUtil.getArgumentMap(cf, rbinding);
	}
}
