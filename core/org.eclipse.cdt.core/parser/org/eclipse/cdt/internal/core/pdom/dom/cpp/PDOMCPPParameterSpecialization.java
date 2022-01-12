/*******************************************************************************
 * Copyright (c) 2007, 2014 QNX Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for a specialization of a parameter in the index.
 */
class PDOMCPPParameterSpecialization extends PDOMCPPSpecialization implements ICPPParameter {
	private static final int NEXT_PARAM = PDOMCPPSpecialization.RECORD_SIZE;
	private static final int DEFAULT_VALUE = NEXT_PARAM + Database.PTR_SIZE;
	@SuppressWarnings("hiding")
	private static final int RECORD_SIZE = DEFAULT_VALUE + Database.VALUE_SIZE;

	private final IType fType;
	private volatile IValue fDefaultValue = IntegralValue.NOT_INITIALIZED;

	public PDOMCPPParameterSpecialization(PDOMLinkage linkage, long record, IType t) {
		super(linkage, record);
		fType = t;
	}

	public PDOMCPPParameterSpecialization(PDOMCPPLinkage linkage, PDOMCPPFunctionSpecialization parent,
			ICPPParameter astParam, PDOMCPPParameter original, PDOMCPPParameterSpecialization next)
			throws CoreException {
		super(linkage, parent, (ICPPSpecialization) astParam, original);
		fType = null; // This constructor is used for adding parameters to the database, only.
		fDefaultValue = astParam.getDefaultValue();

		Database db = getDB();
		db.putRecPtr(record + NEXT_PARAM, next == null ? 0 : next.getRecord());
		linkage.storeValue(record + DEFAULT_VALUE, fDefaultValue);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_PARAMETER_SPECIALIZATION;
	}

	long getNextPtr() throws CoreException {
		long rec = getDB().getRecPtr(record + NEXT_PARAM);
		return rec;
	}

	@Override
	public IType getType() {
		return fType;
	}

	@Override
	protected IPDOMBinding loadSpecializedBinding(long record) throws CoreException {
		if (record == 0)
			return null;
		IType type = null;
		IBinding parent = getParentBinding();
		if (parent instanceof ICPPSpecialization && parent instanceof ICPPFunction) {
			IParameter[] pars = ((ICPPFunction) parent).getParameters();
			int parPos = -1;
			for (parPos = 0; parPos < pars.length; parPos++) {
				IParameter par = pars[parPos];
				if (equals(par)) {
					break;
				}
			}
			if (parPos < pars.length) {
				parent = ((ICPPSpecialization) parent).getSpecializedBinding();
				if (parent instanceof ICPPFunction) {
					ICPPFunctionType ftype = ((ICPPFunction) parent).getType();
					if (ftype != null) {
						IType[] ptypes = ftype.getParameterTypes();
						if (parPos < ptypes.length) {
							type = ptypes[parPos];
						}
					}
				}
			}
		}
		return new PDOMCPPParameter(getLinkage(), record, type);
	}

	private ICPPParameter getParameter() {
		return (ICPPParameter) getSpecializedBinding();
	}

	@Override
	public boolean hasDefaultValue() {
		return getParameter().hasDefaultValue();
	}

	@Override
	public IValue getDefaultValue() {
		if (fDefaultValue == IntegralValue.NOT_INITIALIZED) {
			try {
				fDefaultValue = getLinkage().loadValue(record + DEFAULT_VALUE);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fDefaultValue = null;
			}
		}
		return fDefaultValue;
	}

	@Override
	public boolean isParameterPack() {
		return getType() instanceof ICPPParameterPackType;
	}

	@Override
	public boolean isAuto() {
		return getParameter().isAuto();
	}

	@Override
	public boolean isRegister() {
		return getParameter().isRegister();
	}

	@Override
	public boolean isExtern() {
		return false;
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public boolean isConstexpr() {
		return false;
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}
}
