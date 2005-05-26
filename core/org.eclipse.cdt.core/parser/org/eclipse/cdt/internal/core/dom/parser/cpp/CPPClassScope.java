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
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;

/**
 * @author aniefer
 */
public class CPPClassScope extends CPPScope implements ICPPClassScope {
    private ObjectSet constructorBindings = ObjectSet.EMPTY_SET;
    private ObjectSet constructorNames = ObjectSet.EMPTY_SET;
    private ICPPMethod[] implicits = null;
    
	public CPPClassScope( ICPPASTCompositeTypeSpecifier physicalNode ) {
		super( physicalNode );
		((CPPASTCompositeTypeSpecifier)physicalNode).setScope( this );
		createImplicitMembers();
	}

	// 12.1 The default constructor, copy constructor, copy assignment operator, and destructor are
	//special member functions.  The implementation will implicitly declare these member functions
	//for a class type when the program does not declare them.
	private void createImplicitMembers(){
	    //create bindings for the implicit members, if the user declared them then those declarations
	    //will resolve to these bindings.
	    ICPPASTCompositeTypeSpecifier compTypeSpec = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
        
        IASTName name = compTypeSpec.getName();
        if( name instanceof ICPPASTQualifiedName ){
        	IASTName [] ns = ((ICPPASTQualifiedName) name).getNames();
        	name = ns[ ns.length - 1 ];
        }
        
        IBinding binding = name.resolveBinding();
        if( !(binding instanceof ICPPClassType ) )
        	return;
        
        implicits = new ICPPMethod[4];
        ICPPClassType clsType = (ICPPClassType) binding;
        if( clsType instanceof ICPPClassTemplate ){
            try {
                clsType = (ICPPClassType) CPPTemplates.instantiateWithinClassTemplate( (ICPPClassTemplate) clsType );
            } catch ( DOMException e ) {
            }
        }
        char [] className = name.toCharArray();
            
		IParameter [] voidPs = new IParameter [] { new CPPParameter( CPPSemantics.VOID_TYPE ) };
        if( !hasNonStandardDefaultConstructor( compTypeSpec ) ) {
	        //default constructor: A(void)
	        ICPPMethod m = new CPPImplicitConstructor( this, className, voidPs );
	        implicits[0] = m;
		    addBinding( m );
        }
	    
	    //copy constructor: A( const A & )
	    IType pType = new CPPReferenceType( new CPPQualifierType( clsType, true, false ) );
	    IParameter [] ps = new IParameter [] { new CPPParameter( pType ) };
	    ICPPMethod m = new CPPImplicitConstructor( this, className, ps );
	    implicits[1] = m;
	    addBinding( m );
	    
	    //copy assignment operator: A& operator = ( const A & ) 
	    IType refType = new CPPReferenceType( clsType );
	    m = new CPPImplicitMethod( this, ICPPASTOperatorName.OPERATOR_ASSIGN, refType, ps ); //$NON-NLS-1$
	    implicits[2] = m;
	    addBinding( m );
	    
	    //destructor: ~A()
	    char [] dtorName = CharArrayUtils.concat( "~".toCharArray(), className );  //$NON-NLS-1$
	    m = new CPPImplicitMethod( this, dtorName, new CPPBasicType( IBasicType.t_unspecified, 0 ), voidPs );
	    implicits[3] = m;
	    addBinding( m );
	}
	
	private boolean hasNonStandardDefaultConstructor( ICPPASTCompositeTypeSpecifier compSpec ){
		IASTDeclaration [] members = compSpec.getMembers();
		char [] name = compSpec.getName().toCharArray();
		IASTDeclarator dtor = null;
		IASTDeclSpecifier spec = null;
        for( int i = 0; i < members.length; i++ ){
			if( members[i] instanceof IASTSimpleDeclaration ){
			    IASTDeclarator [] dtors = ((IASTSimpleDeclaration)members[i]).getDeclarators();
			    if( dtors.length == 0 || dtors.length > 1 )
			    	continue;
			    dtor = dtors[0];
			    spec = ((IASTSimpleDeclaration)members[i]).getDeclSpecifier();
			} else if( members[i] instanceof IASTFunctionDefinition ){
			    dtor = ((IASTFunctionDefinition)members[i]).getDeclarator();
			    spec = ((IASTFunctionDefinition)members[i]).getDeclSpecifier();
			}
			if( !(dtor instanceof ICPPASTFunctionDeclarator) || !(spec instanceof IASTSimpleDeclSpecifier) ||
				((IASTSimpleDeclSpecifier)spec).getType() != IASTSimpleDeclSpecifier.t_unspecified ||
				!CharArrayUtils.equals( dtor.getName().toCharArray(), name ) )
			{
				continue;
			}
			
			IASTParameterDeclaration [] ps = ((ICPPASTFunctionDeclarator)dtor).getParameters();
        	if( ps.length >= 1 ){
        		IASTDeclarator d = ps[0].getDeclarator();
        		IASTDeclSpecifier s = ps[0].getDeclSpecifier();
        		if( s instanceof IASTSimpleDeclSpecifier &&
        		    ((IASTSimpleDeclSpecifier)s).getType() == IASTSimpleDeclSpecifier.t_void &&
        		    d.getPointerOperators().length == 0 && !(d instanceof IASTArrayDeclarator) )
        		{
        			continue;  //A(void)
        		}
        		    
        		if( d.getInitializer() != null )
        			return true;
        	}
	    }
		return false;
	}
	
