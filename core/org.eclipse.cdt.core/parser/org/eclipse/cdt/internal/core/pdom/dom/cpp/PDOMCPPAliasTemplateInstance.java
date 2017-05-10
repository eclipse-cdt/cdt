/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.internal.core.index.CPPAliasTemplateInstanceClone;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * PDOM binding for alias template instance.
 */
class PDOMCPPAliasTemplateInstance extends PDOMCPPTypedef implements ICPPAliasTemplateInstance {
	private static final int TEMPLATE_DEFINITION_OFFSET = PDOMCPPTypedef.RECORD_SIZE;
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = TEMPLATE_DEFINITION_OFFSET + Database.PTR_SIZE;

	private volatile ICPPAliasTemplate fTemplateDefinition;

	public PDOMCPPAliasTemplateInstance(PDOMCPPLinkage linkage, PDOMNode parent, PDOMBinding templateDefinition,
			ICPPAliasTemplateInstance binding) throws CoreException, DOMException {
		super(linkage, parent, binding);
		getDB().putRecPtr(record + TEMPLATE_DEFINITION_OFFSET, templateDefinition.getRecord());
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
		if (fTemplateDefinition == null) {
			try {
				long templateRec = getDB().getRecPtr(record + TEMPLATE_DEFINITION_OFFSET);
				fTemplateDefinition = (ICPPAliasTemplate) PDOMNode.load(getPDOM(), templateRec);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return fTemplateDefinition;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public Object clone() {
		return new CPPAliasTemplateInstanceClone(this);
	}
}