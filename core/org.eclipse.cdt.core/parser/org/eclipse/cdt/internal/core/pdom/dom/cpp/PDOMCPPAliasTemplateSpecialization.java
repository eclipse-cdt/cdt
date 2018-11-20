/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * PDOM binding for alias template specializations.
 */
public class PDOMCPPAliasTemplateSpecialization extends PDOMCPPSpecialization implements ICPPAliasTemplate {
	private static final int ALIASED_TYPE = PDOMCPPSpecialization.RECORD_SIZE; // TYPE_SIZE
	private static final int TEMPLATE_PARAMS = ALIASED_TYPE + Database.TYPE_SIZE; // PTR_SIZE

	@SuppressWarnings("hiding")
	private static final int RECORD_SIZE = TEMPLATE_PARAMS + Database.PTR_SIZE;

	private volatile IPDOMCPPTemplateParameter[] fParameters;

	public PDOMCPPAliasTemplateSpecialization(PDOMCPPLinkage linkage, PDOMNode parent,
			ICPPAliasTemplate aliasTemplateSpec, IPDOMBinding specialized) throws CoreException, DOMException {
		super(linkage, parent, (ICPPSpecialization) aliasTemplateSpec, specialized);
		final ICPPTemplateParameter[] origParams = aliasTemplateSpec.getTemplateParameters();
		fParameters = PDOMTemplateParameterArray.createPDOMTemplateParameters(linkage, this, origParams);
		final Database db = getDB();
		long rec = PDOMTemplateParameterArray.putArray(db, fParameters);
		db.putRecPtr(record + TEMPLATE_PARAMS, rec);
		linkage.new ConfigureAliasTemplateSpecialization(aliasTemplateSpec, this);
	}

	public PDOMCPPAliasTemplateSpecialization(PDOMCPPLinkage linkage, long record) {
		super(linkage, record);
	}

	public void initData(IType aliasedType) {
		try {
			getLinkage().storeType(record + ALIASED_TYPE, aliasedType);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_ALIAS_TEMPLATE_SPECIALIZATION;
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == null) {
			return false;
		}
		return type.isSameType(getType());
	}

	@Override
	public IPDOMCPPTemplateParameter[] getTemplateParameters() {
		if (fParameters == null) {
			try {
				Database db = getDB();
				long rec = db.getRecPtr(record + TEMPLATE_PARAMS);
				if (rec == 0) {
					fParameters = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
				} else {
					fParameters = PDOMTemplateParameterArray.getArray(this, rec);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fParameters = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
			}
		}
		return fParameters;
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
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
}
