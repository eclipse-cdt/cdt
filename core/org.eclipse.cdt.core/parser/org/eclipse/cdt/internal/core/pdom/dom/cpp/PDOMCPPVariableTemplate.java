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
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

public class PDOMCPPVariableTemplate extends PDOMCPPVariable
		implements ICPPVariableTemplate, ICPPInstanceCache, IPDOMCPPTemplateParameterOwner {
	private static final int TEMPLATE_PARAMS = PDOMCPPVariable.RECORD_SIZE;
	private static final int FIRST_PARTIAL = TEMPLATE_PARAMS + Database.PTR_SIZE;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = FIRST_PARTIAL + Database.PTR_SIZE;

	private volatile IPDOMCPPTemplateParameter[] params;

	public PDOMCPPVariableTemplate(PDOMCPPLinkage linkage, PDOMNode parent, ICPPVariableTemplate template)
			throws CoreException, DOMException {
		super(linkage, parent, template, false);

		final ICPPTemplateParameter[] origParams = template.getTemplateParameters();
		params = PDOMTemplateParameterArray.createPDOMTemplateParameters(linkage, this, origParams);

		final Database db = getDB();
		long rec = PDOMTemplateParameterArray.putArray(db, params);
		db.putRecPtr(record + TEMPLATE_PARAMS, rec);

		linkage.new ConfigureVariableTemplate(template, this);
	}

	public PDOMCPPVariableTemplate(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding name) {
		// No support for updating templates, yet.
	}

	@Override
	public IPDOMCPPTemplateParameter[] getTemplateParameters() {
		if (params == null) {
			try {
				long rec = getDB().getRecPtr(record + TEMPLATE_PARAMS);
				if (rec == 0) {
					params = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
				} else {
					params = PDOMTemplateParameterArray.getArray(this, rec);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
				params = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
			}
		}
		return params;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_VARIABLE_TEMPLATE;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public static void initData(PDOMCPPVariable binding, IType fOriginalType, IValue fOriginalValue) {
		try {
			binding.setType(binding.getLinkage(), fOriginalType);
			binding.setValue(fOriginalValue);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public ICPPPartialSpecialization[] getPartialSpecializations() {
		try {
			ArrayList<PDOMCPPVariableTemplatePartialSpecialization> partials = new ArrayList<>();
			for (PDOMCPPVariableTemplatePartialSpecialization partial = getFirstPartial(); partial != null; partial = partial
					.getNextPartial()) {
				partials.add(partial);
			}

			return partials.toArray(new ICPPVariableTemplatePartialSpecialization[partials.size()]);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPVariableTemplatePartialSpecialization.EMPTY_ARRAY;
		}
	}

	@Override
	public ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		return PDOMInstanceCache.getCache(this).getInstance(arguments);
	}

	@Override
	public void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		PDOMInstanceCache.getCache(this).addInstance(arguments, instance);
	}

	@Override
	public ICPPTemplateInstance[] getAllInstances() {
		return PDOMInstanceCache.getCache(this).getAllInstances();
	}

	private PDOMCPPVariableTemplatePartialSpecialization getFirstPartial() throws CoreException {
		long value = getDB().getRecPtr(record + FIRST_PARTIAL);
		if (this instanceof PDOMCPPFieldTemplate)
			return value != 0 ? new PDOMCPPFieldTemplatePartialSpecialization(getLinkage(), value) : null;
		else
			return value != 0 ? new PDOMCPPVariableTemplatePartialSpecialization(getLinkage(), value) : null;
	}

	public void addPartial(PDOMCPPVariableTemplatePartialSpecialization partial) throws CoreException {
		PDOMCPPVariableTemplatePartialSpecialization first = getFirstPartial();
		partial.setNextPartial(first);
		getDB().putRecPtr(record + FIRST_PARTIAL, partial.getRecord());
	}

	@Override
	public ICPPTemplateParameter adaptTemplateParameter(ICPPTemplateParameter param) {
		// Template parameters are identified by their position in the parameter list.
		int pos = param.getParameterPosition();
		ICPPTemplateParameter[] pars = getTemplateParameters();

		if (pars == null || pos >= pars.length)
			return null;

		ICPPTemplateParameter result = pars[pos];
		if (param instanceof ICPPTemplateTypeParameter) {
			if (result instanceof ICPPTemplateTypeParameter)
				return result;
		} else if (param instanceof ICPPTemplateNonTypeParameter) {
			if (result instanceof ICPPTemplateNonTypeParameter)
				return result;
		} else if (param instanceof ICPPTemplateTemplateParameter) {
			if (result instanceof ICPPTemplateTemplateParameter)
				return result;
		}
		return null;
	}
}
