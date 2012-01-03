/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecializationSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * A partial specialization further specialized in the context of a class specialization.
 */
class PDOMCPPClassTemplatePartialSpecializationSpecialization extends PDOMCPPClassTemplateSpecialization 
		implements IPDOMPartialSpecialization, ICPPClassTemplatePartialSpecializationSpecialization {

	private static final int PRIMARY_TEMPLATE = PDOMCPPClassTemplateSpecialization.RECORD_SIZE;
	private static final int ARGUMENTS = PDOMCPPClassTemplateSpecialization.RECORD_SIZE+4;
	private static final int NEXT_PARTIAL = PDOMCPPClassTemplateSpecialization.RECORD_SIZE+8;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE= PDOMCPPClassTemplateSpecialization.RECORD_SIZE+12;
	
	private volatile ICPPClassTemplate fPrimaryTemplate;

	public PDOMCPPClassTemplatePartialSpecializationSpecialization(PDOMCPPLinkage linkage,
			PDOMNode parent, PDOMBinding specialized, ICPPClassTemplatePartialSpecialization partial, 
			PDOMCPPClassTemplateSpecialization primary) throws CoreException {
		super(linkage, parent, partial, specialized);		

		getDB().putRecPtr(record + PRIMARY_TEMPLATE, primary.getRecord());
		
		linkage.new ConfigurePartialSpecialization(this, partial);
	
	}

	public PDOMCPPClassTemplatePartialSpecializationSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CLASS_TEMPLATE_PARTIAL_SPEC_SPEC;
	}
		
	@Override
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
	}
	
	public PDOMCPPClassTemplatePartialSpecializationSpecialization getNextPartial() throws CoreException {
		long value = getDB().getRecPtr(record + NEXT_PARTIAL);
		return value != 0 ? new PDOMCPPClassTemplatePartialSpecializationSpecialization(getLinkage(), value) : null;
	}
	
	public void setNextPartial(PDOMCPPClassTemplatePartialSpecializationSpecialization partial) throws CoreException {
		long value = partial != null ? partial.getRecord() : 0;
		getDB().putRecPtr(record + NEXT_PARTIAL, value);
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
		
		if (!(type instanceof ICPPClassTemplatePartialSpecialization)) {
			return false;
		}

		final ICPPClassTemplatePartialSpecialization rhs = (ICPPClassTemplatePartialSpecialization)type;
		return CPPClassTemplatePartialSpecialization.isSamePartialClassSpecialization(this, rhs);
	}

	@Override
	public ICPPClassTemplate getPrimaryClassTemplate() {
		if (fPrimaryTemplate == null) {
			try {
				int specializedRec = getDB().getInt(record + PRIMARY_TEMPLATE);
				fPrimaryTemplate= (ICPPClassTemplate) getLinkage().getNode(specializedRec);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return fPrimaryTemplate;
	}
	
	@Override
	public void setArguments(ICPPTemplateArgument[] templateArguments) throws CoreException {
		final Database db = getPDOM().getDB();
		long oldRec = db.getRecPtr(record+ARGUMENTS);
		long rec= PDOMCPPArgumentList.putArguments(this, templateArguments);
		db.putRecPtr(record+ARGUMENTS, rec);
		if (oldRec != 0) {
			PDOMCPPArgumentList.clearArguments(this, oldRec);
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
	@Deprecated
	public IType[] getArguments() {
		return CPPTemplates.getArguments(getTemplateArguments());
	}
}
