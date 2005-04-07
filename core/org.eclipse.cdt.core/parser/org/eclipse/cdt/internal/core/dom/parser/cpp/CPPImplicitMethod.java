/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jan 19, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * @author aniefer
 */
public class CPPImplicitMethod extends CPPMethod {

    private char [] implicitName = null;
    private IParameter [] parameters = null;
    private IType returnType = null;
    private ICPPClassScope scope = null;
    
    public CPPImplicitMethod( ICPPClassScope scope, char[] name, IType returnType, IParameter[] params ) {
        super( null );
        this.implicitName = name;
        this.parameters = params;
        this.returnType = returnType;
        this.scope = scope;
    }
   
	/**
     * @param cs
     * @param ps
     */
 
    public IParameter [] getParameters() {
        return parameters;
    }
    
    public IFunctionType getType() {
    	if( type == null ){
    		IASTDeclaration primary = null;
			try {
				primary = getPrimaryDeclaration();
			} catch (DOMException e) {
			}
			if( primary != null ){
				type = super.getType();
			} else {
				type = CPPVisitor.createImplicitFunctionType( returnType, parameters );
			}
    	}
    	
        return type;
    }

	public String getName() {
        return String.valueOf( implicitName );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
        return implicitName;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return scope;
	}
	
	public IBinding resolveParameter( IASTParameterDeclaration param ){
    	IASTName name = param.getDeclarator().getName();
    	IParameter binding = (IParameter) name.getBinding();
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
    	binding = parameters[i];
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
		if( parameters != null ){
			IASTParameterDeclaration [] nps = fdtor.getParameters();
			if( nps.length != parameters.length )
			    return;

			for( int i = 0; i < nps.length; i++ ){
			    IASTName name = nps[i].getDeclarator().getName(); 
			    name.setBinding( parameters[i] );
			    ((CPPParameter)parameters[i]).addDeclaration( name );
			}
		}
	}
	
	public IASTDeclaration getPrimaryDeclaration() throws DOMException{
		//first check if we already know it
		if( declarations != null ){
			for( int i = 0; i < declarations.length; i++ ){
				IASTDeclaration decl = (IASTDeclaration) declarations[i].getParent();
				if( decl.getParent() instanceof ICPPASTCompositeTypeSpecifier )
					return decl;
			}
		}
		
		IFunctionType ftype = CPPVisitor.createImplicitFunctionType( returnType, parameters );
		IType [] params = ftype.getParameterTypes();
		
		ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) scope.getPhysicalNode();
		IASTDeclaration [] members = compSpec.getMembers();
		for( int i = 0; i < members.length; i++ ){
			IASTDeclarator dtor = null;
			IASTDeclarator [] ds = null;
			int di = -1;
			
			if( members[i] instanceof IASTSimpleDeclaration ){
				ds = ((IASTSimpleDeclaration)members[i]).getDeclarators();
			} else if( members[i] instanceof IASTFunctionDefinition ){
				dtor = ((IASTFunctionDefinition) members[i]).getDeclarator();
			}
			if( ds != null && ds.length > 0 ){
				di = 0;
				dtor = ds[0];
			}
			
			while( dtor != null ){
				IASTName name = dtor.getName();
				if( dtor instanceof ICPPASTFunctionDeclarator &&
					CharArrayUtils.equals( name.toCharArray(), implicitName ) )
				{
					IFunctionType t = (IFunctionType) CPPVisitor.createType( dtor );
					IType [] ps = t.getParameterTypes();
					if( ps.length == params.length ){
						int idx = 0;
						for( ; idx < ps.length && ps[idx] != null; idx++ ){
							if( !ps[idx].equals(params[idx]) )
								break;
						}
						if( idx == ps.length ){
							name.setBinding( this );
							addDeclaration( dtor );
							return members[i];
						}
							
					}
				}
				dtor = ( di > -1 && ++ di < ds.length ) ? ds[di] : null;
			}
		}
		return null;
	}
}
