/*******************************************************************************
 * Copyright (c) 2007, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bryan Wilkinson (QNX) - Initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * An instantiation or an explicit specialization of a function template.
 */
class PDOMCPPFunctionInstance extends PDOMCPPFunctionSpecialization implements ICPPFunctionInstance {
	private static final int ARGUMENTS = PDOMCPPFunctionSpecialization.RECORD_SIZE + 0;

	@SuppressWarnings("hiding")
	private static final int EXCEPTION_SPEC = PDOMCPPFunctionSpecialization.RECORD_SIZE + 4;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPFunctionSpecialization.RECORD_SIZE + 8;

	public PDOMCPPFunctionInstance(PDOMCPPLinkage linkage, PDOMNode parent, ICPPFunction function, PDOMBinding orig)
			throws CoreException {
		super(linkage, parent, function, orig);

		final Database db = getDB();
		long exceptSpecRec = PDOMCPPTypeList.putTypes(this, function.getExceptionSpecification());
		db.putRecPtr(record + EXCEPTION_SPEC, exceptSpecRec);

		linkage.new ConfigureFunctionInstance(function, this);
	}

	public PDOMCPPFunctionInstance(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	public void initData(ICPPTemplateArgument[] templateArguments) {
		try {
			final long argListRec = PDOMCPPArgumentList.putArguments(this, templateArguments);
			final Database db = getDB();
			db.putRecPtr(record + ARGUMENTS, argListRec);
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
		return IIndexCPPBindingConstants.CPP_FUNCTION_INSTANCE;
	}

	@Override
	public ICPPTemplateDefinition getTemplateDefinition() {
		return (ICPPTemplateDefinition) getSpecializedBinding();
	}

	@Override
	public boolean isExplicitSpecialization() {
		try {
			return hasDeclaration();
		} catch (CoreException e) {
			return false;
		}
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		try {
			final long rec = getPDOM().getDB().getRecPtr(record + ARGUMENTS);
			return PDOMCPPArgumentList.getArguments(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPTemplateArgument.EMPTY_ARGUMENTS;
		}
	}

	@Override
	public IType[] getExceptionSpecification() {
		try {
			final long rec = getPDOM().getDB().getRecPtr(record + EXCEPTION_SPEC);
			return PDOMCPPTypeList.getTypes(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
}
