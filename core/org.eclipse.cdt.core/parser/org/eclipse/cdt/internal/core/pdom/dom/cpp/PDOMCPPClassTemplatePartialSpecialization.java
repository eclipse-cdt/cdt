/*******************************************************************************
 * Copyright (c) 2007, 2016 QNX Software Systems and others.
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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IndexCPPSignatureUtil;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMOverloader;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Partial specialization of a class template for the index.
 */
class PDOMCPPClassTemplatePartialSpecialization extends PDOMCPPClassTemplate
		implements IPDOMPartialSpecialization, IPDOMOverloader, ICPPClassTemplatePartialSpecialization {
	private static final int ARGUMENTS = PDOMCPPClassTemplate.RECORD_SIZE + 0;
	private static final int SIGNATURE_HASH = PDOMCPPClassTemplate.RECORD_SIZE + 4;
	private static final int PRIMARY = PDOMCPPClassTemplate.RECORD_SIZE + 8;
	private static final int NEXT_PARTIAL = PDOMCPPClassTemplate.RECORD_SIZE + 12;

	/**
	 * The size in bytes of a PDOMCPPClassTemplatePartialSpecialization record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPClassTemplate.RECORD_SIZE + 16;

	public PDOMCPPClassTemplatePartialSpecialization(PDOMCPPLinkage linkage, PDOMNode parent,
			ICPPClassTemplatePartialSpecialization partial, PDOMCPPClassTemplate primary)
			throws CoreException, DOMException {
		super(linkage, parent, partial, false);
		getDB().putRecPtr(record + PRIMARY, primary.getRecord());
		primary.addPartial(this);

		try {
			Integer sigHash = IndexCPPSignatureUtil.getSignatureHash(partial);
			getDB().putInt(record + SIGNATURE_HASH, sigHash != null ? sigHash.intValue() : 0);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
		linkage.new ConfigurePartialSpecialization(this, partial);
	}

	public PDOMCPPClassTemplatePartialSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	public int getSignatureHash() throws CoreException {
		return getDB().getInt(record + SIGNATURE_HASH);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CLASS_TEMPLATE_PARTIAL_SPEC;
	}

	public PDOMCPPClassTemplatePartialSpecialization getNextPartial() throws CoreException {
		long value = getDB().getRecPtr(record + NEXT_PARTIAL);
		return value != 0 ? new PDOMCPPClassTemplatePartialSpecialization(getLinkage(), value) : null;
	}

	public void setNextPartial(PDOMCPPClassTemplatePartialSpecialization partial) throws CoreException {
		long value = partial != null ? partial.getRecord() : 0;
		getDB().putRecPtr(record + NEXT_PARTIAL, value);
	}

	@Override
	public ICPPClassTemplate getPrimaryClassTemplate() {
		try {
			return new PDOMCPPClassTemplate(getLinkage(), getDB().getRecPtr(record + PRIMARY));
		} catch (CoreException e) {
			CCorePlugin.log("Failed to load primary template for " + getName(), e); //$NON-NLS-1$
			return null;
		}
	}

	@Override
	public void setTemplateArguments(ICPPTemplateArgument[] templateArguments) throws CoreException {
		final Database db = getPDOM().getDB();
		long oldRec = db.getRecPtr(record + ARGUMENTS);
		long rec = PDOMCPPArgumentList.putArguments(this, templateArguments);
		db.putRecPtr(record + ARGUMENTS, rec);
		if (oldRec != 0) {
			PDOMCPPArgumentList.clearArguments(this, oldRec);
		}
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		try {
			final long rec = getPDOM().getDB().getRecPtr(record + ARGUMENTS);
			return PDOMCPPArgumentList.getArguments(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log("Failed to load template arguments for " + getName(), e); //$NON-NLS-1$
			return ICPPTemplateArgument.EMPTY_ARGUMENTS;
		}
	}

	@Override
	public int pdomCompareTo(PDOMBinding other) {
		int cmp = super.pdomCompareTo(other);
		if (cmp == 0) {
			if (other instanceof PDOMCPPClassTemplatePartialSpecialization) {
				try {
					PDOMCPPClassTemplatePartialSpecialization otherSpec = (PDOMCPPClassTemplatePartialSpecialization) other;
					int mySM = getSignatureHash();
					int otherSM = otherSpec.getSignatureHash();
					return mySM == otherSM ? 0 : mySM < otherSM ? -1 : 1;
				} catch (CoreException e) {
					CCorePlugin.log("Comparison failure for " + getName(), e); //$NON-NLS-1$
				}
			} else {
				assert false;
				CCorePlugin.log(new AssertionError("Assertion failure for " + getName())); //$NON-NLS-1$
			}
		}
		return cmp;
	}

	@Override
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}

		if (type instanceof PDOMNode) {
			PDOMNode node = (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
		}

		if (!(type instanceof ICPPClassTemplatePartialSpecialization)) {
			return false;
		}

		final ICPPClassTemplatePartialSpecialization rhs = (ICPPClassTemplatePartialSpecialization) type;
		return CPPClassTemplatePartialSpecialization.isSamePartialClassSpecialization(this, rhs);
	}

	@Override
	public ICPPTemplateDefinition getPrimaryTemplate() {
		return getPrimaryClassTemplate();
	}
}
