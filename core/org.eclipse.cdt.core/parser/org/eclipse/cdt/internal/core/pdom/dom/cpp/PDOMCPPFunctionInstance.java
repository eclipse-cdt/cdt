/*******************************************************************************
 * Copyright (c) 2007, 2009 QNX Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.DOMException;
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
		final int argListRec= PDOMCPPArgumentList.putArguments(this, asInstance.getTemplateArguments());
		final Database db = getDB();
		db.putInt(record+ARGUMENTS, argListRec);
		
		try {
			int exceptSpecRec = PDOMCPPTypeList.putTypes(this, function.getExceptionSpecification());
			db.putInt(record+EXCEPTION_SPEC, exceptSpecRec);
		} catch (DOMException e) {
			// ignore problems in the exception specification
		}
	}

	public PDOMCPPFunctionInstance(PDOMLinkage linkage, int bindingRecord) {
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
	
	
	public ICPPTemplateDefinition getTemplateDefinition() {
		return (ICPPTemplateDefinition) getSpecializedBinding();
	}
	
	public ICPPTemplateArgument[] getTemplateArguments() {
		try {
			final int rec= getPDOM().getDB().getInt(record+ARGUMENTS);
			return PDOMCPPArgumentList.getArguments(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPTemplateArgument.EMPTY_ARGUMENTS;
		}
	}
	
	@Override
	public IType[] getExceptionSpecification() throws DOMException {
		try {
			final int rec = getPDOM().getDB().getInt(record+EXCEPTION_SPEC);
			return PDOMCPPTypeList.getTypes(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	@Deprecated
	public IType[] getArguments() {
		return CPPTemplates.getArguments(getTemplateArguments());
	}
}
