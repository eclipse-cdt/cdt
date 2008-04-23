/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPClassInstance extends CompositeCPPClassType
		implements ICPPTemplateInstance, ICPPSpecialization {

	public CompositeCPPClassInstance(ICompositesFactory cf, ICPPClassType rbinding) {
		super(cf, rbinding);
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		ICPPClassType specialized = (ICPPClassType) getSpecializedBinding();
		ICPPMethod[] bindings = specialized.getDeclaredMethods();
		for (int i = 0; i < bindings.length; i++) {
			bindings[i]= (ICPPMethod) CPPTemplates.createSpecialization((ICPPScope)getScope(), (IIndexBinding) bindings[i], getArgumentMap());
		}
		return bindings;
	}	

	@Override
	public IScope getCompositeScope() throws DOMException {
		return new CompositeCPPClassSpecializationScope(cf, rbinding);
	}
	
	public ObjectMap getArgumentMap() {	return TemplateInstanceUtil.getArgumentMap(cf, rbinding); }
	public IBinding getSpecializedBinding() { return TemplateInstanceUtil.getSpecializedBinding(cf, rbinding); }
	public IType[] getArguments() {	return TemplateInstanceUtil.getArguments(cf, (ICPPTemplateInstance) rbinding); }
	public ICPPTemplateDefinition getTemplateDefinition() {	return TemplateInstanceUtil.getTemplateDefinition(cf, rbinding); }
	@Override
	public ICPPBase[] getBases() throws DOMException { return CPPTemplates.getBases(this); }
}
