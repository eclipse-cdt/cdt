/*******************************************************************************
 * Copyright (c) 2007, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for template non-type parameter in the index.
 */
class PDOMCPPTemplateNonTypeParameter extends PDOMCPPBinding implements IPDOMMemberOwner,
		ICPPTemplateNonTypeParameter, IPDOMCPPTemplateParameter {

	private static final int TYPE_OFFSET= PDOMCPPBinding.RECORD_SIZE;
	private static final int PARAMETERID= TYPE_OFFSET + Database.TYPE_SIZE;
	private static final int DEFAULTVAL= PARAMETERID + Database.VALUE_SIZE;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = DEFAULTVAL + Database.PTR_SIZE;

	private int fCachedParamID= -1;
	private volatile IType fType;

	public PDOMCPPTemplateNonTypeParameter(PDOMLinkage linkage, PDOMNode parent,
			ICPPTemplateNonTypeParameter param) throws CoreException {
		super(linkage, parent, param.getNameCharArray());
		final Database db = getDB();
		db.putInt(record + PARAMETERID, param.getParameterID());
	}

	public PDOMCPPTemplateNonTypeParameter(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_TEMPLATE_NON_TYPE_PARAMETER;
	}
	
	@Override
	public ICPPTemplateArgument getDefaultValue() {
		try {
			IValue val= getLinkage().loadValue(record + DEFAULTVAL);
			if (val == null) 
				return null;
			return new CPPTemplateArgument(val, getType());
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
	
	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPTemplateNonTypeParameter) {
			ICPPTemplateNonTypeParameter ntp= (ICPPTemplateNonTypeParameter) newBinding;
			updateName(newBinding.getNameCharArray());
			final Database db = getDB();
			try {
				IType newType= ntp.getType();
				setType(linkage, newType);
				setDefaultValue(db, ntp);
			} catch (DOMException e) {
				throw new CoreException(Util.createStatus(e));
			}
		}
	}

	@Override
	public void forceDelete(PDOMLinkage linkage) throws CoreException {
		getDBName().delete();
		linkage.storeType(record+TYPE_OFFSET, null);
		linkage.storeValue(record+DEFAULTVAL, null);
	}

	@Override
	public short getParameterPosition() {
		return (short) getParameterID();
	}
	
	@Override
	public short getTemplateNestingLevel() {
		readParamID();
		return (short)(getParameterID() >> 16);
	}
	
	@Override
	public boolean isParameterPack() {
		return getType() instanceof ICPPParameterPackType;
	}

	@Override
	public int getParameterID() {
		readParamID();
		return fCachedParamID;
	}
	
	private void readParamID() {
		if (fCachedParamID == -1) {
			try {
				final Database db = getDB();
				fCachedParamID= db.getInt(record + PARAMETERID);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fCachedParamID= Integer.MAX_VALUE;
			}
		}
	}
	
	private void setType(final PDOMLinkage linkage, IType newType) throws CoreException, DOMException {
		linkage.storeType(record + TYPE_OFFSET, newType);
	}

	@Override
	public void configure(ICPPTemplateParameter param) {
		try {
			if (param instanceof ICPPTemplateNonTypeParameter) {
				ICPPTemplateNonTypeParameter nonTypeParm= (ICPPTemplateNonTypeParameter) param;
				setType(getLinkage(), nonTypeParm.getType());
				final Database db= getDB();
				setDefaultValue(db, nonTypeParm);
			} 
		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
	}

	private void setDefaultValue(Database db, ICPPTemplateNonTypeParameter nonTypeParm) throws CoreException {
		ICPPTemplateArgument val= nonTypeParm.getDefaultValue();
		if (val != null) {
			IValue sval= val.getNonTypeValue();
			if (sval != null) {
				getLinkage().storeValue(record + DEFAULTVAL, sval);
			}
		}
	}

	@Override
	public IType getType() {
		if (fType == null) {
			try {
				fType= getLinkage().loadType(record + TYPE_OFFSET);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return fType;
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}
	@Override
	public boolean isAuto() {
		return false;
	}
	@Override
	public boolean isExtern() {
		return false;
	}
	@Override
	public boolean isRegister() {
		return false;
	}
	@Override
	public boolean isStatic() {
		return false;
	}
	@Override
	public boolean isExternC() {
		return false;
	}
	@Override
	public boolean isMutable() {
		return false;
	}
	
	@Override
	public Object clone() {
		throw new UnsupportedOperationException(); 
	}
	/**
	 * @deprecated
	 */
	@Override
	@Deprecated
	public IASTExpression getDefault() {
		return null;
	}

}
