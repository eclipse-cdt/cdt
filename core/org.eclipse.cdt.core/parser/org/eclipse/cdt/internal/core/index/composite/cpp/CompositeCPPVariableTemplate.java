/*******************************************************************************
 * Copyright (c) 2015, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplatePartialSpecialization;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.core.runtime.CoreException;

public class CompositeCPPVariableTemplate extends CompositeCPPVariable
		implements ICPPVariableTemplate, ICPPInstanceCache {

	public CompositeCPPVariableTemplate(ICompositesFactory cf, IVariable delegate) {
		super(cf, delegate);
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		return TemplateInstanceUtil.convert(cf, ((ICPPVariableTemplate) rbinding).getTemplateParameters());
	}

	@Override
	public ICPPPartialSpecialization[] getPartialSpecializations() {
		try {
			final CIndex cIndex = (CIndex) ((CPPCompositesFactory) cf).getContext();
			IIndexFragmentBinding[] bindings = cIndex.findEquivalentBindings(rbinding);
			IIndexFragmentBinding[][] preresult = new IIndexFragmentBinding[bindings.length][];

			for (int i = 0; i < bindings.length; i++) {
				final ICPPVariableTemplate template = (ICPPVariableTemplate) bindings[i];
				ICPPPartialSpecialization[] ss = template.getPartialSpecializations();
				preresult[i] = new IIndexFragmentBinding[ss.length];
				System.arraycopy(ss, 0, preresult[i], 0, ss.length);
			}

			return ArrayUtil.addAll(ICPPVariableTemplatePartialSpecialization.EMPTY_ARRAY,
					cf.getCompositeBindings(preresult));
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPVariableTemplatePartialSpecialization.EMPTY_ARRAY;
		}
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
}