	public IScope getParent() {
	    ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
	    IASTName compName = compType.getName();
	    if( compName instanceof ICPPASTQualifiedName ){
	    	IASTName [] ns = ((ICPPASTQualifiedName)compName).getNames();
	    	compName = ns[ ns.length - 1 ];
	    }
		return CPPVisitor.getContainingScope( compName );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#addBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public void addBinding(IBinding binding) {
	    if( binding instanceof ICPPConstructor ){
	        addConstructor( binding );
	        return;
	    }
        super.addBinding(binding);
	}

	public void addName(IASTName name) {
		if( name instanceof ICPPASTQualifiedName )
			return;
		
		IASTNode parent = name.getParent();
		if( parent instanceof IASTDeclarator ){
			if( CPPVisitor.isConstructor( this, (IASTDeclarator) parent ) ){
				addConstructor( name );
				return;
			}
		}
		super.addName( name );
	}

	private void addConstructor( Object constructor ){
		if( constructor instanceof IBinding ){
		    if( constructorBindings == ObjectSet.EMPTY_SET )
		        constructorBindings = new ObjectSet( 2 );
		    
		    constructorBindings.put( constructor );
		} else {
			if( constructorNames == ObjectSet.EMPTY_SET )
				constructorNames = new ObjectSet( 2 );
		    
		    constructorNames.put( constructor );
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getBinding(int, char[])
	 */
	public IBinding getBinding( IASTName name, boolean resolve ) throws DOMException {
	    char [] c = name.toCharArray();
	
	    ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
	    IASTName compName = compType.getName();
	    if( compName instanceof ICPPASTQualifiedName ){
	    	IASTName [] ns = ((ICPPASTQualifiedName)compName).getNames();
	    	compName = ns[ ns.length - 1 ];
	    }
	    if( CharArrayUtils.equals( c, compName.toCharArray() ) ){
	        if( isConstructorReference( name ) ){
//	            if( constructors == null )
//	                return null;
	            return CPPSemantics.resolveAmbiguities( name, getConstructors( resolve ) );
	        }
            //9.2 ... The class-name is also inserted into the scope of the class itself
            return compName.resolveBinding();
	    }
	    return super.getBinding( name, resolve );
	}

	protected ICPPConstructor [] getConstructors( boolean forceResolve ){
		if( forceResolve && constructorNames.size() > 0 ){
			Object [] names = constructorNames.keyArray();
			for( int i = 0; i < names.length; i++ ){
				ICPPConstructor ctor = (ICPPConstructor) ((IASTName)names[i]).resolveBinding();
				constructorBindings.put( ctor );
			}
			constructorNames.clear();
		}
	    return (ICPPConstructor[]) ArrayUtil.trim( ICPPConstructor.class, constructorBindings.keyArray(), true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public IBinding[] find(String name) throws DOMException {
	    char [] n = name.toCharArray();
	    ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
	    IASTName compName = compType.getName();
	    if( compName instanceof ICPPASTQualifiedName ){
	    	IASTName [] ns = ((ICPPASTQualifiedName)compName).getNames();
	    	compName = ns[ ns.length - 1 ];
	    }
	    if( CharArrayUtils.equals( n, compName.toCharArray() ) ){
	        return (IBinding[]) ArrayUtil.addAll( IBinding.class, null, getConstructors( true ) );
	    }
	    return super.find( name );
	}
	
	private boolean isConstructorReference( IASTName name ){
	    if( name.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY ) return false;
	    IASTNode node = name.getParent();
	    if( node instanceof ICPPASTTemplateId )
	    	node = node.getParent();
	    if( node instanceof ICPPASTQualifiedName ){
	    	IASTName [] ns = ((ICPPASTQualifiedName)node).getNames();
	    	if( ns[ ns.length - 1 ] == name )
	    		node = node.getParent();
	    	else
	    		return false;
	    }
	    if( node instanceof IASTDeclSpecifier ){
	        IASTNode parent = node.getParent();
	        if( parent instanceof IASTTypeId && parent.getParent() instanceof ICPPASTNewExpression )
	            return true;
	        return false;
	    }
	    return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope#getClassType()
	 */
	public ICPPClassType getClassType() {
		ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
		return (ICPPClassType) compSpec.getName().resolveBinding();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope#getImplicitMethods()
	 */
	public ICPPMethod[] getImplicitMethods() {
		if( implicits == null )
			implicits = new ICPPMethod[] { new CPPMethod.CPPMethodProblem( null, IProblemBinding.SEMANTIC_INVALID_TYPE, CPPSemantics.EMPTY_NAME_ARRAY ) };
		return implicits;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getScopeName()
     */
    public IASTName getScopeName() {
        IASTNode node = getPhysicalNode();
        if( node instanceof ICPPASTCompositeTypeSpecifier ){
            return ((ICPPASTCompositeTypeSpecifier)node).getName();
        }
        return null;
    }
    
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#removeBinding(org.eclipse.cdt.core.dom.ast.IBinding)
     */
	public void removeBinding(IBinding binding) {
	    if( binding instanceof ICPPConstructor ){
	        if( constructorBindings.containsKey( binding ) )
	            constructorBindings.remove( binding );
	        setFullyCached( false );
	    } else {
	        super.removeBinding( binding );
	    }
	}
	
	public void flushCache() {
		constructorNames.clear();
		for( int i = constructorBindings.size() - 1; i >= 0; i-- ){
			IBinding binding = (IBinding) constructorBindings.keyAt(i);
			if( !(binding instanceof CPPImplicitConstructor) )
				constructorBindings.remove( binding );
		}
		super.flushCache();
	}
}
