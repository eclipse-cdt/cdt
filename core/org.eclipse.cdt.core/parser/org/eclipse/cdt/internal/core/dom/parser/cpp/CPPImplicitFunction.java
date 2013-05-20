/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

/**
 * The CPPImplicitFunction is used to represent implicit functions that exist on the translation
 * unit but are not actually part of the physical AST created by CDT.
 * 
 * An example is GCC built-in functions.
 */
public class CPPImplicitFunction extends CPPFunction {

	private ICPPParameter[] parms=null;
	private IScope scope=null;
    private ICPPFunctionType functionType=null;
	private final boolean takesVarArgs;
	private boolean isDeleted;
	private final char[] name;
	
	public CPPImplicitFunction(char[] name, IScope scope, ICPPFunctionType type, ICPPParameter[] parms, boolean takesVarArgs) {
        super( null );
        this.name=name;
		this.scope=scope;
		this.functionType= type;
		this.parms=parms;
		this.takesVarArgs=takesVarArgs;
	}

    @Override
	public ICPPParameter [] getParameters() {
        return parms;
    }
    
    @Override
	public ICPPFunctionType getType() {
    	return functionType;
    }
    
    @Override
	public String getName() {
        return String.valueOf( name );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    @Override
	public char[] getNameCharArray() {
        return name;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    @Override
	public IScope getScope() {
        return scope;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
     */
    @Override
	public IScope getFunctionScope() {
        return null;
    }
    
    @Override
	public boolean takesVarArgs() {
        return takesVarArgs;
    }
    
    @Override
	public boolean isDeleted() {
    	return isDeleted;
    }
    
    @Override
	public IBinding getOwner() {
    	return null;
    }
    
    public void setDeleted(boolean val) {
    	isDeleted= val;
    }	
}
