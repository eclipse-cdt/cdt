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
 * Created on Dec 1, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPFunction implements IFunction, ICPPBinding {
    
    public static class CPPFunctionProblem extends ProblemBinding implements IFunction {

        /**
         * @param id
         * @param arg
         */
        public CPPFunctionProblem( int id, char[] arg ) {
            super( id, arg );
        }

        public IParameter[] getParameters() throws DOMException {
            throw new DOMException( this );
        }

        public IScope getFunctionScope() throws DOMException {
            throw new DOMException( this );
        }

        public IFunctionType getType() throws DOMException {
            throw new DOMException( this );
        }
    }
    
	protected ICPPASTFunctionDeclarator [] declarations;
	protected ICPPASTFunctionDeclarator definition;
	protected IFunctionType type = null;
	
	public CPPFunction( ICPPASTFunctionDeclarator declarator ){
	    if( declarator != null ) {
			IASTNode parent = declarator.getParent();
			if( parent instanceof IASTFunctionDefinition )
				definition = declarator;
			else
				declarations = new ICPPASTFunctionDeclarator [] { declarator };
	    }
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return declarations;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        return definition;
    }
    
	public void addDefinition( ICPPASTFunctionDeclarator dtor ){
		updateParameterBindings( dtor );
		definition = dtor;
	}
	public void addDeclaration( ICPPASTFunctionDeclarator dtor ){
		updateParameterBindings( dtor );
		if( declarations == null ){
			declarations = new ICPPASTFunctionDeclarator [] { dtor };
			return;
		}
		for( int i = 0; i < declarations.length; i++ ){
			if( declarations[i] == null ){
				declarations[i] = dtor;
				updateParameterBindings( dtor );
				return;
			}
		}
		ICPPASTFunctionDeclarator [] tmp = new ICPPASTFunctionDeclarator[ declarations.length * 2 ];
		System.arraycopy( declarations, 0, tmp, 0, declarations.length );
		tmp[ declarations.length ] = dtor;
		declarations = tmp;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getParameters()
	 */
	public IParameter [] getParameters() {
	    IASTStandardFunctionDeclarator dtor = ( definition != null ) ? definition : declarations[0];
		IASTParameterDeclaration[] params = dtor.getParameters();
		int size = params.length;
		IParameter [] result = new IParameter[ size ];
		if( size > 0 ){
			for( int i = 0; i < size; i++ ){
				IASTParameterDeclaration p = params[i];
				result[i] = (IParameter) p.getDeclarator().getName().resolveBinding();
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
	 */
	public IScope getFunctionScope() {
	    if( definition != null ){
			IASTFunctionDefinition def = (IASTFunctionDefinition) definition.getParent();
			return def.getScope();
	    }
	    return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return (definition != null ) ? definition.getName().toString() : declarations[0].getName().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return (definition != null ) ? definition.getName().toCharArray() : declarations[0].getName().toCharArray();	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
	    ICPPASTDeclSpecifier declSpec = null;
	    if( definition != null ){
	    	IASTNode node = definition.getParent();
	    	while( node instanceof IASTDeclarator )
	    		node = node.getParent();
	        IASTFunctionDefinition def = (IASTFunctionDefinition) node;
		    declSpec = (ICPPASTDeclSpecifier) def.getDeclSpecifier();    
	    } else {
	    	IASTNode node = declarations[0].getParent();
	    	while( node instanceof IASTDeclarator )
	    		node = node.getParent();
	        IASTSimpleDeclaration decl = (IASTSimpleDeclaration)node; 
	        declSpec = (ICPPASTDeclSpecifier) decl.getDeclSpecifier();
	    }	

	    IScope scope = CPPVisitor.getContainingScope( definition != null ? definition : declarations[0] );
	    if( declSpec.isFriend() && scope instanceof ICPPClassScope ){
	        try {
                while( scope instanceof ICPPClassScope ){
	                scope = scope.getParent();
                }
	        } catch ( DOMException e ) {
            }
	    }
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
	 */
	public IASTNode getPhysicalNode() {
	    if( definition != null )
	        return definition;
	    else if( declarations != null && declarations.length > 0 )
	        return declarations[0];
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#getType()
     */
    public IFunctionType getType() {
        if( type == null )
            type = (IFunctionType) CPPVisitor.createType( ( definition != null ) ? definition : declarations[0] );
        return type;
    }

    public IBinding resolveParameter( IASTParameterDeclaration param ){
    	IASTName name = param.getDeclarator().getName();
    	IBinding binding = ((CPPASTName)name).getBinding();
    	if( binding != null )
    		return binding;
		
    	IASTStandardFunctionDeclarator fdtor = (IASTStandardFunctionDeclarator) param.getParent();
    	IASTParameterDeclaration [] ps = fdtor.getParameters();
    	int i = 0;
    	for( ; i < ps.length; i++ ){
    		if( param == ps[i] )
    			break;
    	}
    	
    	//create a new binding and set it for the corresponding parameter in all known defns and decls
    	binding = new CPPParameter( name );
    	IASTParameterDeclaration temp = null;
    	if( definition != null ){
    		temp = definition.getParameters()[i];
    		((CPPASTName)temp.getDeclarator().getName()).setBinding( binding );
    	}
    	if( declarations != null ){
    		for( int j = 0; j < declarations.length && declarations[j] != null; j++ ){
    			temp = declarations[j].getParameters()[i];
        		((CPPASTName)temp.getDeclarator().getName()).setBinding( binding );
    		}
    	}
    	return binding;
    }
    
    protected void updateParameterBindings( ICPPASTFunctionDeclarator fdtor ){
    	ICPPASTFunctionDeclarator orig = (ICPPASTFunctionDeclarator) getPhysicalNode();
    	IASTParameterDeclaration [] ops = orig.getParameters();
    	IASTParameterDeclaration [] nps = fdtor.getParameters();
    	CPPParameter temp = null;
    	for( int i = 0; i < nps.length; i++ ){
    		temp = (CPPParameter) ((CPPASTName)ops[i].getDeclarator().getName()).getBinding();
    		if( temp != null ){
    		    CPPASTName name = (CPPASTName) nps[i].getDeclarator().getName();
    			name.setBinding( temp );
    			temp.addDeclaration( name );
    		}
    	}
    }
}
