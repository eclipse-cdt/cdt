/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecializationSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPClassTemplatePartialSpecializationSpecialization extends
		CompositeCPPClassSpecialization implements ICPPClassTemplatePartialSpecializationSpecialization, ICPPInstanceCache {

	public CompositeCPPClassTemplatePartialSpecializationSpecialization(ICompositesFactory cf, ICPPClassTemplatePartialSpecializationSpecialization rbinding) {
		super(cf, rbinding);
	}

	@Override
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		return TemplateInstanceUtil.convert(cf, ((ICPPClassTemplate) rbinding).getTemplateParameters());
	}

	@Override
	public ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		return CompositeInstanceCache.getCache(cf, rbinding).getInstance(arguments);	
	}

	@Override
	public void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		CompositeInstanceCache.getCache(cf, rbinding).addInstance(arguments, instance);	
	}

	@Override
	public ICPPTemplateInstance[] getAllInstances() {
		return CompositeInstanceCache.getCache(cf, rbinding).getAllInstances();
	}

	@Override
	public ICPPClassTemplate getPrimaryClassTemplate() {
		return (ICPPClassTemplate) cf.getCompositeBinding((IIndexFragmentBinding) ((ICPPClassTemplatePartialSpecializationSpecialization) rbinding).getPrimaryClassTemplate());
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return TemplateInstanceUtil.getTemplateArguments(cf, (ICPPClassTemplatePartialSpecialization) rbinding);
	}

	@Override
	public ICPPDeferredClassInstance asDeferredInstance() {
		CompositeInstanceCache cache= CompositeInstanceCache.getCache(cf, rbinding);
		synchronized (cache) {
			ICPPDeferredClassInstance dci= cache.getDeferredInstance();
			if (dci == null) {
				dci= CPPTemplates.createDeferredInstance(this);
				cache.putDeferredInstance(dci);
			}
			return dci;
		}
	}
	
	@Override
	@Deprecated
	public IType[] getArguments() {
		return TemplateInstanceUtil.getArguments(cf, (ICPPClassTemplatePartialSpecialization) rbinding);
	}
}
