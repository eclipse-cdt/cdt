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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplateInstantiator;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPFunctionTemplateSpecialization
	extends CompositeCPPFunctionSpecialization
	implements ICPPFunctionTemplate, ICPPInternalTemplateInstantiator {

	public CompositeCPPFunctionTemplateSpecialization(ICompositesFactory cf, ICPPFunction ft) {
		super(cf, ft);
	}

	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		ICPPTemplateParameter[] result= ((ICPPClassTemplate)rbinding).getTemplateParameters();
		for(int i=0; i<result.length; i++) {
			result[i]= (ICPPTemplateParameter) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	public ICPPSpecialization deferredInstance(IType[] arguments) {
		fail(); return null; 
	}

	public ICPPSpecialization getInstance(IType[] arguments) {
		return InternalTemplateInstantiatorUtil.getInstance(arguments, cf, this);
	}

	public IBinding instantiate(IType[] arguments) {
		return InternalTemplateInstantiatorUtil.instantiate(arguments, cf, rbinding);
	}
}
