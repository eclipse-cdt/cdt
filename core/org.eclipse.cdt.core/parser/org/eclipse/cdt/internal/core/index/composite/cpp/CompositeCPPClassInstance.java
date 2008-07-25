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
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPClassInstance extends CompositeCPPClassSpecialization implements ICPPTemplateInstance {

	public CompositeCPPClassInstance(ICompositesFactory cf, ICPPClassType rbinding) {
		super(cf, rbinding);
	}

	@Override
	public IScope getCompositeScope() throws DOMException {
		return cf.getCompositeScope((IIndexScope) ((ICPPClassType) rbinding).getCompositeScope());
	}
	
	@Override
	public ObjectMap getArgumentMap() {	return TemplateInstanceUtil.getArgumentMap(cf, rbinding); }
	@Override
	public ICPPClassType getSpecializedBinding() { return (ICPPClassType) TemplateInstanceUtil.getSpecializedBinding(cf, rbinding); }
	public IType[] getArguments() {	return TemplateInstanceUtil.getArguments(cf, (ICPPTemplateInstance) rbinding); }
	public ICPPTemplateDefinition getTemplateDefinition() {	return TemplateInstanceUtil.getTemplateDefinition(cf, rbinding); }
}
