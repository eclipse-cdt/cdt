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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMOverloader;
import org.eclipse.core.runtime.CoreException;

public class CompositeCPPClassTemplatePartialSpecialization extends CompositeCPPClassTemplate implements ICPPClassTemplatePartialSpecialization, ICPPSpecialization, IPDOMOverloader {
	public CompositeCPPClassTemplatePartialSpecialization(ICompositesFactory cf, ICPPClassTemplatePartialSpecialization delegate) {
		super(cf, (ICPPClassType) delegate);
	}

	public ICPPClassTemplate getPrimaryClassTemplate() throws DOMException {
		ICPPClassTemplate preresult= ((ICPPClassTemplatePartialSpecialization)rbinding).getPrimaryClassTemplate();
		return (ICPPClassTemplate) cf.getCompositeBinding((IIndexFragmentBinding)preresult);
	}

	public IType[] getArguments() { return TemplateInstanceUtil.getArguments(cf, (ICPPClassTemplatePartialSpecialization) rbinding); }
	public ObjectMap getArgumentMap() {	return TemplateInstanceUtil.getArgumentMap(cf, rbinding); }
	public IBinding getSpecializedBinding() { return TemplateInstanceUtil.getSpecializedBinding(cf, rbinding); }
	public int getSignatureMemento() throws CoreException { return ((IPDOMOverloader) rbinding).getSignatureMemento(); }
	public IBinding instantiate(IType[] args) { return InternalTemplateInstantiatorUtil.instantiate(args, cf, rbinding); }
}
