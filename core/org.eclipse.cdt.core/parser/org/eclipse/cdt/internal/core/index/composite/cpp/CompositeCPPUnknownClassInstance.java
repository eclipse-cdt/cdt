/*******************************************************************************
 * Copyright (c) 2008 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

/**
 * @author Sergey Prigogin
 */
class CompositeCPPUnknownClassInstance extends CompositeCPPUnknownClassType
		implements ICPPInternalUnknownClassInstance {
	public CompositeCPPUnknownClassInstance(ICompositesFactory cf,
			ICPPInternalUnknownClassInstance rbinding) {
		super(cf, rbinding);
	}

	public IType[] getArguments() {
		IType[] arguments = ((ICPPInternalUnknownClassInstance) rbinding).getArguments();
		try {
			for (int i = 0; i < arguments.length; i++) {
				arguments[i] = cf.getCompositeType((IIndexType) arguments[i]);
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return arguments;
	}

	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations()	throws DOMException {
		return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
	}

	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		return ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
	}

	public void addPartialSpecialization(ICPPClassTemplatePartialSpecialization spec) {
	}

	public void addSpecialization(IType[] arguments, ICPPSpecialization specialization) {
	}

	public ICPPSpecialization deferredInstance(ObjectMap argMap, IType[] arguments) {
		return InternalTemplateInstantiatorUtil.deferredInstance(argMap, arguments, cf, rbinding);
	}

	public ICPPSpecialization getInstance(IType[] arguments) {
		return InternalTemplateInstantiatorUtil.getInstance(arguments, cf, this);
	}

	public IBinding instantiate(IType[] args) {
		return InternalTemplateInstantiatorUtil.instantiate(args, cf, rbinding);
	}
	
	@Override
	public IBinding resolveUnknown(ObjectMap argMap) throws DOMException {
		IBinding result = super.resolveUnknown(argMap);
		if (result instanceof ICPPSpecialization && result instanceof ICPPTemplateDefinition) {
			IType[] newArgs = CPPTemplates.instantiateTypes(getArguments(), argMap);
			IBinding instance = CPPTemplates.instantiateTemplate((ICPPTemplateDefinition) result, newArgs, null);
			if (instance != null) {
				result = instance;
			}
		}
		return result;
	}
}
