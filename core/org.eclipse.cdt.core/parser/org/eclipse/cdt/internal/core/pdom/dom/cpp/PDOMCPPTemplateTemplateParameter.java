/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Implementation of template template parameters for the index.
 */
public class PDOMCPPTemplateTemplateParameter extends PDOMCPPBinding 
		implements ICPPTemplateTemplateParameter, ICPPUnknownBinding, ICPPUnknownType, IIndexType, 
		IPDOMCPPTemplateParameter, IPDOMCPPTemplateParameterOwner {

	private static final int PACK_BIT = 1 << 31;

	private static final int DEFAULT_TYPE = PDOMCPPBinding.RECORD_SIZE;	
	private static final int MEMBERLIST = DEFAULT_TYPE + Database.TYPE_SIZE;
	private static final int PARAMETERID= MEMBERLIST + Database.PTR_SIZE;
	private static final int PARAMETERS= PARAMETERID + 4;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PARAMETERS + Database.PTR_SIZE;
	
	private ICPPScope fUnknownScope;
	private int fCachedParamID= -1;
	private IPDOMCPPTemplateParameter[] params;
	
	public PDOMCPPTemplateTemplateParameter(PDOMLinkage linkage, PDOMNode parent, ICPPTemplateTemplateParameter param) 
			throws CoreException, DOMException {
		super(linkage, parent, param.getNameCharArray());
		
		final Database db = getDB();
		int id= param.getParameterID();
		if (param.isParameterPack()) {
			id |= PACK_BIT;
		}
		db.putInt(record + PARAMETERID, id);
		final ICPPTemplateParameter[] origParams= param.getTemplateParameters();
		final IPDOMCPPTemplateParameter[] params = PDOMTemplateParameterArray.createPDOMTemplateParameters(linkage, this, origParams);
		long rec= PDOMTemplateParameterArray.putArray(db, params);
		getDB().putRecPtr(record + PARAMETERS, rec);
	}

	public PDOMCPPTemplateTemplateParameter(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_TEMPLATE_TEMPLATE_PARAMETER;
	}
	
	public short getParameterPosition() {
		return (short) getParameterID();
	}
	
	public short getTemplateNestingLevel() {
		readParamID();
		return (short)(getParameterID() >> 16);
	}
	
	public boolean isParameterPack() {
		readParamID();
		return (fCachedParamID & PACK_BIT) != 0;
	}

	public int getParameterID() {
		readParamID();
		return fCachedParamID & ~PACK_BIT;
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

	@Override
	public void addChild(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + MEMBERLIST);
		list.addMember(member);
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + MEMBERLIST);
		list.accept(visitor);
	}
	
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}

        if (!(type instanceof ICPPTemplateTemplateParameter))
        	return false;
        
        return getParameterID() == ((ICPPTemplateParameter) type).getParameterID();
	}

	public IType getDefault() {
		try {
			return getLinkage().loadType(record + DEFAULT_TYPE);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
		
	public ICPPTemplateArgument getDefaultValue() {
		IType d= getDefault();
		if (d == null)
			return null;
		
		return new CPPTemplateArgument(d);
	}
	
	@Override
	public Object clone() { 
		throw new UnsupportedOperationException(); 
	}


	public ICPPScope asScope() {
		if (fUnknownScope == null) {
			fUnknownScope= new PDOMCPPUnknownScope(this, new CPPASTName(getNameCharArray()));
		}
		return fUnknownScope;
	}

	public IASTName getUnknownName() {
		return new CPPASTName(getNameCharArray());
	}

	public void configure(ICPPTemplateParameter param) {
		try {
			ICPPTemplateArgument val= param.getDefaultValue();
			if (val != null) {
				IType dflt= val.getTypeValue();
				if (dflt != null) {
					getLinkage().storeType(record + DEFAULT_TYPE, dflt);
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPTemplateTemplateParameter) {
			final Database db = getDB();
			ICPPTemplateTemplateParameter ttp= (ICPPTemplateTemplateParameter) newBinding;
			updateName(newBinding.getNameCharArray());
			IType newDefault= null;
			try {
				newDefault = ttp.getDefault();
			} catch (DOMException e) {
				// ignore
			}
			if (newDefault != null) {
				linkage.storeType(record + DEFAULT_TYPE, newDefault);
			}
			long oldRec= db.getRecPtr(record + PARAMETERS);
			IPDOMCPPTemplateParameter[] oldParams= getTemplateParameters();
			try {
				params= PDOMTemplateParameterArray.createPDOMTemplateParameters(getLinkage(), this, ttp.getTemplateParameters());
				long newRec= PDOMTemplateParameterArray.putArray(db, params);
				db.putRecPtr(record + PARAMETERS, newRec);
				if (oldRec != 0)
					db.free(oldRec);
				for (IPDOMCPPTemplateParameter opar : oldParams) {
					opar.forceDelete(linkage);
				} 
			} catch (DOMException e) {
			}
		}
	}

	public void forceDelete(PDOMLinkage linkage) throws CoreException {
		getDBName().delete();
		linkage.storeType(record + DEFAULT_TYPE, null);

		final Database db= getDB();
		long oldRec= db.getRecPtr(record + PARAMETERS);
		IPDOMCPPTemplateParameter[] oldParams= getTemplateParameters();
		if (oldRec != 0)
			db.free(oldRec);
		for (IPDOMCPPTemplateParameter opar : oldParams) {
			opar.forceDelete(linkage);
		} 
	}

	public IPDOMCPPTemplateParameter[] getTemplateParameters() {
		if (params == null) {
			try {
				long rec= getDB().getRecPtr(record + PARAMETERS);
				if (rec == 0) {
					params= IPDOMCPPTemplateParameter.EMPTY_ARRAY;
				} else {
					params= PDOMTemplateParameterArray.getArray(this, rec);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
				params = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
			}
		}
		return params;
	}

	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
	}

	public IField findField(String name) {
		return null;
	}

	public ICPPMethod[] getAllDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	public ICPPBase[] getBases() {
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	public ICPPField[] getDeclaredFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	public ICPPMethod[] getDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	public IField[] getFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	public IBinding[] getFriends() {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	public ICPPMethod[] getMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	public ICPPClassType[] getNestedClasses() {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

	public IScope getCompositeScope() {
		return asScope();
	}

	public int getKey() {
		return 0;
	}

	public boolean isAnonymous() {
		return false;
	}

	public ICPPTemplateParameter adaptTemplateParameter(ICPPTemplateParameter param) {
		int pos = param.getParameterPosition();
		ICPPTemplateParameter[] pars = getTemplateParameters();
		
		if (pars == null || pos >= pars.length)
			return null;
		
		ICPPTemplateParameter result= pars[pos];
		if (param instanceof ICPPTemplateTypeParameter) {
			if (result instanceof ICPPTemplateTypeParameter)
				return result;
		} else if (param instanceof ICPPTemplateNonTypeParameter) {
			if (result instanceof ICPPTemplateNonTypeParameter)
				return result;
		} else if (param instanceof ICPPTemplateTemplateParameter) {
			if (result instanceof ICPPTemplateTemplateParameter)
				return result;
		}
		return null;
	}

	public ICPPDeferredClassInstance asDeferredInstance() throws DOMException {
		return null;
	}
}
