/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplateInstantiator;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class InternalTemplateInstantiatorUtil {
	public static ICPPSpecialization deferredInstance(IType[] arguments, ICompositesFactory cf, IIndexBinding rbinding) {
		ICPPSpecialization spec= ((ICPPInternalTemplateInstantiator)rbinding).deferredInstance(arguments);
		if (spec instanceof IIndexFragmentBinding) {
			return (ICPPSpecialization) cf.getCompositeBinding((IIndexFragmentBinding)spec);
		} else {
			//can result in a non-index binding
			return spec;
		}
	}

	public static ICPPSpecialization getInstance(IType[] arguments, ICompositesFactory cf, IIndexBinding rbinding) {
		ICPPSpecialization ins= ((ICPPInternalTemplateInstantiator)rbinding).getInstance(arguments);
		return (ICPPSpecialization) cf.getCompositeBinding((IIndexFragmentBinding)ins);
	}

	public static IBinding instantiate(IType[] arguments, ICompositesFactory cf, IIndexBinding rbinding) {
		IBinding ins= ((ICPPInternalTemplateInstantiator)rbinding).instantiate(arguments);
		if (ins instanceof IIndexFragmentBinding) {
			return (IBinding) cf.getCompositeBinding((IIndexFragmentBinding)ins);
		} else {
			//can result in a non-index binding
			return ins;
		}
	}
}
