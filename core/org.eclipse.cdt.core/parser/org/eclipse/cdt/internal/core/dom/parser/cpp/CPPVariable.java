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
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPVariable implements IVariable, ICPPBinding {
    public static class CPPVariableProblem extends ProblemBinding implements IVariable{
        public CPPVariableProblem( int id, char[] arg ) {
            super( id, arg );
        }

        public IType getType() throws DOMException {
            throw new DOMException( this );
        }

        public boolean isStatic() throws DOMException {
            throw new DOMException( this );
        }
    }
	private IASTName declarations[] = null;
	private IASTName definition = null;
	private IType type = null;
	
	public CPPVariable( IASTName name ){
	    boolean isDef = isDefinition( name );
	    if( name instanceof ICPPASTQualifiedName ){
	        IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
	        name = ns[ ns.length - 1 ];
	    }
	    
	    if( isDef )
	        definition = name;
	    else 
	        declarations = new IASTName [] { name };
	    ((CPPASTName)name).setBinding( this );
	}
	
	protected boolean isDefinition( IASTName name ){
	    IASTNode node = name.getParent();
	    if( node instanceof ICPPASTQualifiedName )
	        node = node.getParent();
	    
	    if( !( node instanceof IASTDeclarator ) )
	        return false;
	    
	    IASTDeclarator dtor = (IASTDeclarator) name.getParent();
	    while( dtor.getParent() instanceof IASTDeclarator )
	        dtor = (IASTDeclarator) dtor.getParent();
	    
	    IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) dtor.getParent();
	    IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();
	    
	    //(3.1-1) A declaration is a definition unless ...
	    //it contains the extern specifier or a linkage-spec and does not contain an initializer
	    if( dtor.getInitializer() == null && declSpec.getStorageClass() == IASTDeclSpecifier.sc_extern )
	        return false;
	    //or it declares a static data member in a class declaration
	    if( simpleDecl.getParent() instanceof ICPPASTCompositeTypeSpecifier && 
	        declSpec.getStorageClass() == IASTDeclSpecifier.sc_static )
	    {
	        return false;
	    }
	    
	    return true;
	}
	
	public void addDeclaration( IASTName name ) {
	    if( isDefinition( name ) )
	        definition = name;
	    else 
	        declarations = (IASTName[]) ArrayUtil.append( IASTName.class, declarations, name );
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	public IType getType() {
		if( type == null ){
		    IASTDeclarator dtor = (IASTDeclarator) ( (definition != null) ? definition.getParent() : declarations[0].getParent() );
		    type = CPPVisitor.createType( dtor );
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
	    if( declarations != null ){
	        return declarations[0].toString();
	    } 
        IASTName name = definition;
        if( name instanceof ICPPASTQualifiedName ){
            IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
            name = ns[ ns.length - 1 ];
        }
	        
	    return name.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
	    if( declarations != null ){
	        return declarations[0].toCharArray();
	    } 
        IASTName name = definition;
        if( name instanceof ICPPASTQualifiedName ){
            IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
            name = ns[ ns.length - 1 ];
        }
	        
	    return name.toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CPPVisitor.getContainingScope( definition != null ? definition : declarations[0] );
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPMember#isStatic()
     */
    public boolean isStatic() {
        IASTDeclarator dtor = null;
        if( declarations != null )
            dtor = (IASTDeclarator) declarations[0].getParent();
        else {
            //definition of a static field doesn't necessarily say static
            if( definition instanceof ICPPASTQualifiedName )
                return true;
            dtor = (IASTDeclarator) definition.getParent();
        }

        while( dtor.getPropertyInParent() == IASTDeclarator.NESTED_DECLARATOR )
            dtor = (IASTDeclarator) dtor.getParent();
        
        IASTNode node = dtor.getParent();
        if( node instanceof IASTSimpleDeclaration ){
            IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration)node).getDeclSpecifier();
            return (declSpec.getStorageClass() == IASTDeclSpecifier.sc_static );
        }
        return false;
    }
}
