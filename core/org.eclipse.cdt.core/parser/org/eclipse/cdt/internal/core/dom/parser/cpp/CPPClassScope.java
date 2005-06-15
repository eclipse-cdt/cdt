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
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;

/**
 * @author aniefer
 */
public class CPPClassScope extends CPPScope implements ICPPClassScope {
	private static final char [] CONSTRUCTOR_KEY = "!!!CTOR!!!".toCharArray(); //$NON-NLS-1$ 
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
		if( bindings == null )
            bindings = new CharArrayObjectMap(1);
        
		if( constructor instanceof IASTName && ((IASTName)constructor).getBinding() != null ){
			constructor = ((IASTName)constructor).getBinding();
		}
		
        Object o = bindings.get( CONSTRUCTOR_KEY );
        if( o != null ){
            if( o instanceof ObjectSet ){
                ((ObjectSet)o).put( constructor );
            } else {
                ObjectSet set = new ObjectSet(2);
                set.put( o );
                set.put( constructor );
                bindings.put( CONSTRUCTOR_KEY, set );
            }
        } else {
            bindings.put( CONSTRUCTOR_KEY, constructor );
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
	            return CPPSemantics.resolveAmbiguities( name, getConstructors( bindings, resolve ) );
	        }
            //9.2 ... The class-name is also inserted into the scope of the class itself
            return compName.resolveBinding();
	    }
	    return super.getBinding( name, resolve );
	}

	protected ICPPConstructor [] getConstructors( boolean forceResolve ){
		return getConstructors( bindings, forceResolve );
	}
	static protected ICPPConstructor [] getConstructors( CharArrayObjectMap bindings, boolean forceResolve ){
		if( bindings == null )
			return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		
		Object o = bindings.get( CONSTRUCTOR_KEY );
		if( o != null ){
			IBinding binding = null;
	        if( o instanceof ObjectSet ) {
	        	ObjectSet set = (ObjectSet) o;
	        	IBinding [] bs = null;
        		for( int i = 0; i < set.size(); i++ ){
        			Object obj = set.keyAt( i );
        			if( obj instanceof IASTName ){
        				IASTName n = (IASTName) obj;
        				binding = n.getBinding();
        				if( binding != null || forceResolve ){
        					binding = n.resolveBinding();
        					set.remove( n );
        					set.put( binding );
        					i--;
        					continue;
        				}
        			} else if( obj instanceof ICPPConstructor )
						bs = (IBinding[]) ArrayUtil.append( ICPPConstructor.class, bs, obj );
        		}	    
        		return (ICPPConstructor[]) ArrayUtil.trim( ICPPConstructor.class, bs );
	        } else if( o instanceof IASTName ){
	        	if( forceResolve || ((IASTName)o).getBinding() != null ){
	        		binding = ((IASTName)o).resolveBinding();
	        		bindings.put( CONSTRUCTOR_KEY, binding );
	        	}
	        } else if( o instanceof IBinding ){
	        	binding = (IBinding) o;
	        }
	        if( binding != null && binding instanceof ICPPConstructor){
	        	return new ICPPConstructor[] { (ICPPConstructor) binding };
	        }
	    }
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
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
	        return (IBinding[]) ArrayUtil.addAll( IBinding.class, null, getConstructors( bindings, true ) );
	    }
	    return super.find( name );
	}
	
	static protected boolean isConstructorReference( IASTName name ){
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
	        removeBinding( CONSTRUCTOR_KEY, binding );
	    } else {
	        removeBinding( binding.getNameCharArray(), binding );
	    }
	}
}
