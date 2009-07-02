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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for template type parameters in the index.
 */
class PDOMCPPTemplateTypeParameter extends PDOMCPPBinding implements IPDOMMemberOwner,
		ICPPTemplateTypeParameter, ICPPUnknownBinding, ICPPUnknownType, IIndexType,
		IPDOMCPPTemplateParameter {

	private static final int DEFAULT_TYPE = PDOMCPPBinding.RECORD_SIZE + 0;	
	private static final int MEMBERLIST = PDOMCPPBinding.RECORD_SIZE + 4;
	private static final int PARAMETERID= PDOMCPPBinding.RECORD_SIZE + 8;

	/**
	 * The size in bytes of a PDOMCPPTemplateTypeParameter record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 12;
	
	private ICPPScope fUnknownScope;
	private int fCachedParamID= -1;
	
	public PDOMCPPTemplateTypeParameter(PDOMLinkage linkage, PDOMNode parent, ICPPTemplateTypeParameter param) 
			throws CoreException {
		super(linkage, parent, param.getNameCharArray());
		
		final Database db = getDB();
		db.putInt(record + PARAMETERID, param.getParameterID());
	}

	public PDOMCPPTemplateTypeParameter(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_TEMPLATE_TYPE_PARAMETER;
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

        if (!(type instanceof ICPPTemplateTypeParameter))
        	return false;
        
        return getParameterID() == ((ICPPTemplateParameter) type).getParameterID();
	}

	public IType getDefault() {
		try {
			PDOMNode node = getLinkage().getNode(getDB().getRecPtr(record + DEFAULT_TYPE));
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
					PDOMNode typeNode = getLinkage().addType(this, dflt);
					if (typeNode != null) {
						db.putRecPtr(record + DEFAULT_TYPE, typeNode.getRecord());
					}
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
	
	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPTemplateTypeParameter) {
			ICPPTemplateTypeParameter ttp= (ICPPTemplateTypeParameter) newBinding;
			updateName(newBinding.getNameCharArray());
			IType newDefault= null;
			try {
				newDefault = ttp.getDefault();
			} catch (DOMException e) {
				// ignore
			}
			if (newDefault != null) {
				final Database db = getDB();
				IType mytype= getDefault();
				PDOMNode typeNode = getLinkage().addType(this, newDefault);
				if (typeNode != null) {
					db.putRecPtr(record + DEFAULT_TYPE, typeNode.getRecord());
					if (mytype != null) 
						linkage.deleteType(mytype, record);
				}
			}
		}
	}
	
	public void forceDelete(PDOMLinkage linkage) throws CoreException {
		getDBName().delete();
		IType type= getDefault();
		if (type instanceof PDOMNode) {
			((PDOMNode) type).delete(linkage);
		}
	}
}
