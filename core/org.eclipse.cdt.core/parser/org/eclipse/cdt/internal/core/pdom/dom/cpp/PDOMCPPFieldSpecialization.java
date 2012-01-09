/*******************************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for a specialization of a field, used in the index.
 */
class PDOMCPPFieldSpecialization extends PDOMCPPSpecialization implements ICPPField {

	private static final int TYPE_OFFSET = PDOMCPPSpecialization.RECORD_SIZE + 0;
	private static final int VALUE_OFFSET = TYPE_OFFSET + Database.TYPE_SIZE;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = VALUE_OFFSET + Database.VALUE_SIZE;
	
	public PDOMCPPFieldSpecialization(PDOMLinkage linkage, PDOMNode parent,
			ICPPField field, PDOMBinding specialized)
			throws CoreException {
		super(linkage, parent, (ICPPSpecialization) field, specialized);
		
		linkage.storeType(record + TYPE_OFFSET, field.getType());
		linkage.storeValue(record + VALUE_OFFSET, field.getInitialValue());
	}

	public PDOMCPPFieldSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FIELD_SPECIALIZATION;
	}

	private ICPPField getField() {
		return (ICPPField) getSpecializedBinding();
	}
	
	@Override
	public ICompositeType getCompositeTypeOwner() {
		return getClassOwner();
	}

	@Override
	public IType getType() {
		try {
			return getLinkage().loadType(record + TYPE_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ProblemType(ISemanticProblem.TYPE_NOT_PERSISTED);
		}
	}

	@Override
	public IValue getInitialValue() {
		try {
			return getLinkage().loadValue(record + VALUE_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	@Override
	public boolean isAuto() {
		return getField().isAuto();
	}

	@Override
	public boolean isExtern() {
		return getField().isExtern();
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public boolean isRegister() {
		return getField().isRegister();
	}

	@Override
	public boolean isStatic() {
		return getField().isStatic();
	}

	@Override
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	@Override
	public int getVisibility() {
		return getField().getVisibility();
	}

	@Override
	public boolean isMutable() {
		return getField().isMutable();
	}
}
