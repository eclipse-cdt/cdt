/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * IBM Corporation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPFunction extends PDOMCPPBinding implements IIndexType, ICPPFunction, ICPPFunctionType {

	/**
	 * Offset of total number of function parameters (relative to the
	 * beginning of the record).
	 */
	private static final int NUM_PARAMS = PDOMBinding.RECORD_SIZE + 0;

	/**
	 * Offset of pointer to the first parameter of this function (relative to
	 * the beginning of the record).
	 */
	private static final int FIRST_PARAM = PDOMBinding.RECORD_SIZE + 4;
	
	/**
	 * Offset of hash of parameter information to allow fast comparison
	 */
	private static final int SIGNATURE_MEMENTO = PDOMBinding.RECORD_SIZE + 8;

	/**
	 * Offset for return type of this function (relative to
	 * the beginning of the record).
	 */
	private static final int RETURN_TYPE = PDOMBinding.RECORD_SIZE + 12;	

	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	protected static final int ANNOTATION = PDOMBinding.RECORD_SIZE + 16; // byte
	
	/**
	 * The size in bytes of a PDOMCPPFunction record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 17;
	
	public PDOMCPPFunction(PDOM pdom, PDOMNode parent, ICPPFunction function) throws CoreException {
		super(pdom, parent, function.getNameCharArray());
		
		Database db = pdom.getDB();
		IBinding binding = function;
		try {
			IFunctionType ft= function.getType();
			IType rt= ft.getReturnType();
			if (rt != null) {
				PDOMNode typeNode = getLinkageImpl().addType(this, rt);
				if (typeNode != null) {
					db.putInt(record + RETURN_TYPE, typeNode.getRecord());
				}
			}

			IParameter[] params= function.getParameters();
			IType[] paramTypes= ft.getParameterTypes();
			db.putInt(record + NUM_PARAMS, params.length);
			
			for (int i=0; i<params.length; ++i) {
				IType pt= i<paramTypes.length ? paramTypes[i] : null;
				setFirstParameter(new PDOMCPPParameter(pdom, this, params[i], pt));
			}
			db.putByte(record + ANNOTATION, PDOMCPPAnnotation.encodeAnnotation(binding));
			IFunctionType ftype = function.getType();
			db.putInt(record + SIGNATURE_MEMENTO, getSignatureMemento(ftype.getParameterTypes()));
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public PDOMCPPFunction(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	public int getSignatureMemento() throws CoreException {
		return pdom.getDB().getInt(record + SIGNATURE_MEMENTO);
	}
	
	public static int getSignatureMemento(PDOM pdom, int record) throws CoreException {
		return pdom.getDB().getInt(record + SIGNATURE_MEMENTO);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPPFUNCTION;
	}
	
	public PDOMCPPParameter getFirstParameter() throws CoreException {
		int rec = pdom.getDB().getInt(record + FIRST_PARAM);
		return rec != 0 ? new PDOMCPPParameter(pdom, rec) : null;
	}

	public void setFirstParameter(PDOMCPPParameter param) throws CoreException {
		if (param != null)
			param.setNextParameter(getFirstParameter());
		int rec = param != null ? param.getRecord() :  0;
		pdom.getDB().putInt(record + FIRST_PARAM, rec);
	}
	
	public boolean isInline() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.INLINE_OFFSET);
	}

	public boolean isMutable() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IScope getFunctionScope() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IParameter[] getParameters() throws DOMException {
		try {
			int n = pdom.getDB().getInt(record + NUM_PARAMS);
			IParameter[] params = new IParameter[n];
			PDOMCPPParameter param = getFirstParameter();
			while (param != null) {
				params[--n] = param;
				param = param.getNextParameter();
			}
			return params;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IParameter[0];
		}
	}

	public IFunctionType getType() throws DOMException {
		return this;
	}

	public boolean isAuto() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.2
		return false; 
	}

	public boolean isExtern() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.EXTERN_OFFSET);
	}

	public boolean isRegister() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.2
		return false; 
	}

	public boolean isStatic() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.STATIC_OFFSET);
	}

	public boolean takesVarArgs() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.VARARGS_OFFSET);
	}

	public IType[] getParameterTypes() throws DOMException {
		try {
			int n = pdom.getDB().getInt(record + NUM_PARAMS);
			IType[] types = new IType[n];
			PDOMCPPParameter param = getFirstParameter();
			while (param != null) {
				types[--n] = param.getType();
				param = param.getNextParameter();
			}
			return types;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IType[0];
		}
	}

	public IType getReturnType() throws DOMException {
		try {
			PDOMNode node = getLinkageImpl().getNode(pdom.getDB().getInt(record + RETURN_TYPE));
			if (node instanceof IType) {
				return (IType) node;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public boolean isConst() {
		// ISO/IEC 14882:2003 9.3.1.3
		// Only applicable to member functions
		return false; 
	}

	public boolean isVolatile() {
		// ISO/IEC 14882:2003 9.3.1.3
		// Only applicable to member functions
		return false; 
	}

	public boolean isSameType(IType type) {		
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}

		try {
			if (type instanceof ICPPFunctionType) {
				ICPPFunctionType ft = (ICPPFunctionType) type;
				IType rt1= getReturnType();
				IType rt2= ft.getReturnType();
				if (rt1 != rt2) {
					if (rt1 == null || !rt1.isSameType(rt2)) {
						return false;
					}
				}
	            
				IType[] params1= getParameterTypes();
				IType[] params2= ft.getParameterTypes();
				if( params1.length == 1 && params2.length == 0 ){
					if( !(params1[0] instanceof IBasicType) || ((IBasicType)params1[0]).getType() != IBasicType.t_void )
						return false;
				} else if( params2.length == 1 && params1.length == 0 ){
					if( !(params2[0] instanceof IBasicType) || ((IBasicType)params2[0]).getType() != IBasicType.t_void )
						return false;
				} else if( params1.length != params2.length ){
					return false;
				} else {
					for( int i = 0; i < params1.length; i++ ){
						if (params1[i] == null || ! params1[i].isSameType( params2[i] ) )
							return false;
					}
				}

				if( isConst() != ft.isConst() || isVolatile() != ft.isVolatile() )
					return false;

				return true;
			}
			return false;
		} catch (DOMException e) {
		}
		return false;
	}

	public Object clone() {
		throw new PDOMNotImplementedError();
	}

	public static int getSignatureMemento(IType[] types) throws DOMException {
		if(types.length==1) {
			if(types[0] instanceof IBasicType) {
				if(((IBasicType)types[0]).getType()==IBasicType.t_void) {
					types = new IType[0];
				}
			}
		}
		StringBuffer result = new StringBuffer();
		result.append('(');
		for(int i=0; i<types.length; i++) {
			if (i>0) {
				result.append(',');
			}
			result.append(ASTTypeUtil.getType(types[i]));
		}
		result.append(')');
		return result.toString().hashCode();
	}

	public static int getSignatureMemento(IFunctionType type) throws DOMException {
		IType[] params = type.getParameterTypes();	
		return getSignatureMemento(params);
	}

	public int compareTo(Object other) {
		int cmp = super.compareTo(other);
		if(cmp==0) {
			if(other instanceof PDOMCPPFunction) {
				try {
					PDOMCPPFunction otherFunction = (PDOMCPPFunction) other;
					int mySM = getSignatureMemento();
					int otherSM = otherFunction.getSignatureMemento();
					return mySM == otherSM ? 0 : mySM < otherSM ? -1 : 1;
				} catch(CoreException ce) {
					CCorePlugin.log(ce);
				}
			} else {
				throw new PDOMNotImplementedError();
			}
		}
		return cmp;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		try {
			result.append(getName()+" "+ASTTypeUtil.getParameterTypeString(getType())); //$NON-NLS-1$
		} catch(DOMException de) {
			result.append(de);
		}
		return result.toString();
	}
}
