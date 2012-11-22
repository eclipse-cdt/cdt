/*******************************************************************************
 * Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * PDOM binding for alias template.
 */
class PDOMCPPAliasTemplate extends PDOMCPPBinding implements ICPPAliasTemplate {
	private static final int ALIASED_TYPE_SIZE = Database.TYPE_SIZE;
	private static final int TEMPLATE_PARAMS_SIZE = PDOMCPPTemplateTemplateParameter.RECORD_SIZE;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + ALIASED_TYPE_SIZE + TEMPLATE_PARAMS_SIZE;

	private static final int ALIASED_TYPE_OFFSET = PDOMCPPBinding.RECORD_SIZE + 0;
	private static final int TEMPLATE_PARAMS_OFFSET = ALIASED_TYPE_OFFSET + ALIASED_TYPE_SIZE;

	private volatile IPDOMCPPTemplateParameter[] parameters;

	public PDOMCPPAliasTemplate(PDOMCPPLinkage linkage, PDOMNode parent,
			ICPPAliasTemplate templateAlias) throws CoreException, DOMException {
		super(linkage, parent, templateAlias.getNameCharArray());
		setTemplateParameters(linkage, templateAlias.getTemplateParameters());
		setType(linkage, templateAlias.getType());
	}

	public PDOMCPPAliasTemplate(PDOMCPPLinkage linkage, long record) {
		super(linkage, record);
	}

	private void setTemplateParameters(PDOMCPPLinkage linkage,
			final ICPPTemplateParameter[] origParams) throws CoreException, DOMException {
		parameters = PDOMTemplateParameterArray.createPDOMTemplateParameters(linkage, this, origParams);
		final Database db = getDB();
		long rec= PDOMTemplateParameterArray.putArray(db, parameters);
		db.putRecPtr(record + TEMPLATE_PARAMS_OFFSET, rec);
		linkage.new ConfigureTemplateParameters(origParams, parameters);
	}

	private void setType(PDOMCPPLinkage linkage, IType aliasedType) throws CoreException {
		linkage.storeType(record + ALIASED_TYPE_OFFSET, aliasedType);
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_TEMPLATE_ALIAS;
	}

	@Override
	public boolean isSameType(IType type) {
		if(type == null){
			return false;
		}
		IType aliasedType = getType();
		return type.isSameType(aliasedType);
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		if (parameters == null) {
			try {
				Database db = getDB();
				long rec= db.getRecPtr(record + TEMPLATE_PARAMS_OFFSET);
				if (rec == 0) {
					parameters= IPDOMCPPTemplateParameter.EMPTY_ARRAY;
				} else {
					parameters= PDOMTemplateParameterArray.getArray(this, rec);
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
			return getLinkage().loadType(record + ALIASED_TYPE_OFFSET);
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
}