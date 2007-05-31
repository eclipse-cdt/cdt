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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplateInstantiator;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.core.runtime.CoreException;

public class CompositeCPPClassTemplate extends CompositeCPPClassType implements
ICPPClassTemplate, ICPPInternalTemplateInstantiator{

	public CompositeCPPClassTemplate(ICompositesFactory cf, ICPPClassType ct) {
		super(cf, ct);
	}
	
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations()
			throws DOMException {		
		try {
			IIndexFragmentBinding[] bindings= ((CIndex)((CPPCompositesFactory)cf).getContext()).findEquivalentBindings(rbinding);
			IIndexFragmentBinding[][] preresult= new IIndexFragmentBinding[bindings.length][];
			
			for(int i=0; i<bindings.length; i++) {
				ICPPClassTemplatePartialSpecialization[] ss= ((ICPPClassTemplate)bindings[i]).getPartialSpecializations();
				preresult[i]= new IIndexFragmentBinding[ss.length];
				System.arraycopy(ss, 0, preresult[i], 0, ss.length);
			}
			
			return (ICPPClassTemplatePartialSpecialization[]) ArrayUtil.addAll(ICPPClassTemplatePartialSpecialization.class, null, cf.getCompositeBindings(preresult));
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
			return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
		}
	}

	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		ICPPTemplateParameter[] result= ((ICPPClassTemplate)rbinding).getTemplateParameters();
		for(int i=0; i<result.length; i++) {
			result[i]= (ICPPTemplateParameter) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	public ICPPSpecialization deferredInstance(IType[] arguments) {
		return InternalTemplateInstantiatorUtil.deferredInstance(arguments, cf, rbinding);
	}

	public ICPPSpecialization getInstance(IType[] arguments) {
		return InternalTemplateInstantiatorUtil.getInstance(arguments, cf, this);
	}

	public IBinding instantiate(IType[] arguments) {
		ICPPTemplateDefinition template = null;
		try {
			template = CPPTemplates.matchTemplatePartialSpecialization(this, arguments);
		} catch (DOMException e) {
			return e.getProblem();
		}
		
		if( template instanceof IProblemBinding )
			return template;
		if( template != null && template instanceof ICPPClassTemplatePartialSpecialization ){
			return ((ICPPInternalTemplateInstantiator)template).instantiate( arguments );	
		}
		
		return CPPTemplates.instantiateTemplate(this, arguments, null);
	}

}
