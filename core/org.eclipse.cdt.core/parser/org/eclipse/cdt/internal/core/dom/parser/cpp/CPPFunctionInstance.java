/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Mar 29, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */
public class CPPFunctionInstance extends CPPInstance implements ICPPFunction, ICPPInternalBinding {
	private IFunctionType type = null;
	private IParameter [] parameters = null;
	/**
	 * @param scope
	 * @param orig
	 * @param argMap
	 */
	public CPPFunctionInstance(ICPPScope scope, IBinding orig, ObjectMap argMap) {
		super(scope, orig, argMap);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDeclarations()
	 */
	public IASTNode[] getDeclarations() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDefinition()
	 */
	public IASTNode getDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public ICPPDelegate createDelegate(IASTName name) {
		return new CPPFunction.CPPFunctionDelegate( name, this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getParameters()
	 */
	public IParameter[] getParameters() throws DOMException {
		if( parameters == null ){
			IParameter [] params = ((ICPPFunction)getOriginalBinding()).getParameters();
			parameters = new IParameter[ params.length ];
			for (int i = 0; i < params.length; i++) {
				parameters[i] = new CPPParameterInstance( null, params[i], getArgumentMap() );
			}
		}
		
		return parameters;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
	 */
	public IScope getFunctionScope() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getType()
	 */
	public IFunctionType getType() throws DOMException {
		if( type == null ){
			type = (IFunctionType) CPPTemplates.instantiateType( ((ICPPFunction)getOriginalBinding()).getType(), getArgumentMap() );
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#isStatic()
	 */
	public boolean isStatic() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
	 */
	public String[] getQualifiedName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
	 */
	public char[][] getQualifiedNameCharArray() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
	 */
	public boolean isGloballyQualified() {
		// TODO Auto-generated method stub
		return false;
	}
}
