/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Apr 14, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;

/**
 * @author aniefer
 */
public class CPPDeferredFunctionInstance extends CPPInstance implements	ICPPFunction, ICPPInternalFunction {

	private IType[] arguments;

	/**
	 * @param scope
	 * @param orig
	 * @param argMap
	 */
	public CPPDeferredFunctionInstance( ICPPFunctionTemplate template, IType[] arguments ) {
		super( null, template, null, arguments );
		this.arguments = arguments;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance#getArguments()
	 */
	public IType[] getArguments() {
		return arguments;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getParameters()
	 */
	public IParameter[] getParameters() throws DOMException {
		return ((ICPPFunction)getTemplateDefinition()).getParameters();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
	 */
	public IScope getFunctionScope() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getType()
	 */
	public IFunctionType getType() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#isStatic()
	 */
	public boolean isStatic() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction#isMutable()
	 */
	public boolean isMutable() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction#isInline()
	 */
	public boolean isInline() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#isExtern()
	 */
	public boolean isExtern() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#isAuto()
	 */
	public boolean isAuto() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#isRegister()
	 */
	public boolean isRegister() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#takesVarArgs()
	 */
	public boolean takesVarArgs() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public ICPPDelegate createDelegate(IASTName name) {
		// TODO Auto-generated method stub
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalFunction#isStatic(boolean)
     */
    public boolean isStatic( boolean resolveAll ) {
        // TODO Auto-generated method stub
        return false;
    }
}
