/*******************************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bryan Wilkinson (QNX) - Initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
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
	
	private volatile ICPPTemplateArgument[] fTemplateArguments;
	
	public PDOMCPPClassInstance(PDOMCPPLinkage linkage, PDOMNode parent, ICPPClassType classType, PDOMBinding orig)
			throws CoreException {
		super(linkage, parent, classType, orig);
		final ICPPTemplateInstance asInstance= (ICPPTemplateInstance) classType;
		// Defer storing of template arguments to the post-process
		// to avoid infinite recursion when the evaluation of a non-type 
		// template argument tries to store its template definition.
		// Until the post-process runs, temporarily store the input (possibly
		// non-PDOM) arguments.
		fTemplateArguments = asInstance.getTemplateArguments();
		linkage.new ConfigureClassInstance(this);
	}
	
	public PDOMCPPClassInstance(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CLASS_INSTANCE;
	}

	@Override
	public ICPPTemplateDefinition getTemplateDefinition() {
		return (ICPPTemplateDefinition) getSpecializedBinding();
	}
		
	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		if (fTemplateArguments == null) {
			try {
				final long rec= getPDOM().getDB().getRecPtr(record + ARGUMENTS);
				fTemplateArguments = PDOMCPPArgumentList.getArguments(this, rec);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return fTemplateArguments;
	}
	
	public void storeTemplateArguments() {
		try {
			// fTemplateArguments here are the temporarily stored, possibly non-PDOM arguments stored
			// by the constructor. Construct the PDOM arguments and store them.
			final long argListRec= PDOMCPPArgumentList.putArguments(this, fTemplateArguments);
			getDB().putRecPtr(record + ARGUMENTS, argListRec);
			
			// Read the stored arguments next time getTemplateArguments() is called.
			fTemplateArguments = null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
	
	@Override
	protected boolean hasOwnScope() throws CoreException {
		// An instance with a declaration does not use the original template.
		return hasDeclaration();
	}
	
	@Override
	public boolean isExplicitSpecialization() {
		return !(getCompositeScope() instanceof ICPPClassSpecializationScope);
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
		
		return CPPClassInstance.isSameClassInstance(this, type);
	}
	
	@Override
	@Deprecated
	public IType[] getArguments() {
		return CPPTemplates.getArguments(getTemplateArguments());
	}
}
