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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.composite.CompositeScope;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPTemplateScope extends CompositeScope implements ICPPTemplateScope {
	public CompositeCPPTemplateScope(ICompositesFactory cf,
			ICPPTemplateScope rbinding) {
		super(cf, (IIndexFragmentBinding) ((IIndexScope)rbinding).getScopeBinding());
	}

	public ICPPTemplateDefinition getTemplateDefinition() throws DOMException {
		ICPPTemplateDefinition preresult= ((ICPPTemplateScope) rbinding).getTemplateDefinition();
		return (ICPPTemplateDefinition) processUncertainBinding(preresult);
	}

	public IBinding[] find(String name) throws DOMException {
		IBinding[] preresult = ((ICPPTemplateScope)rbinding).find(name);
		return processUncertainBindings(preresult);	
	}

	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) throws DOMException {
		IBinding binding = ((ICPPTemplateScope)rbinding).getBinding(name, resolve, fileSet);
		return processUncertainBinding(binding);
	}
	
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) throws DOMException {
		IBinding[] bindings = ((ICPPTemplateScope)rbinding).getBindings(name, resolve, prefixLookup, fileSet);
		return processUncertainBindings(bindings);
	}

	public IIndexBinding getScopeBinding() {
		return cf.getCompositeBinding(rbinding);
	}
	
	@Override
	public IIndexScope getParent() {
		return (IIndexScope) rbinding;
	}
}
