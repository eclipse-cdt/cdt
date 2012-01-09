/*******************************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Andrew Ferguson (Symbian)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Result of instantiating a function template.
 */
class PDOMCPPFunctionInstance extends PDOMCPPFunctionSpecialization implements ICPPTemplateInstance {
	private static final int ARGUMENTS = PDOMCPPFunctionSpecialization.RECORD_SIZE + 0;
	
	@SuppressWarnings("hiding")
	private static final int EXCEPTION_SPEC = PDOMCPPFunctionSpecialization.RECORD_SIZE + 4;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPFunctionSpecialization.RECORD_SIZE + 8;
	
	public PDOMCPPFunctionInstance(PDOMLinkage linkage, PDOMNode parent, ICPPFunction function, PDOMBinding orig)
			throws CoreException {
		super(linkage, parent, function, orig);

		final ICPPTemplateInstance asInstance= (ICPPTemplateInstance) function;
		final long argListRec= PDOMCPPArgumentList.putArguments(this, asInstance.getTemplateArguments());
		final Database db = getDB();
		db.putRecPtr(record+ARGUMENTS, argListRec);
		
		long exceptSpecRec = PDOMCPPTypeList.putTypes(this, function.getExceptionSpecification());
		db.putRecPtr(record+EXCEPTION_SPEC, exceptSpecRec);
	}

	public PDOMCPPFunctionInstance(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
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
			final long rec= getPDOM().getDB().getRecPtr(record+ARGUMENTS);
			return PDOMCPPArgumentList.getArguments(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPTemplateArgument.EMPTY_ARGUMENTS;
		}
	}
	
	@Override
	public IType[] getExceptionSpecification() {
		try {
			final long rec = getPDOM().getDB().getRecPtr(record+EXCEPTION_SPEC);
			return PDOMCPPTypeList.getTypes(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	@Override
	@Deprecated
	public IType[] getArguments() {
		return CPPTemplates.getArguments(getTemplateArguments());
	}
}
