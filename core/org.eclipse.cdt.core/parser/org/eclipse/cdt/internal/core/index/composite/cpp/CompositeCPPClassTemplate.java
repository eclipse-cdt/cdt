/*******************************************************************************
 * Copyright (c) 2007, 2011 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.core.runtime.CoreException;

public class CompositeCPPClassTemplate extends CompositeCPPClassType 
		implements ICPPClassTemplate, ICPPInstanceCache {

	public CompositeCPPClassTemplate(ICompositesFactory cf, ICPPClassType ct) {
		super(cf, ct);
	}
	
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() throws DOMException {
		try {
			final CIndex cIndex = (CIndex) ((CPPCompositesFactory) cf).getContext();
			IIndexFragmentBinding[] bindings = cIndex.findEquivalentBindings(rbinding);
			IIndexFragmentBinding[][] preresult = new IIndexFragmentBinding[bindings.length][];

			for (int i = 0; i < bindings.length; i++) {
				final ICPPClassTemplate template = (ICPPClassTemplate) bindings[i];
				ICPPClassTemplatePartialSpecialization[] ss = template.getPartialSpecializations();
				preresult[i] = new IIndexFragmentBinding[ss.length];
				System.arraycopy(ss, 0, preresult[i], 0, ss.length);
			}

			return (ICPPClassTemplatePartialSpecialization[]) ArrayUtil.addAll(
					ICPPClassTemplatePartialSpecialization.class,
					ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY, cf
							.getCompositeBindings(preresult));
		} catch (CoreException ce) {
			CCorePlugin.log(ce);
			return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
		}
	}

	public ICPPTemplateParameter[] getTemplateParameters() {
		return TemplateInstanceUtil.convert(cf, ((ICPPClassTemplate) rbinding).getTemplateParameters());
	}

	public ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		return CompositeInstanceCache.getCache(cf, rbinding).getInstance(arguments);	
	}

	public void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		CompositeInstanceCache.getCache(cf, rbinding).addInstance(arguments, instance);	
	}

	public ICPPTemplateInstance[] getAllInstances() {
		return CompositeInstanceCache.getCache(cf, rbinding).getAllInstances();
	}
	
	public ICPPDeferredClassInstance asDeferredInstance() throws DOMException  {
		CompositeInstanceCache cache= CompositeInstanceCache.getCache(cf, rbinding);
		synchronized (cache) {
			ICPPDeferredClassInstance dci= cache.getDeferredInstance();
			if (dci == null) {
				dci= createDeferredInstance();
				cache.putDeferredInstance(dci);
			}
			return dci;
		}
	}

	protected ICPPDeferredClassInstance createDeferredInstance() throws DOMException {
		ICPPTemplateArgument[] args = CPPTemplates.templateParametersAsArguments(getTemplateParameters());
		return new CPPDeferredClassInstance(this, args, getCompositeScope());
	}

}
