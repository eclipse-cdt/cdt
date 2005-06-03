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
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPClassType implements ICPPClassType, ICPPInternalClassType {
    public static class CPPClassTypeDelegate extends CPPDelegate implements ICPPClassType, ICPPInternalClassType {
        public CPPClassTypeDelegate( IASTName name, ICPPClassType cls ){
            super( name, cls );
        }
        public ICPPBase[] getBases() throws DOMException {
            return ((ICPPClassType)getBinding()).getBases();
        }
        public IField[] getFields() throws DOMException {
            return ((ICPPClassType)getBinding()).getFields();
        }
        public IField findField( String name ) throws DOMException {
            return ((ICPPClassType)getBinding()).findField( name );
        }
        public ICPPField[] getDeclaredFields() throws DOMException {
            return ((ICPPClassType)getBinding()).getDeclaredFields();
        }
        public ICPPMethod[] getMethods() throws DOMException {
            return ((ICPPClassType)getBinding()).getMethods();
        }
        public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
            return ((ICPPClassType)getBinding()).getAllDeclaredMethods();
        }
        public ICPPMethod[] getDeclaredMethods() throws DOMException {
            return ((ICPPClassType)getBinding()).getDeclaredMethods();
        }
        public ICPPConstructor[] getConstructors() throws DOMException {
            return ((ICPPClassType)getBinding()).getConstructors();
        }
        public IBinding[] getFriends() throws DOMException {
            return ((ICPPClassType)getBinding()).getFriends();
        }
        public int getKey() throws DOMException {
            return ((ICPPClassType)getBinding()).getKey();
        }
        public IScope getCompositeScope() throws DOMException {
            return ((ICPPClassType)getBinding()).getCompositeScope();
        }
        public Object clone() {
            CPPClassTypeDelegate d = null;
            try {
                d = (CPPClassTypeDelegate) super.clone();
            } catch ( CloneNotSupportedException e ) {
            }
            return d;
        }
		public ICPPMethod[] getConversionOperators() {
			IBinding binding = getBinding();
			if( binding instanceof ICPPInternalClassType )
				return ((ICPPInternalClassType)binding).getConversionOperators();
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		}
        public boolean isSameType( IType type ) {
            return ((ICPPClassType)getBinding()).isSameType( type );
        }
		public ICPPClassType[] getNestedClasses() throws DOMException {
			return ((ICPPClassType)getBinding()).getNestedClasses();
		}
    }
	public static class CPPClassTypeProblem extends ProblemBinding implements ICPPClassType{
        public CPPClassTypeProblem( IASTNode node, int id, char[] arg ) {
            super( node, id, arg );
        }

		public ICPPBase[] getBases() throws DOMException {
			throw new DOMException( this );
		}
		public IField[] getFields() throws DOMException {
			throw new DOMException( this );
		}
		public ICPPField[] getDeclaredFields() throws DOMException {
			throw new DOMException( this );
		}
		public ICPPMethod[] getMethods() throws DOMException {
			throw new DOMException( this );
		}
		public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
			throw new DOMException( this );
		}
		public ICPPMethod[] getDeclaredMethods() throws DOMException {
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
        public String[] getQualifiedName() throws DOMException {
            throw new DOMException( this );
        }
        public char[][] getQualifiedNameCharArray() throws DOMException {
            throw new DOMException( this );
        }
        public boolean isGloballyQualified() throws DOMException {
            throw new DOMException( this );
        }
		public ICPPClassType[] getNestedClasses() throws DOMException {
			throw new DOMException( this );
		}
    }
	
	private IASTName definition;
	private IASTName [] declarations;
	private boolean checked = false;
	public CPPClassType( IASTName name ){
	    if( name instanceof ICPPASTQualifiedName ){
	        IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
	        name = ns[ ns.length - 1 ];
	    }
	    IASTNode parent = name.getParent();
	    while( parent instanceof IASTName )
	        parent = parent.getParent();
	    
	    if( parent instanceof IASTCompositeTypeSpecifier )
			definition = name;
		else 
			declarations = new IASTName[] { name };
		name.setBinding( this );
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
	    }
	    
	    public int visit( IASTName name ){
			if( name instanceof ICPPASTTemplateId )
				return PROCESS_SKIP;
			if( name instanceof ICPPASTQualifiedName )
				return PROCESS_CONTINUE;
			char [] c = name.toCharArray();

			if( name.getParent() instanceof ICPPASTQualifiedName ){
				IASTName [] ns = ((ICPPASTQualifiedName)name.getParent()).getNames();
				if( ns[ ns.length - 1 ] != name )
					return PROCESS_CONTINUE;
				name = (IASTName) name.getParent();
			}
			
	        if( name.getParent() instanceof ICPPASTCompositeTypeSpecifier &&
	            CharArrayUtils.equals( c, nameArray ) ) 
	        {
	            IBinding binding = name.resolveBinding();
	            if( binding == CPPClassType.this ){
	            	if( name instanceof ICPPASTQualifiedName ){
	            		IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
	            		name = ns[ ns.length - 1 ];
	            	}
	                result = name;
	                return PROCESS_ABORT;
	            }
	        }
	        return PROCESS_CONTINUE; 
	    }
	    
		public int visit( IASTDeclaration declaration ){ 
			if( declaration instanceof IASTSimpleDeclaration || declaration instanceof ICPPASTTemplateDeclaration )
				return PROCESS_CONTINUE;
			return PROCESS_SKIP; 
		}
		public int visit( IASTDeclSpecifier declSpec ){
		    return (declSpec instanceof ICPPASTCompositeTypeSpecifier ) ? PROCESS_CONTINUE : PROCESS_SKIP; 
		}
		public int visit( IASTDeclarator declarator ) 			{ return PROCESS_SKIP; }
	}
	
	private void checkForDefinition(){
		if( !checked ) {
			FindDefinitionAction action = new FindDefinitionAction();
			IASTNode node = CPPVisitor.getContainingBlockItem( getPhysicalNode() ).getParent();
	
			if( node instanceof ICPPASTCompositeTypeSpecifier )
				node = CPPVisitor.getContainingBlockItem( node.getParent() );
			while( node instanceof ICPPASTTemplateDeclaration )
				node = node.getParent();
			node.accept( action );
		    definition = action.result;
			
			if( definition == null ){
				node.getTranslationUnit().accept( action );
			    definition = action.result;
			}
			checked = true;
		}
		return;
	}
	
	private ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier(){
	    if( definition != null ){
	        IASTNode node = definition;
	        while( node instanceof IASTName )
	            node = node.getParent();
	        if( node instanceof ICPPASTCompositeTypeSpecifier )
	            return (ICPPASTCompositeTypeSpecifier)node;
	    }
	    return null;
	}
	private ICPPASTElaboratedTypeSpecifier getElaboratedTypeSpecifier() {
	    if( declarations != null ){
	        IASTNode node = declarations[0];
	        while( node instanceof IASTName )
	            node = node.getParent();
	        if( node instanceof ICPPASTElaboratedTypeSpecifier )
	            return (ICPPASTElaboratedTypeSpecifier)node;
	    }
	    return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public IField[] getFields() throws DOMException {
	    if( definition == null ){
	        checkForDefinition();
	        if( definition == null ){
	            IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
	            return new IField [] { new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
	        }
	    }

		IField[] fields = getDeclaredFields();
		ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
            fields = (IField[]) ArrayUtil.addAll( IField.class, fields, bases[i].getBaseClass().getFields() );
        }
		return (IField[]) ArrayUtil.trim( IField.class, fields );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#findField(java.lang.String)
	 */
	public IField findField(String name) throws DOMException {
		IBinding [] bindings = CPPSemantics.findBindings( getCompositeScope(), name, true );
		IField field = null;
		for ( int i = 0; i < bindings.length; i++ ) {
            if( bindings[i] instanceof IField ){
                if( field == null )
                    field = (IField) bindings[i];
                else {
                    IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                    return new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, name.toCharArray() );
                }
            }
        }
		return field;
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
		if( definition == null ){
			checkForDefinition();
		}
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

	public void addDefinition( IASTNode node ){
		if( node instanceof ICPPASTCompositeTypeSpecifier )
			definition = ((ICPPASTCompositeTypeSpecifier)node).getName();
	}
	public void addDeclaration( IASTNode node ) {
		if( !(node instanceof ICPPASTElaboratedTypeSpecifier) )
			return;
		
		IASTName name = ((ICPPASTElaboratedTypeSpecifier) node).getName();

		if( declarations == null ){
			declarations = new IASTName[] { name };
			return;
		}

		//keep the lowest offset declaration in [0]
		if( declarations.length > 0 && ((ASTNode)node).getOffset() < ((ASTNode)declarations[0]).getOffset() ){
		   declarations = (IASTName[]) ArrayUtil.prepend( IASTName.class, declarations, name );
		} else {
			declarations = (IASTName[]) ArrayUtil.append( IASTName.class, declarations, name );
		}
	}
	
	public void removeDeclaration(IASTNode node) {
		if( definition == node ){
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
					return;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	public ICPPBase [] getBases() {
		if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPBase [] { new CPPBaseClause.CPPBaseProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
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
	public ICPPField[] getDeclaredFields() throws DOMException {
	    if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPField[] { new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
	    IBinding binding = null;
	    ICPPField [] result = null;
	    
	    IASTDeclaration [] decls = getCompositeTypeSpecifier().getMembers();
	    for ( int i = 0; i < decls.length; i++ ) {
            if( decls[i] instanceof IASTSimpleDeclaration ){
                IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decls[i]).getDeclarators();
                for ( int j = 0; j < dtors.length; j++ ) {
                    binding = dtors[j].getName().resolveBinding();
                    if( binding instanceof ICPPField )
                        result = (ICPPField[]) ArrayUtil.append( ICPPField.class, result, binding );
                }
            } else if( decls[i] instanceof ICPPASTUsingDeclaration ){
                IASTName n = ((ICPPASTUsingDeclaration)decls[i]).getName();
                binding = n.resolveBinding();
                if( binding instanceof ICPPUsingDeclaration ){
                    IBinding [] bs = ((ICPPUsingDeclaration)binding).getDelegates();
                    for ( int j = 0; j < bs.length; j++ ) {
                        if( bs[j] instanceof ICPPField )
                            result = (ICPPField[]) ArrayUtil.append( ICPPField.class, result, bs[j] );
                    }
                } else if( binding instanceof ICPPField ) {
                    result = (ICPPField[]) ArrayUtil.append( ICPPField.class, result, binding );
                }
            }
        }
		return (ICPPField[]) ArrayUtil.trim( ICPPField.class, result );
	}
	
	public ICPPMethod[] getConversionOperators() {
		if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPMethod[] { new CPPMethod.CPPMethodProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
	    IBinding binding = null;
	    ICPPMethod [] result = null;
	    
	    IASTDeclaration [] decls = getCompositeTypeSpecifier().getMembers();
	    IASTName name = null;
	    for ( int i = 0; i < decls.length; i++ ) {
            if( decls[i] instanceof IASTSimpleDeclaration ){
                IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decls[i]).getDeclarators();
                for ( int j = 0; j < dtors.length; j++ ) {
                	name = CPPVisitor.getMostNestedDeclarator( dtors[j] ).getName();
                	if( name instanceof ICPPASTConversionName ){
                		binding = name.resolveBinding();
                        if( binding instanceof ICPPMethod)
                            result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );	
                	}
                }
            } else if( decls[i] instanceof IASTFunctionDefinition ){
                IASTDeclarator dtor = ((IASTFunctionDefinition)decls[i]).getDeclarator();
                name = CPPVisitor.getMostNestedDeclarator( dtor ).getName();
                if( name instanceof ICPPASTConversionName ){
                	binding = name.resolveBinding();
                    if( binding instanceof ICPPMethod ){
                        result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
                    }
                }
            } 
        }
	    
	    ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
			ICPPClassType cls;
			try {
				cls = bases[i].getBaseClass();
			} catch (DOMException e) {
				continue;
			}
			if( cls instanceof CPPClassType )
				result = (ICPPMethod[]) ArrayUtil.addAll( ICPPMethod.class, result, ((CPPClassType)cls).getConversionOperators() );
        }
		return (ICPPMethod[]) ArrayUtil.trim( ICPPMethod.class, result );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getMethods()
	 */
	public ICPPMethod[] getMethods() throws DOMException {
		ObjectSet set = new ObjectSet(2);
		ICPPMethod [] ms = getDeclaredMethods();
		set.addAll( ms );
		ICPPClassScope scope = (ICPPClassScope) getCompositeScope();
		set.addAll( scope.getImplicitMethods() );
		ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
			set.addAll( bases[i].getBaseClass().getMethods() );
        }
		return (ICPPMethod[]) ArrayUtil.trim( ICPPMethod.class, set.keyArray(), true );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getAllDeclaredMethods()
	 */
	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		if( definition == null ){
	        checkForDefinition();
	        if( definition == null ){
	            IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
	            return new ICPPMethod [] { new CPPMethod.CPPMethodProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
	        }
	    }

		ICPPMethod[] methods = getDeclaredMethods();
		ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
            methods = (ICPPMethod[]) ArrayUtil.addAll( ICPPMethod.class, methods, bases[i].getBaseClass().getAllDeclaredMethods() );
        }
		return (ICPPMethod[]) ArrayUtil.trim( ICPPMethod.class, methods );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredMethods()
	 */
	public ICPPMethod[] getDeclaredMethods() throws DOMException {
	    if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPMethod[] { new CPPMethod.CPPMethodProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
	    IBinding binding = null;
	    ICPPMethod [] result = null;
	    
	    IASTDeclaration [] decls = getCompositeTypeSpecifier().getMembers();
	    for ( int i = 0; i < decls.length; i++ ) {
			IASTDeclaration decl = decls[i];
			while( decl instanceof ICPPASTTemplateDeclaration )
				decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();
            if( decl instanceof IASTSimpleDeclaration ){
                IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decl).getDeclarators();
                for ( int j = 0; j < dtors.length; j++ ) {
                    binding = dtors[j].getName().resolveBinding();
                    if( binding instanceof ICPPMethod)
                        result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
                }
            } else if( decl instanceof IASTFunctionDefinition ){
                IASTDeclarator dtor = ((IASTFunctionDefinition)decl).getDeclarator();
                dtor = CPPVisitor.getMostNestedDeclarator( dtor );
                binding = dtor.getName().resolveBinding();
                if( binding instanceof ICPPMethod ){
                    result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
                }
            } else if( decl instanceof ICPPASTUsingDeclaration ){
                IASTName n = ((ICPPASTUsingDeclaration)decl).getName();
                binding = n.resolveBinding();
                if( binding instanceof ICPPUsingDeclaration ){
                    IBinding [] bs = ((ICPPUsingDeclaration)binding).getDelegates();
                    for ( int j = 0; j < bs.length; j++ ) {
                        if( bs[j] instanceof ICPPMethod )
                            result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, bs[j] );
                    }
                } else if( binding instanceof ICPPMethod ) {
                    result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
                }
            }
        }
		return (ICPPMethod[]) ArrayUtil.trim( ICPPMethod.class, result );
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
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPConstructor [] { new CPPConstructor.CPPConstructorProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
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
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new IBinding [] { new ProblemBinding( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
        ObjectSet resultSet = new ObjectSet(2);
        IASTDeclaration [] members = getCompositeTypeSpecifier().getMembers();
        for( int i = 0; i < members.length; i++ ){
			IASTDeclaration decl = members[i];
			while( decl instanceof ICPPASTTemplateDeclaration )
				decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();
			
			if( decl instanceof IASTSimpleDeclaration ){
			    ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)decl).getDeclSpecifier();
			    if( declSpec.isFriend() ){
			        IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decl).getDeclarators();
			        if( declSpec instanceof ICPPASTElaboratedTypeSpecifier && dtors.length == 0 ){
			        	resultSet.put( ((ICPPASTElaboratedTypeSpecifier)declSpec).getName().resolveBinding() );
			        } else {
					    for( int j = 0; j < dtors.length; j++ ){
					        if( dtors[j] == null ) break;
					        resultSet.put( dtors[j].getName().resolveBinding() );
					    }    
			        }
			    }
			} else if( decl instanceof IASTFunctionDefinition ){
			    ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTFunctionDefinition)decl).getDeclSpecifier();
			    if( declSpec.isFriend() ){
			        IASTDeclarator dtor = ((IASTFunctionDefinition)decl).getDeclarator();
			        resultSet.put( dtor.getName().resolveBinding() );
			    }
			    
			}
        }
        
        return (IBinding[]) ArrayUtil.trim( IBinding.class, resultSet.keyArray(), true );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
     */
    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
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
        return new CPPClassTypeDelegate( name, this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    public boolean isSameType( IType type ) {
        if( type == this )
            return true;
        if( type instanceof ITypedef )
            return ((ITypedef)type).isSameType( this );
        return false;
    }

	public ICPPClassType[] getNestedClasses() {
		if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPClassType[] { new CPPClassTypeProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
	    IBinding binding = null;
	    ICPPClassType [] result = null;
	    
	    IASTDeclaration [] decls = getCompositeTypeSpecifier().getMembers();
	    for ( int i = 0; i < decls.length; i++ ) {
			IASTDeclaration decl = decls[i];
			while( decl instanceof ICPPASTTemplateDeclaration )
				decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();
            if( decls[i] instanceof IASTSimpleDeclaration ){
				IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) decls[i]).getDeclSpecifier();
				if( declSpec instanceof ICPPASTCompositeTypeSpecifier ){
					binding = ((ICPPASTCompositeTypeSpecifier)declSpec).getName().resolveBinding();
				} else if( declSpec instanceof ICPPASTElaboratedTypeSpecifier &&
						   ((IASTSimpleDeclaration)decls[i]).getDeclarators().length == 0 )
				{
					binding = ((ICPPASTElaboratedTypeSpecifier)declSpec).getName().resolveBinding();
				}
				if( binding instanceof ICPPClassType )
					result = (ICPPClassType[])ArrayUtil.append( ICPPClassType.class, result, binding );
            } 
        }
		return (ICPPClassType[]) ArrayUtil.trim( ICPPClassType.class, result );
	}
}
