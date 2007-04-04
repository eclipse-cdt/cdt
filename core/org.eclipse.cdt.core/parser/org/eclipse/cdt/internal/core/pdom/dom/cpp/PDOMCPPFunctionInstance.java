/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPFunctionInstance extends PDOMCPPInstance implements IIndexType,
		ICPPFunction, ICPPFunctionType {

	/**
	 * Offset of total number of function parameters (relative to the
	 * beginning of the record).
	 */
	private static final int NUM_PARAMS = PDOMCPPInstance.RECORD_SIZE + 0;

	/**
	 * Offset of pointer to the first parameter of this function (relative to
	 * the beginning of the record).
	 */
	private static final int FIRST_PARAM = PDOMCPPInstance.RECORD_SIZE + 4;

	/**
	 * Offset for return type of this function (relative to
	 * the beginning of the record).
	 */
	private static final int RETURN_TYPE = PDOMCPPInstance.RECORD_SIZE + 8;
	
	/**
	 * The size in bytes of a PDOMCPPFunctionInstance record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPInstance.RECORD_SIZE + 12;
	
	public PDOMCPPFunctionInstance(PDOM pdom, PDOMNode parent, ICPPFunction function, PDOMBinding instantiated)
			throws CoreException {
		super(pdom, parent, (ICPPTemplateInstance) function, instantiated);
		
		Database db = pdom.getDB();
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
			
			ICPPFunction sFunc= (ICPPFunction) ((ICPPSpecialization)function).getSpecializedBinding();
			IParameter[] sParams= sFunc.getParameters();
			IType[] sParamTypes= sFunc.getType().getParameterTypes();
			
			for (int i=0; i<params.length; ++i) {
				IType pt= i<paramTypes.length ? paramTypes[i] : null;
				//TODO shouldn't need to make new parameter (find old one)
				PDOMCPPParameter sParam = new PDOMCPPParameter(pdom, this, sParams[i], sParamTypes[i]);
				setFirstParameter(new PDOMCPPParameterSpecialization(pdom, this, (ICPPParameter) params[i], sParam, pt));
			}
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public PDOMCPPFunctionInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_FUNCTION_INSTANCE;
	}
	
	public PDOMCPPParameterSpecialization getFirstParameter() throws CoreException {
		int rec = pdom.getDB().getInt(record + FIRST_PARAM);
		return rec != 0 ? new PDOMCPPParameterSpecialization(pdom, rec) : null;
	}

	public void setFirstParameter(PDOMCPPParameterSpecialization param) throws CoreException {
		if (param != null)
			param.setNextParameter(getFirstParameter());
		int rec = param != null ? param.getRecord() :  0;
		pdom.getDB().putInt(record + FIRST_PARAM, rec);
	}
	
	public IParameter[] getParameters() throws DOMException {
		try {
			int n = pdom.getDB().getInt(record + NUM_PARAMS);
			IParameter[] params = new IParameter[n];
			PDOMCPPParameterSpecialization param = getFirstParameter();
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
	
	public boolean isInline() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isInline();
	}

	public boolean isMutable() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isMutable();
	}

	public boolean isAuto() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isAuto();
	}

	public boolean isExtern() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isExtern();
	}

	public boolean isRegister() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isRegister();
	}

	public boolean isStatic() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).isStatic();
	}

	public boolean takesVarArgs() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).takesVarArgs();
	}
	
	public IScope getFunctionScope() throws DOMException { fail(); return null; }

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

	public IType[] getParameterTypes() throws DOMException {
		try {
			int n = pdom.getDB().getInt(record + NUM_PARAMS);
			IType[] types = new IType[n];
			PDOMCPPParameterSpecialization param = getFirstParameter();
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
	
	public Object clone() {fail();return null;}
}
