/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplateInstantiator;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPDeferredClassInstance extends CompositeCPPClassType implements ICPPDeferredTemplateInstance, ICPPSpecialization {

	public CompositeCPPDeferredClassInstance(ICompositesFactory cf,	ICPPClassType rbinding) {
		super(cf, rbinding);
	}

	public ICPPTemplateDefinition getTemplateDefinition() {
		ICPPTemplateDefinition preresult= ((ICPPTemplateInstance)rbinding).getTemplateDefinition();
		return (ICPPTemplateDefinition) cf.getCompositeBinding((IIndexFragmentBinding)preresult);
	}
	
	//	TODO - what happens to the arguments?
	public ICPPSpecialization deferredInstance(IType[] arguments) {
		ICPPSpecialization spec= ((ICPPInternalTemplateInstantiator)rbinding).deferredInstance(arguments);
		return (ICPPSpecialization) cf.getCompositeBinding((IIndexFragmentBinding)spec);
	}

	//	TODO - what happens to the arguments?
	public ICPPSpecialization getInstance(IType[] arguments) {
		ICPPSpecialization ins= ((ICPPInternalTemplateInstantiator)rbinding).getInstance(arguments);
		return (ICPPSpecialization) cf.getCompositeBinding((IIndexFragmentBinding)ins);
	}

	//	TODO - what happens to the arguments?
	public IBinding instantiate(IType[] arguments) {
		IBinding ins= ((ICPPInternalTemplateInstantiator)rbinding).instantiate(arguments);
		return (IBinding) cf.getCompositeBinding((IIndexFragmentBinding)ins);
	}
	
	public IType[] getArguments() { return TemplateInstanceUtil.getArguments(cf, rbinding);	}
	public ObjectMap getArgumentMap() {	return TemplateInstanceUtil.getArgumentMap(cf, rbinding); }
	public IBinding getSpecializedBinding() { return TemplateInstanceUtil.getSpecializedBinding(cf, rbinding); }
}
