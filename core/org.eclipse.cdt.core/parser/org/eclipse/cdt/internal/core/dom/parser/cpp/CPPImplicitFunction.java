/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;

/**
 * The CPPImplicitFunction is used to represent implicit functions that exist on the translation
 * unit but are not actually part of the physical AST created by CDT.
 * 
 * An example is GCC built-in functions.
 * 
 * @author dsteffle
 */
public class CPPImplicitFunction extends CPPFunction implements ICPPFunction, ICPPInternalBinding {

	private IParameter[] parms=null;
	private IScope scope=null;
	private IType returnType=null;
    private IFunctionType functionType=null;
	private boolean takesVarArgs=false;
	private char[] name=null;
	
	public CPPImplicitFunction(char[] name, IScope scope, IType type, IParameter[] parms, boolean takesVarArgs) {
        super( null );
        this.name=name;
		this.scope=scope;
		this.returnType=type;
		this.parms=parms;
		this.takesVarArgs=takesVarArgs;
	}

    public IParameter [] getParameters() {
        return parms;
    }
    
    public IFunctionType getType() {
        if( functionType == null ){
            ICPPASTFunctionDeclarator primary = getPrimaryDeclaration();
            
            if( primary != null ){
                functionType = super.getType();
            } else {
                functionType = CPPVisitor.createImplicitFunctionType( returnType, parms );
            }
        }
        
        return functionType;
    }
    
    private ICPPASTFunctionDeclarator getPrimaryDeclaration() {
        if (definition != null)
            return definition;
        else if (declarations != null && declarations.length > 0)
            return declarations[0];
            
        return null;
    }

    public String getName() {
        return String.valueOf( name );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return name;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return scope;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
     */
    public IScope getFunctionScope() {
        return null;
    }
    
    public IBinding resolveParameter( IASTParameterDeclaration param ){
        IASTName aName = param.getDeclarator().getName();
        IParameter binding = (IParameter) aName.getBinding();
        if( binding != null )
            return binding;
        
        //get the index in the parameter list
        ICPPASTFunctionDeclarator fdtor = (ICPPASTFunctionDeclarator) param.getParent();
        IASTParameterDeclaration [] ps = fdtor.getParameters();
        int i = 0;
        for( ; i < ps.length; i++ ){
            if( param == ps[i] )
                break;
        }
        
        //set the binding for the corresponding parameter in all known defns and decls
        binding = parms[i];
        IASTParameterDeclaration temp = null;
        if( definition != null ){
            temp = definition.getParameters()[i];
            IASTName n = temp.getDeclarator().getName();
            n.setBinding( binding );
            ((CPPParameter)binding).addDeclaration( n );
        }
        if( declarations != null ){
            for( int j = 0; j < declarations.length; j++ ){
                temp = declarations[j].getParameters()[i];
                IASTName n = temp.getDeclarator().getName();
                n.setBinding( binding );
                ((CPPParameter)binding).addDeclaration( n );
            }
        }
        return binding;
    }
   
    protected void updateParameterBindings( ICPPASTFunctionDeclarator fdtor ){
        if( parms != null ){
            IASTParameterDeclaration [] nps = fdtor.getParameters();
            if( nps.length != parms.length )
                return;

            for( int i = 0; i < nps.length; i++ ){
                IASTName aName = nps[i].getDeclarator().getName(); 
                aName.setBinding( parms[i] );
				if( parms[i] instanceof ICPPInternalBinding )
					((ICPPInternalBinding)parms[i]).addDeclaration( aName );
            }
        }
    }

    public boolean takesVarArgs() {
        return takesVarArgs;
    }
}
