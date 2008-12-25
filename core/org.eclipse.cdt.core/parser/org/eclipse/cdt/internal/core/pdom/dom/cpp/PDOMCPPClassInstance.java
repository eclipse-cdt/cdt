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
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
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
 * Result of instantiating a class template.
 */
class PDOMCPPClassInstance extends PDOMCPPClassSpecialization implements ICPPTemplateInstance {
	
	private static final int ARGUMENTS = PDOMCPPClassSpecialization.RECORD_SIZE + 0;
	
	/**
	 * The size in bytes of a PDOMCPPClassInstance record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPClassSpecialization.RECORD_SIZE + 4;
	
	public PDOMCPPClassInstance(PDOM pdom, PDOMNode parent, ICPPClassType classType, PDOMBinding orig)
			throws CoreException {
		super(pdom, parent, classType, orig);
		final ICPPTemplateInstance asInstance= (ICPPTemplateInstance) classType;
		final int argListRec= PDOMCPPArgumentList.putArguments(this, asInstance.getTemplateArguments());
		pdom.getDB().putInt(record + ARGUMENTS, argListRec);
	}
	
	public PDOMCPPClassInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CLASS_INSTANCE;
	}

	public ICPPTemplateDefinition getTemplateDefinition() {
		return (ICPPTemplateDefinition) getSpecializedBinding();
	}
		
	public ICPPTemplateArgument[] getTemplateArguments() {
		try {
			final int rec= getPDOM().getDB().getInt(record + ARGUMENTS);
			return PDOMCPPArgumentList.getArguments(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPTemplateArgument.EMPTY_ARGUMENTS;
		}
	}
	
	@Override
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}
		
		if (type instanceof PDOMNode) {
			PDOMNode node= (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
		}
		
		// require a class instance
		if (!(type instanceof ICPPClassSpecialization) || !(type instanceof ICPPTemplateInstance) ||
				type instanceof IProblemBinding) {
			return false;
		}

		final ICPPClassSpecialization classSpec2 = (ICPPClassSpecialization) type;
		final ICPPClassType orig1= getSpecializedBinding();
		final ICPPClassType orig2= classSpec2.getSpecializedBinding();
		if (!orig1.isSameType(orig2))
			return false;
		
		return CPPTemplates.haveSameArguments(this, (ICPPTemplateInstance) type);
	}
	
	@Deprecated
	public IType[] getArguments() {
		return CPPTemplates.getArguments(getTemplateArguments());
	}
}
