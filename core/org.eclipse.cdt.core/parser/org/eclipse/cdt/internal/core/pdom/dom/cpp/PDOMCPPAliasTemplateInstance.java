/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.index.CPPAliasTemplateInstanceClone;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * PDOM binding for alias template instance.
 */
class PDOMCPPAliasTemplateInstance extends PDOMCPPTypedefSpecialization implements ICPPAliasTemplateInstance {
	private static final int TEMPLATE_ARGUMENTS = PDOMCPPTypedefSpecialization.RECORD_SIZE; // Database.PTR_SIZE

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = TEMPLATE_ARGUMENTS + Database.PTR_SIZE;

	private volatile ICPPTemplateArgument[] fTemplateArguments;

	public PDOMCPPAliasTemplateInstance(PDOMCPPLinkage linkage, PDOMNode parent, PDOMBinding aliasTemplate,
			ICPPAliasTemplateInstance astInstance) throws CoreException, DOMException {
		super(linkage, parent, astInstance, aliasTemplate);
		fTemplateArguments = astInstance.getTemplateArguments();
		linkage.new ConfigureAliasTemplateInstance(this);
	}

	public PDOMCPPAliasTemplateInstance(PDOMCPPLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_ALIAS_TEMPLATE_INSTANCE;
	}

	@Override
	public ICPPAliasTemplate getTemplateDefinition() {
		return (ICPPAliasTemplate) getSpecializedBinding();
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public Object clone() {
		return new CPPAliasTemplateInstanceClone(this);
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		if (fTemplateArguments == null) {
			try {
				final long rec = getPDOM().getDB().getRecPtr(record + TEMPLATE_ARGUMENTS);
				fTemplateArguments = PDOMCPPArgumentList.getArguments(this, rec);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fTemplateArguments = ICPPTemplateArgument.EMPTY_ARGUMENTS;
			}
		}
		return fTemplateArguments;
	}

	public void storeTemplateArguments() {
		try {
			// fTemplateArguments here are the temporarily stored, possibly non-PDOM arguments stored
			// by the constructor. Construct the PDOM arguments and store them.
			final long argListRec = PDOMCPPArgumentList.putArguments(this, fTemplateArguments);
			getDB().putRecPtr(record + TEMPLATE_ARGUMENTS, argListRec);

			// Read the stored arguments next time getTemplateArguments() is called.
			fTemplateArguments = null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public boolean isExplicitSpecialization() {
		// Alias templates cannot be explicitly specialized.
		return false;
	}
}