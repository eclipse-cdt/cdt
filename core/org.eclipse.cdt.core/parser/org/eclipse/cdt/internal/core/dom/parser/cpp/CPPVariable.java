/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPVariable implements ICPPVariable, ICPPInternalBinding {
    public static class CPPVariableDelegate extends CPPDelegate implements ICPPVariable {
        public CPPVariableDelegate( IASTName name, ICPPVariable binding ) {
            super( name, binding );
        }
        public IType getType() throws DOMException {
            return ((ICPPVariable)getBinding()).getType();
        }
        public boolean isStatic() throws DOMException {
            return ((ICPPVariable)getBinding()).isStatic();
        }
        public boolean isMutable() throws DOMException {
            return ((ICPPVariable)getBinding()).isMutable();
        }
        public boolean isExtern() throws DOMException {
            return ((ICPPVariable)getBinding()).isExtern();
        }
        public boolean isAuto() throws DOMException {
            return ((ICPPVariable)getBinding()).isAuto();
        }
        public boolean isRegister() throws DOMException {
            return ((ICPPVariable)getBinding()).isRegister();
        }
    }
    public static class CPPVariableProblem extends ProblemBinding implements ICPPVariable{
        public CPPVariableProblem( IASTNode node, int id, char[] arg ) {
            super( node, id, arg );
        }

        public IType getType() throws DOMException {
            throw new DOMException( this );
        }

        public boolean isStatic() throws DOMException {
            throw new DOMException( this );
        }
        public String[] getQualifiedName() throws DOMException {
            throw new DOMException( this );
        }
        public char[][] getQualifiedNameCharArray() throws DOMException {
            throw new DOMException( this );
        }
        public boolean isGloballyQualified() throws DOMException {
            throw new DOMException( this );
        }
        public boolean isMutable() throws DOMException {
            throw new DOMException( this );
        }
        public boolean isExtern() throws DOMException {
             throw new DOMException( this );
        }
        public boolean isAuto() throws DOMException {
            throw new DOMException( this );
        }
        public boolean isRegister() throws DOMException {
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
	    name.setBinding( this );
	}
	
	protected boolean isDefinition( IASTName name ){
	    IASTNode node = name.getParent();
	    if( node instanceof ICPPASTQualifiedName )
	        node = node.getParent();
	    
	    if( !( node instanceof IASTDeclarator ) )
	        return false;
	    
	    IASTDeclarator dtor = (IASTDeclarator) node;
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
	
	public void addDeclaration( IASTNode node ) {
		if( !(node instanceof IASTName) )
			return;
		IASTName name = (IASTName) node;
	    if( isDefinition( name ) )
	        definition = name;
	    else if( declarations == null )
	        declarations = new IASTName[] { name };
	    else {
	        //keep the lowest offset declaration in [0]
			if( declarations.length > 0 && ((ASTNode)node).getOffset() < ((ASTNode)declarations[0]).getOffset() ){
				declarations = (IASTName[]) ArrayUtil.prepend( IASTName.class, declarations, name );
			} else {
				declarations = (IASTName[]) ArrayUtil.append( IASTName.class, declarations, name );
			}
	    }
	}
	
	public void removeDeclaration(IASTNode node) {
		if( node == definition ){
			definition = null;
			return;
		}
		if( declarations != null ) {
			for (int i = 0; i < declarations.length; i++) {
				if( node == declarations[i] ) {
					if( i == declarations.length - 1 )
						declarations[i] = null;
					else
						System.arraycopy( declarations, i + 1, declarations, i, declarations.length - 1 - i );
				}
			}
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
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedName()
     */
    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while( scope != null ){
            if( scope instanceof ICPPBlockScope )
                return false;
            scope = scope.getParent();
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public ICPPDelegate createDelegate( IASTName name ) {
        return new CPPVariableDelegate( name, this );
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDefinition(IASTNode node) {
		addDeclaration( node );
	}

	public boolean hasStorageClass( int storage ){
	    IASTName name = (IASTName) getDefinition();
        IASTNode[] ns = getDeclarations();
        int i = -1;
        do{
            if( name != null ){
                IASTNode parent = name.getParent();
	            while( !(parent instanceof IASTDeclaration) )
	                parent = parent.getParent();
	            
	            if( parent instanceof IASTSimpleDeclaration ){
	                IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	                if( declSpec.getStorageClass() == storage )
	                    return true;
	            }
            }
            if( ns != null && ++i < ns.length )
                name = (IASTName) ns[i];
            else
                break;
        } while( name != null );
        return false;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable#isMutable()
     */
    public boolean isMutable() {
        //7.1.1-8 the mutable specifier can only be applied to names of class data members
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isExtern()
     */
    public boolean isExtern() {
        return hasStorageClass( IASTDeclSpecifier.sc_extern );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isAuto()
     */
    public boolean isAuto() {
        return hasStorageClass( IASTDeclSpecifier.sc_auto );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isRegister()
     */
    public boolean isRegister() {
        return hasStorageClass( IASTDeclSpecifier.sc_register );
    }
}
