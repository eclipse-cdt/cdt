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
import org.eclipse.cdt.internal.core.pdom.dom.PDOMValue;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for template non-type parameter in the index.
 */
class PDOMCPPTemplateNonTypeParameter extends PDOMCPPBinding implements IPDOMMemberOwner,
		ICPPTemplateNonTypeParameter, IPDOMCPPTemplateParameter {

	private static final int TYPE_OFFSET= PDOMCPPBinding.RECORD_SIZE;
	private static final int PARAMETERID= PDOMCPPBinding.RECORD_SIZE + 4;
	private static final int DEFAULTVAL= PDOMCPPBinding.RECORD_SIZE + 8;

	private int fCachedParamID= -1;

	/**
	 * The size in bytes of a PDOMCPPTemplateTypeParameter record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPVariable.RECORD_SIZE + 12;
	
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
	
	public ICPPTemplateArgument getDefaultValue() {
		try {
			final Database db = getDB();
			long rec= db.getRecPtr(record + DEFAULTVAL);
			IValue val= PDOMValue.restore(db, getLinkage(), rec);
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
			IType mytype= getType();
			long valueRec= db.getRecPtr(record + DEFAULTVAL);
			try {
				IType newType= ntp.getType();
				setType(linkage, newType);
				if (mytype != null) 
					linkage.deleteType(mytype, record);
				if (setDefaultValue(db, ntp)) {
					PDOMValue.delete(db, valueRec);
				}
			} catch (DOMException e) {
				throw new CoreException(Util.createStatus(e));
			}
		}
	}

	public void forceDelete(PDOMLinkage linkage) throws CoreException {
		getDBName().delete();
		IType type= getType();
		if (type instanceof PDOMNode) {
			((PDOMNode) type).delete(linkage);
		}
		Database db= getDB();
		long valueRec= db.getRecPtr(record + DEFAULTVAL);
		PDOMValue.delete(db, valueRec);
	}

	public short getParameterPosition() {
		readParamID();
		return (short) fCachedParamID;
	}
	
	public short getTemplateNestingLevel() {
		readParamID();
		return (short)(fCachedParamID >> 16);
	}
	
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
				fCachedParamID= -2;
			}
		}
	}
	
	private void setType(final PDOMLinkage linkage, IType newType) throws CoreException, DOMException {
		PDOMNode typeNode = linkage.addType(this, newType);
		getDB().putRecPtr(record + TYPE_OFFSET, typeNode != null ? typeNode.getRecord() : 0);
	}

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

	private boolean setDefaultValue(Database db, ICPPTemplateNonTypeParameter nonTypeParm) throws CoreException {
		ICPPTemplateArgument val= nonTypeParm.getDefaultValue();
		if (val != null) {
			IValue sval= val.getNonTypeValue();
			if (sval != null) {
				long valueRec= PDOMValue.store(db, getLinkage(), sval);
				db.putRecPtr(record + DEFAULTVAL, valueRec);
				return true;
			}
		}
		return false;
	}

	public IType getType() {
		try {
			long typeRec = getDB().getRecPtr(record + TYPE_OFFSET);
			return (IType)getLinkage().getNode(typeRec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public IValue getInitialValue() {
		return null;
	}
	public boolean isAuto() {
		return false;
	}
	public boolean isExtern() {
		return false;
	}
	public boolean isRegister() {
		return false;
	}
	public boolean isStatic() {
		return false;
	}
	public boolean isExternC() {
		return false;
	}
	public boolean isMutable() {
		return false;
	}
	
	@Override
	public Object clone() {
		fail();
		return null;
	}
	/**
	 * @deprecated
	 */
	@Deprecated
	public IASTExpression getDefault() {
		return null;
	}

}
