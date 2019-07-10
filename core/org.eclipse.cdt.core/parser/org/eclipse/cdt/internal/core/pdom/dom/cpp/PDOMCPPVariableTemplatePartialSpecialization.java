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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IndexCPPSignatureUtil;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMOverloader;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

public class PDOMCPPVariableTemplatePartialSpecialization extends PDOMCPPVariableTemplate
		implements ICPPVariableTemplatePartialSpecialization, IPDOMPartialSpecialization, IPDOMOverloader {
	private static final int ARGUMENTS = PDOMCPPVariableTemplate.RECORD_SIZE + 0;
	private static final int SIGNATURE_HASH = ARGUMENTS + Database.PTR_SIZE;
	private static final int PRIMARY = SIGNATURE_HASH + Database.INT_SIZE;
	private static final int NEXT_PARTIAL = PRIMARY + Database.PTR_SIZE;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = NEXT_PARTIAL + Database.PTR_SIZE;

	public PDOMCPPVariableTemplatePartialSpecialization(PDOMCPPLinkage linkage, PDOMNode parent,
			ICPPVariableTemplatePartialSpecialization parSpec, PDOMCPPVariableTemplate primary)
			throws CoreException, DOMException {
		super(linkage, parent, parSpec);
		getDB().putRecPtr(record + PRIMARY, primary.getRecord());
		primary.addPartial(this);

		try {
			Integer sigHash = IndexCPPSignatureUtil.getSignatureHash(parSpec);
			getDB().putInt(record + SIGNATURE_HASH, sigHash != null ? sigHash.intValue() : 0);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}

		linkage.new ConfigurePartialSpecialization(this, parSpec);
	}

	public PDOMCPPVariableTemplatePartialSpecialization(PDOMLinkage pdomLinkage, long record) {
		super(pdomLinkage, record);
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_VARIABLE_TEMPLATE_PARTIAL_SPECIALIZATION;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public ICPPTemplateDefinition getPrimaryTemplate() {
		try {
			return new PDOMCPPVariableTemplate(getLinkage(), getPrimaryTemplateRec());
		} catch (CoreException e) {
			CCorePlugin.log("Failed to load primary template for " + getName(), e); //$NON-NLS-1$
			return null;
		}
	}

	protected final long getPrimaryTemplateRec() throws CoreException {
		return getDB().getRecPtr(record + PRIMARY);
	}

	@Override
	public void setTemplateArguments(ICPPTemplateArgument[] args) throws CoreException {
		final Database db = getPDOM().getDB();
		long oldRec = db.getRecPtr(record + ARGUMENTS);
		long rec = PDOMCPPArgumentList.putArguments(this, args);
		db.putRecPtr(record + ARGUMENTS, rec);
		if (oldRec != 0) {
			PDOMCPPArgumentList.clearArguments(this, oldRec);
		}
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		try {
			long rec = getPDOM().getDB().getRecPtr(record + ARGUMENTS);
			return PDOMCPPArgumentList.getArguments(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log("Failed to load template arguments for " + getName(), e); //$NON-NLS-1$
			return ICPPTemplateArgument.EMPTY_ARGUMENTS;
		}
	}

	@Override
	public int getSignatureHash() throws CoreException {
		return getDB().getInt(record + SIGNATURE_HASH);
	}

	public PDOMCPPVariableTemplatePartialSpecialization getNextPartial() throws CoreException {
		long rec = getNextPartialRec();
		if (rec == 0)
			return null;
		return new PDOMCPPVariableTemplatePartialSpecialization(getLinkage(), rec);
	}

	protected final long getNextPartialRec() throws CoreException {
		return getDB().getRecPtr(record + NEXT_PARTIAL);
	}

	public void setNextPartial(PDOMCPPVariableTemplatePartialSpecialization partial) throws CoreException {
		long rec = partial != null ? partial.getRecord() : 0;
		getDB().putRecPtr(record + NEXT_PARTIAL, rec);
	}
}
