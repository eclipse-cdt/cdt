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

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPClassType implements ICPPClassType, ICPPBinding {
	public static class CPPClassTypeProblem extends ProblemBinding implements ICPPClassType{
        public CPPClassTypeProblem( int id, char[] arg ) {
            super( id, arg );
        }

		public ICPPBase[] getBases() throws DOMException {
			throw new DOMException( this );
		}
		public IField[] getFields() throws DOMException {
			throw new DOMException( this );
		}
		public List getDeclaredFields() throws DOMException {
			throw new DOMException( this );
		}
		public List getMethods() throws DOMException {
			throw new DOMException( this );
		}
		public List getAllDeclaredMethods() throws DOMException {
			throw new DOMException( this );
		}
		public List getDeclaredMethods() throws DOMException {
			throw new DOMException( this );
		}
		public ICPPConstructor[] getConstructors() throws DOMException {
			throw new DOMException( this );
		}
		public int getKey() throws DOMException {
			throw new DOMException( this );
		}
		public IField findField(String name) throws DOMException {
			throw new DOMException( this );
		}
		public IScope getCompositeScope() throws DOMException {
			throw new DOMException( this );
		}
		public IScope getParent() throws DOMException {
			throw new DOMException( this );
		}
		public IBinding[] find(String name) throws DOMException {
			throw new DOMException( this );
		}
        public IBinding[] getFriends() throws DOMException {
			throw new DOMException( this );
        }
    }
	
	private IASTName definition;
	private IASTName [] declarations;
	
	public CPPClassType( IASTName name ){
	    ASTNodeProperty prop = name.getPropertyInParent();
	    if( name instanceof ICPPASTQualifiedName ){
	        IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
	        name = ns[ ns.length - 1 ];
	    }
	    
	    if( prop == IASTCompositeTypeSpecifier.TYPE_NAME )
			definition = name;
		else 
			declarations = new IASTName[] { name };
		((CPPASTName)name).setBinding( this );
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
    
	private class FindDefinitionAction extends CPPASTVisitor {
	    private char [] nameArray = CPPClassType.this.getNameCharArray();
	    public IASTName result = null;
	    
	    {
	        shouldVisitNames          = true;
			shouldVisitDeclarations   = true;
			shouldVisitDeclSpecifiers = true;
			shouldVisitDeclarators    = true;
			shouldVisitNamespaces     = true;
	    }
	    
	    public int visit( IASTName name ){
	        if( name.getParent() instanceof ICPPASTCompositeTypeSpecifier &&
	            CharArrayUtils.equals( name.toCharArray(), nameArray ) ) 
	        {
	            IBinding binding = name.resolveBinding();
	            if( binding == CPPClassType.this ){
	                result = name;
	                return PROCESS_ABORT;
	            }
	        }
	        return PROCESS_CONTINUE; 
	    }
	    
		public int visit( IASTDeclaration declaration ){ 
		    return (declaration instanceof IASTSimpleDeclaration ) ? PROCESS_CONTINUE : PROCESS_SKIP; 
		}
		public int visit( IASTDeclSpecifier declSpec ){
		    return (declSpec instanceof ICPPASTCompositeTypeSpecifier ) ? PROCESS_CONTINUE : PROCESS_SKIP; 
		}
		public int processDeclarators( IASTDeclarator declarator ) 			{ return PROCESS_SKIP; }
	}
	
	private void checkForDefinition(){
		FindDefinitionAction action = new FindDefinitionAction();
		IASTNode node = CPPVisitor.getContainingBlockItem( getPhysicalNode() ).getParent();

		node.accept( action );
	    definition = action.result;
		
		if( definition == null ){
			node.getTranslationUnit().accept( action );
		    definition = action.result;
		}
		
		return;
	}
	
	private ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier(){
	    if( definition != null ){
	        return (ICPPASTCompositeTypeSpecifier) definition.getParent();
	    }
	    return null;
	}
	private ICPPASTElaboratedTypeSpecifier getElaboratedTypeSpecifier() {
	    if( declarations != null )
	        return (ICPPASTElaboratedTypeSpecifier) declarations[0].getParent();
	    return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public IField[] getFields() {
	    if( definition == null ){
	        checkForDefinition();
	        if( definition == null )
	            return new IField [] { new CPPField.CPPFieldProblem( IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
	    }

		IASTDeclaration[] members = getCompositeTypeSpecifier().getMembers();
		int size = members.length;
		IField[] fields = null;
		if( size > 0 ){

			for( int i = 0; i < size; i++ ){
				IASTNode node = members[i];
				if( node instanceof IASTSimpleDeclaration ){
					IASTDeclarator[] declarators = ((IASTSimpleDeclaration)node).getDeclarators();
					for( int j = 0; j < declarators.length; j++ ){
						IASTDeclarator declarator = declarators[i];
						IBinding binding = declarator.getName().resolveBinding();
						if( binding != null && binding instanceof IField )
							fields = (IField[]) ArrayUtil.append( IField.class, fields, binding );
					}
				}
			}
			
		}
		return (IField[]) ArrayUtil.trim( IField.class, fields );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#findField(java.lang.String)
	 */
	public IField findField(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return ( definition != null ) ? definition.toString() : declarations[0].toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return ( definition != null ) ? definition.toCharArray() : declarations[0].toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		IASTName name = definition != null ? definition : declarations[0];

		IScope scope = CPPVisitor.getContainingScope( name );
		if( definition == null && name.getPropertyInParent() != ICPPASTQualifiedName.SEGMENT_NAME ){
		    IASTNode node = declarations[0].getParent().getParent();
		    if( node instanceof IASTFunctionDefinition || node instanceof IASTParameterDeclaration ||
		        ( node instanceof IASTSimpleDeclaration && 
		          ( ((IASTSimpleDeclaration) node).getDeclarators().length > 0 || getElaboratedTypeSpecifier().isFriend() ) ) )
		    {
	            while( scope instanceof ICPPClassScope || scope instanceof ICPPFunctionScope ){
					try {
						scope = (ICPPScope) scope.getParent();
					} catch (DOMException e1) {
					}
				}
		    }
		}
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() {
		return (definition != null ) ? getCompositeTypeSpecifier().getScope() : null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
	 */
	public IASTNode getPhysicalNode() {
		return (definition != null ) ? (IASTNode) definition : declarations[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
	 */
	public int getKey() {
	    if( definition != null )
	        return getCompositeTypeSpecifier().getKey();
	    
		return getElaboratedTypeSpecifier().getKind();
	}

	public void addDefinition( ICPPASTCompositeTypeSpecifier compSpec ){
		definition = compSpec.getName();
	}
	public void addDeclaration( ICPPASTElaboratedTypeSpecifier elabSpec ) {
		if( declarations == null ){
			declarations = new IASTName[] { elabSpec.getName() };
			return;
		}

        for( int i = 0; i < declarations.length; i++ ){
            if( declarations[i] == null ){
                declarations[i] = elabSpec.getName();
                return;
            }
        }
        IASTName tmp [] = new IASTName[ declarations.length * 2 ];
        System.arraycopy( declarations, 0, tmp, 0, declarations.length );
        tmp[ declarations.length ] = elabSpec.getName();
        declarations = tmp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	public ICPPBase [] getBases() {
		if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                return new ICPPBase [] { new CPPBaseClause.CPPBaseProblem( IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
		ICPPASTBaseSpecifier [] bases = getCompositeTypeSpecifier().getBaseSpecifiers();
		if( bases.length == 0 )
		    return ICPPBase.EMPTY_BASE_ARRAY;
		
		ICPPBase [] bindings = new ICPPBase[ bases.length ];
		for( int i = 0; i < bases.length; i++ ){
		    bindings[i] = new CPPBaseClause( bases[i] );
		}
		
		return bindings; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredFields()
	 */
	public List getDeclaredFields() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getMethods()
	 */
	public List getMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getAllDeclaredMethods()
	 */
	public List getAllDeclaredMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredMethods()
	 */
	public List getDeclaredMethods() {
		// TODO Auto-generated method stub
		return null;
	}
	
    public Object clone(){
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch ( CloneNotSupportedException e ) {
            //not going to happen
        }
        return t;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getConstructors()
     */
    public ICPPConstructor[] getConstructors() throws DOMException {
        if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                return new ICPPConstructor [] { new CPPConstructor.CPPConstructorProblem( IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
        
        ICPPClassScope scope = (ICPPClassScope) getCompositeScope();
        if( scope.isFullyCached() )
        	return ((CPPClassScope)scope).getConstructors( true );
        	
        IASTDeclaration [] members = getCompositeTypeSpecifier().getMembers();
        for( int i = 0; i < members.length; i++ ){
			if( members[i] instanceof IASTSimpleDeclaration ){
			    IASTDeclarator [] dtors = ((IASTSimpleDeclaration)members[i]).getDeclarators();
			    for( int j = 0; j < dtors.length; j++ ){
			        if( dtors[j] == null ) break;
		            scope.addName( dtors[j].getName() );
			    }
			} else if( members[i] instanceof IASTFunctionDefinition ){
			    IASTDeclarator dtor = ((IASTFunctionDefinition)members[i]).getDeclarator();
			    scope.addName( dtor.getName() );
			}
        }
        
        return ((CPPClassScope)scope).getConstructors( true );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getFriends()
     */
    public IBinding[] getFriends() {
        if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                return new IBinding [] { new ProblemBinding( IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
        ObjectSet resultSet = new ObjectSet(2);
        IASTDeclaration [] members = getCompositeTypeSpecifier().getMembers();
        for( int i = 0; i < members.length; i++ ){
			if( members[i] instanceof IASTSimpleDeclaration ){
			    ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)members[i]).getDeclSpecifier();
			    if( declSpec.isFriend() ){
			        IASTDeclarator [] dtors = ((IASTSimpleDeclaration)members[i]).getDeclarators();
			        if( declSpec instanceof ICPPASTElaboratedTypeSpecifier && dtors.length == 0 ){
			        	resultSet.put( ((ICPPASTElaboratedTypeSpecifier)declSpec).getName().resolveBinding() );
			        } else {
					    for( int j = 0; j < dtors.length; j++ ){
					        if( dtors[j] == null ) break;
					        resultSet.put( dtors[j].getName().resolveBinding() );
					    }    
			        }
			    }
			} else if( members[i] instanceof IASTFunctionDefinition ){
			    ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)members[i]).getDeclSpecifier();
			    if( declSpec.isFriend() ){
			        IASTDeclarator dtor = ((IASTFunctionDefinition)members[i]).getDeclarator();
			        resultSet.put( dtor.getName().resolveBinding() );
			    }
			    
			}
        }
        
        return (IBinding[]) ArrayUtil.trim( IBinding.class, resultSet.keyArray(), true );
    }
}
