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
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * The CImplicitFunction is used to represent implicit functions that exist on the translation
 * unit but are not actually part of the physical AST created by CDT.
 * 
 * An example is GCC built-in functions.
 * 
 * @author dsteffle
 */
public class CImplicitFunction extends CExternalFunction implements IFunction, ICInternalBinding {

    private IParameter[] parms=null;
    private IScope scope=null;
    private IFunctionType type=null;
    private boolean takesVarArgs=false;
    private char[] name=null;
    
    public CImplicitFunction(char[] name, IScope scope, IFunctionType type, IParameter[] parms, boolean takesVarArgs) {
        super(null, null);
        this.name=name;
        this.scope=scope;
        this.type=type;
        this.parms=parms;
        this.takesVarArgs=takesVarArgs;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#getParameters()
     */
    public IParameter[] getParameters() {
        return parms;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#getType()
     */
    public IFunctionType getType() {
        return type;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#takesVarArgs()
     */
    public boolean takesVarArgs() {
        return takesVarArgs;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        return String.valueOf(name);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return name;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return scope;
    }
    
}
