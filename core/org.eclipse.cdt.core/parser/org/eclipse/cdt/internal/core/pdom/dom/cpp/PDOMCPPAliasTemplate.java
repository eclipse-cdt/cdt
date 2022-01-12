/*******************************************************************************
 * Copyright (c) 2012, 2018 Institute for Software, HSR Hochschule fuer Technik
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
 *     Thomas Corbat (IFS) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * PDOM binding for alias template.
 */
class PDOMCPPAliasTemplate extends PDOMCPPBinding implements ICPPAliasTemplate, IPDOMCPPTemplateParameterOwner {
	private static final int ALIASED_TYPE_SIZE = Database.TYPE_SIZE;
	private static final int TEMPLATE_PARAMS_SIZE = Database.PTR_SIZE;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + ALIASED_TYPE_SIZE + TEMPLATE_PARAMS_SIZE;

	private static final int ALIASED_TYPE = PDOMCPPBinding.RECORD_SIZE + 0;
	private static final int TEMPLATE_PARAMS = ALIASED_TYPE + ALIASED_TYPE_SIZE;

	private volatile IPDOMCPPTemplateParameter[] parameters;

	public PDOMCPPAliasTemplate(PDOMCPPLinkage linkage, PDOMNode parent, ICPPAliasTemplate template)
			throws CoreException, DOMException {
		super(linkage, parent, template.getNameCharArray());
		final ICPPTemplateParameter[] origParams = template.getTemplateParameters();
		parameters = PDOMTemplateParameterArray.createPDOMTemplateParameters(linkage, this, origParams);
		final Database db = getDB();
		long rec = PDOMTemplateParameterArray.putArray(db, parameters);
		db.putRecPtr(record + TEMPLATE_PARAMS, rec);
		linkage.new ConfigureAliasTemplate(template, this);
	}

	public PDOMCPPAliasTemplate(PDOMCPPLinkage linkage, long record) {
		super(linkage, record);
	}

	public void initData(IType aliasedType) {
		storeAliasedType(aliasedType);
	}

	private void storeAliasedType(IType aliasedType) {
		try {
			getLinkage().storeType(record + ALIASED_TYPE, aliasedType);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (!(newBinding instanceof ICPPAliasTemplate)) {
			return;
		}

		storeAliasedType(((ICPPAliasTemplate) newBinding).getType());
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_ALIAS_TEMPLATE;
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == null) {
			return false;
		}
		IType aliasedType = getType();
		return type.isSameType(aliasedType);
	}

	@Override
	public IPDOMCPPTemplateParameter[] getTemplateParameters() {
		if (parameters == null) {
			try {
				Database db = getDB();
				long rec = db.getRecPtr(record + TEMPLATE_PARAMS);
				if (rec == 0) {
					parameters = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
				} else {
					parameters = PDOMTemplateParameterArray.getArray(this, rec);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
				parameters = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
			}
		}
		return parameters;
	}

	@Override
	public IType getType() {
		try {
			return getLinkage().loadType(record + ALIASED_TYPE);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
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