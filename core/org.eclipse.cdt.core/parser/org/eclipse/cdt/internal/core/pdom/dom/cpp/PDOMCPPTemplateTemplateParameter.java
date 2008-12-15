/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * mstodo
 *
 */
public class PDOMCPPTemplateTemplateParameter extends PDOMCPPBinding 
		implements ICPPTemplateTemplateParameter, ICPPUnknownBinding, ICPPUnknownType, IIndexType, 
		IPDOMCPPTemplateParameter, IPDOMCPPTemplateParameterOwner {

	private static final int DEFAULT_TYPE = PDOMCPPBinding.RECORD_SIZE + 0;	
	private static final int MEMBERLIST = PDOMCPPBinding.RECORD_SIZE + 4;
	private static final int PARAMETERID= PDOMCPPBinding.RECORD_SIZE + 8;
	private static final int PARAMETERS= PDOMCPPBinding.RECORD_SIZE + 12;

	/**
	 * The size in bytes of a PDOMCPPTemplateTypeParameter record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 14;
	
	private ICPPScope fUnknownScope;
	private int fCachedParamID= -1;
	private IPDOMCPPTemplateParameter[] params;
	
	public PDOMCPPTemplateTemplateParameter(PDOM pdom, PDOMNode parent, ICPPTemplateTemplateParameter param) 
			throws CoreException, DOMException {
		super(pdom, parent, param.getNameCharArray());
		
		final Database db = pdom.getDB();
		db.putInt(record + PARAMETERID, param.getParameterID());
		final ICPPTemplateParameter[] origParams= param.getTemplateParameters();
		final IPDOMCPPTemplateParameter[] params = PDOMTemplateParameterArray.createPDOMTemplateParameters(pdom, this, origParams);
		int rec= PDOMTemplateParameterArray.putArray(db, params);
		pdom.getDB().putInt(record + PARAMETERS, rec);
	}

	public PDOMCPPTemplateTemplateParameter(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
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
				final Database db = pdom.getDB();
				fCachedParamID= db.getInt(record + PARAMETERID);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fCachedParamID= -2;
			}
		}
	}

	@Override
	public void addChild(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.addMember(member);
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
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
			PDOMNode node = getLinkageImpl().getNode(pdom.getDB().getInt(record + DEFAULT_TYPE));
			if (node instanceof IType) {
				return (IType) node;
			}
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
	public Object clone() { fail(); return null; }


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
					final Database db= getPDOM().getDB();
					PDOMNode typeNode = getLinkageImpl().addType(this, dflt);
					if (typeNode != null) {
						db.putInt(record + DEFAULT_TYPE, typeNode.getRecord());
					}
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPTemplateTemplateParameter) {
			final Database db = pdom.getDB();
			ICPPTemplateTemplateParameter ttp= (ICPPTemplateTemplateParameter) newBinding;
			updateName(newBinding.getNameCharArray());
			IType newDefault= null;
			try {
				newDefault = ttp.getDefault();
			} catch (DOMException e) {
				// ignore
			}
			if (newDefault != null) {
				IType mytype= getDefault();
				PDOMNode typeNode = getLinkageImpl().addType(this, newDefault);
				if (typeNode != null) {
					db.putInt(record + DEFAULT_TYPE, typeNode.getRecord());
					if (mytype != null) 
						linkage.deleteType(mytype, record);
				}
			}
			int oldRec= db.getInt(record + PARAMETERS);
			IPDOMCPPTemplateParameter[] oldParams= getTemplateParameters();
			try {
				params= PDOMTemplateParameterArray.createPDOMTemplateParameters(pdom, this, ttp.getTemplateParameters());
				int newRec= PDOMTemplateParameterArray.putArray(db, params);
				db.putInt(record + PARAMETERS, newRec);
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
		IType type= getDefault();
		if (type instanceof PDOMNode) {
			((PDOMNode) type).delete(linkage);
		}
		Database db= pdom.getDB();
		int valueRec= db.getInt(record + DEFAULT_TYPE);
		if (valueRec != 0)
			db.getString(valueRec).delete();

		int oldRec= db.getInt(record + PARAMETERS);
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
				int rec= pdom.getDB().getInt(record + PARAMETERS);
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
}
