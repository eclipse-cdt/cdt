/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.core.runtime.CoreException;

public class CompositeCPPClassTemplate extends CompositeCPPClassType implements ICPPClassTemplate, ICPPInstanceCache {

	public CompositeCPPClassTemplate(ICompositesFactory cf, ICPPClassType ct) {
		super(cf, ct);
	}

	@Override
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
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

			return ArrayUtil.addAll(ICPPClassTemplatePartialSpecialization.EMPTY_ARRAY,
					cf.getCompositeBindings(preresult));
		} catch (CoreException ce) {
			CCorePlugin.log(ce);
			return ICPPClassTemplatePartialSpecialization.EMPTY_ARRAY;
		}
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
	public final ICPPDeferredClassInstance asDeferredInstance() {
		CompositeInstanceCache cache = CompositeInstanceCache.getCache(cf, rbinding);
		synchronized (cache) {
			ICPPDeferredClassInstance dci = cache.getDeferredInstance();
			if (dci == null) {
				dci = CPPTemplates.createDeferredInstance(this);
				cache.putDeferredInstance(dci);
			}
			return dci;
		}
	}
}
