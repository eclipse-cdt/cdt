/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Result of instantiating a function template.
 */
class PDOMCPPFunctionInstance extends PDOMCPPFunctionSpecialization implements ICPPTemplateInstance {
	private static final int ARGUMENTS = PDOMCPPFunctionSpecialization.RECORD_SIZE + 0;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPFunctionSpecialization.RECORD_SIZE + 4;
	
	public PDOMCPPFunctionInstance(PDOM pdom, PDOMNode parent, ICPPFunction function, PDOMBinding orig)
			throws CoreException {
		super(pdom, parent, function, orig);

		final ICPPTemplateInstance asInstance= (ICPPTemplateInstance) function;
		final int argListRec= PDOMCPPArgumentList.putArguments(this, asInstance.getTemplateArguments());
		pdom.getDB().putInt(record+ARGUMENTS, argListRec);
	}

	public PDOMCPPFunctionInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
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
	
	@Deprecated
	public IType[] getArguments() {
		return CPPTemplates.getArguments(getTemplateArguments());
	}
	
	/*
	 * For debug purposes only
	 */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getName());
		result.append("(){"); 
		try {
			result.append(ASTTypeUtil.getType(getType()));
		} catch (DOMException e) {
			e.printStackTrace();
		}
		result.append("} "); 
		try {
			result.append(getConstantNameForValue(getLinkageImpl(), getNodeType()));
		} catch (CoreException ce) {
			result.append(getNodeType());
		}
		return result.toString();
	}
}
