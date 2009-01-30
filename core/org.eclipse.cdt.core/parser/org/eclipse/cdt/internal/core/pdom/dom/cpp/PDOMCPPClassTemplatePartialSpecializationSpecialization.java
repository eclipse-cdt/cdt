/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecializationSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
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
	
	private ICPPClassTemplate fPrimaryTemplate;

	public PDOMCPPClassTemplatePartialSpecializationSpecialization(PDOMCPPLinkage linkage,
			PDOMNode parent, PDOMBinding specialized, ICPPClassTemplatePartialSpecialization partial, 
			PDOMCPPClassTemplateSpecialization primary) throws CoreException {
		super(linkage, parent, partial, specialized);		

		getDB().putInt(record + PRIMARY_TEMPLATE, primary.getRecord());
		primary.addPartial(this);
		
		linkage.new ConfigurePartialSpecialization(this, partial);
	
	}

	public PDOMCPPClassTemplatePartialSpecializationSpecialization(PDOMLinkage linkage, int bindingRecord) {
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
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() throws DOMException {
		return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
	}
	
	public PDOMCPPClassTemplatePartialSpecializationSpecialization getNextPartial() throws CoreException {
		int value = getDB().getInt(record + NEXT_PARTIAL);
		return value != 0 ? new PDOMCPPClassTemplatePartialSpecializationSpecialization(getLinkage(), value) : null;
	}
	
	public void setNextPartial(PDOMCPPClassTemplatePartialSpecializationSpecialization partial) throws CoreException {
		int value = partial != null ? partial.getRecord() : 0;
		getDB().putInt(record + NEXT_PARTIAL, value);
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
		try {
			ICPPClassType ct1= getPrimaryClassTemplate();
			ICPPClassType ct2= rhs.getPrimaryClassTemplate();
			if(!ct1.isSameType(ct2))
				return false;

			ICPPTemplateArgument[] args1= getTemplateArguments();
			ICPPTemplateArgument[] args2= rhs.getTemplateArguments();
			if (args1.length != args2.length)
				return false;

			for (int i = 0; i < args2.length; i++) {
				if (args1[i].isSameValue(args2[i])) 
					return false;
			}
		} catch (DOMException e) {
			return false;
		}
		return true;
	}

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
	
	public void setArguments(ICPPTemplateArgument[] templateArguments) throws CoreException {
		final Database db = getPDOM().getDB();
		int oldRec = db.getInt(record+ARGUMENTS);
		int rec= PDOMCPPArgumentList.putArguments(this, templateArguments);
		db.putInt(record+ARGUMENTS, rec);
		if (oldRec != 0) {
			PDOMCPPArgumentList.clearArguments(this, oldRec);
		}
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
}
