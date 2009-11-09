/*******************************************************************************
 * Copyright (c) 2007, 2009 QNX Software Systems and others.
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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
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
	@SuppressWarnings("hiding")
	private static final int RECORD_SIZE = NEXT_PARAM + Database.PTR_SIZE;

	private final IType fType;
	
	public PDOMCPPParameterSpecialization(PDOMLinkage linkage, long record, IType t) {
		super(linkage, record);
		fType= t;
	}
		
	public PDOMCPPParameterSpecialization(PDOMLinkage linkage, PDOMCPPFunctionSpecialization parent, IParameter param,
			PDOMCPPParameter specialized, PDOMCPPParameterSpecialization next) throws CoreException {
		super(linkage, parent, (ICPPSpecialization) param, specialized);
		fType= null;  // this constructor is used for adding parameters to the database, only.
		
		Database db = getDB();
		db.putRecPtr(record + NEXT_PARAM, next == null ? 0 : next.getRecord());
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_PARAMETER_SPECIALIZATION;
	}

	public PDOMCPPParameterSpecialization getNextParameter(IType t) throws CoreException {
		long rec = getNextPtr();
		return rec != 0 ? new PDOMCPPParameterSpecialization(getLinkage(), rec, t) : null;
	}

	long getNextPtr() throws CoreException {
		long rec = getDB().getRecPtr(record + NEXT_PARAM);
		return rec;
	}
	
	public IType getType() {
		return fType;
	}
	
	@Override
	protected IPDOMBinding loadSpecializedBinding(long record) throws CoreException {
		if (record == 0)
			return null;
		IType type= null;
		IBinding parent = getParentBinding();
		if (parent instanceof ICPPSpecialization && parent instanceof ICPPFunction) {
			try {
				IParameter[] pars= ((ICPPFunction) parent).getParameters();
				int parPos= -1;
				for (parPos= 0; parPos<pars.length; parPos++) {
					IParameter par= pars[parPos];
					if (equals(par)) {
						break;
					}
				}
				if (parPos < pars.length) {
					parent= ((ICPPSpecialization) parent).getSpecializedBinding();
					if (parent instanceof ICPPFunction) {
						ICPPFunctionType ftype = ((ICPPFunction) parent).getType();
						if (ftype != null) {
							IType[] ptypes= ftype.getParameterTypes();
							if (parPos < ptypes.length) {
								type= ptypes[parPos];
							}
						}
					}
				}
			} catch (DOMException e) {
			}
		} 
		return new PDOMCPPParameter(getLinkage(), record, type);
	}

	private ICPPParameter getParameter(){
		return (ICPPParameter) getSpecializedBinding();
	}
	
	public boolean hasDefaultValue() {
		return getParameter().hasDefaultValue();
	}

	public boolean isAuto() throws DOMException {
		return getParameter().isAuto();
	}

	public boolean isRegister() throws DOMException {
		return getParameter().isRegister();
	}

	public boolean isExtern() throws DOMException {
		return false;
	}

	public boolean isExternC() {
		return false;
	}

	public boolean isStatic() throws DOMException {
		return false;
	}

	public boolean isMutable() throws DOMException {
		return false;
	}

	public IValue getInitialValue() {
		return null;
	}
}
