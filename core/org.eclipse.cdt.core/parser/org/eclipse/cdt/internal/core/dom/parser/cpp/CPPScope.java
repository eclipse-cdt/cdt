/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;

/**
 * @author aniefer
 */
abstract public class CPPScope implements ICPPScope{
    public static class CPPScopeProblem extends ProblemBinding implements ICPPScope {
        public CPPScopeProblem( IASTNode node, int id, char[] arg ) {
            super( node, id, arg );
        }
    }

    private IASTNode physicalNode;
	public CPPScope( IASTNode physicalNode ) {
		this.physicalNode = physicalNode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
	 */
	public IScope getParent() throws DOMException {
		return CPPVisitor.getContainingScope( physicalNode );
	}
	
	public IASTNode getPhysicalNode() {
		return physicalNode;
	}

	protected CharArrayObjectMap bindings = null;
	
	public void addName(IASTName name) {
		if( bindings == null )
			bindings = new CharArrayObjectMap(1);
		if( name instanceof ICPPASTQualifiedName ){
			//name belongs to a different scope, don't add it here
			return;
		}
		char [] c = name.toCharArray();
		Object o = bindings.get( c );
		if( o != null ){
		    if( o instanceof ObjectSet ){
		    	((ObjectSet)o).put( name );
		    } else {
		    	ObjectSet temp = new ObjectSet( 2 );
		    	temp.put( o );
		    	temp.put( name );
		        bindings.put( c, temp );
		    }
		} else {
		    bindings.put( c, name );
		}
	}

	public IBinding getBinding(IASTName name, boolean forceResolve) throws DOMException {
	    char [] c = name.toCharArray();
	    //can't look up bindings that don't have a name
	    if( c.length == 0 )
	        return null;
	    
	    Object obj = bindings != null ? bindings.get( c ) : null;
	    if( obj != null ){
	        if( obj instanceof ObjectSet ) {
	        	ObjectSet os = (ObjectSet) obj;
	        	if( forceResolve )
	        		return CPPSemantics.resolveAmbiguities( name,  os.keyArray() );
	        	IBinding [] bs = null;
        		for( int i = 0; i < os.size(); i++ ){
        			Object o = os.keyAt( i );
        			if( o instanceof IASTName ){
        				IASTName n = (IASTName) o;
        				if( n instanceof ICPPASTQualifiedName ){
        					IASTName [] ns = ((ICPPASTQualifiedName)n).getNames();
        					n = ns[ ns.length - 1 ];
        				}
        				bs = (IBinding[]) ArrayUtil.append( IBinding.class, bs, n.getBinding() );
        			} else
						bs = (IBinding[]) ArrayUtil.append( IBinding.class, bs, o );
        		}
        		return CPPSemantics.resolveAmbiguities( name,  bs );
	        } else if( obj instanceof IASTName ){
	        	IBinding binding = null;
	        	if( forceResolve && obj != name && obj != name.getParent())
	        		binding = ((IASTName)obj).resolveBinding();
	        	else {
	        		IASTName n = (IASTName) obj;
    				if( n instanceof ICPPASTQualifiedName ){
    					IASTName [] ns = ((ICPPASTQualifiedName)n).getNames();
    					n = ns[ ns.length - 1 ];
    				}
	        		binding = n.getBinding();
	        	}
	        	if( binding instanceof ICPPUsingDeclaration ){
	        		return CPPSemantics.resolveAmbiguities( name, ((ICPPUsingDeclaration)binding).getDelegates() );
	        	}
	        	return binding;
	        }
	        return (IBinding) obj;
	    } else {
	    	IPDOM pdom = name.getTranslationUnit().getIndex();
	    	if (pdom != null) {
	    		// Try looking this up in the PDOM
	    		if (physicalNode instanceof ICPPASTNamespaceDefinition) {
	    			ICPPASTNamespaceDefinition nsdef = (ICPPASTNamespaceDefinition)physicalNode;
	    			IASTName nsname = nsdef.getName();
	    			IBinding nsbinding = ((PDOM)pdom).resolveBinding(nsname);
		    		if (nsbinding instanceof ICPPScope)
		    			return ((ICPPScope)nsbinding).getBinding(name, forceResolve);
	    		}
	    	}
	    }
		return null;
	}
	
	private boolean isfull = false;
	public void setFullyCached( boolean full ){
		isfull = full;
	}
	
	public boolean isFullyCached(){
		return isfull;
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#removeBinding(org.eclipse.cdt.core.dom.ast.IBinding)
     */
	public void removeBinding(IBinding binding) {
	    char [] key = binding.getNameCharArray();
	    removeBinding( key, binding );
	}
	
	protected void removeBinding( char [] key, IBinding binding ){
	    if( bindings == null || ! bindings.containsKey( key ) )
	        return;
	    
	    Object obj = bindings.get( key );
	    if( obj instanceof ObjectSet ){
	        ObjectSet set = (ObjectSet) obj;
	        for ( int i = set.size() - 1; i > 0; i-- ) {
                Object o = set.keyAt( i );
                if( (o instanceof IBinding && o == binding) ||
                    (o instanceof IASTName && ((IASTName)o).getBinding() == binding) )
                {
                    set.remove( o );
                }
            }
	        if( set.size() == 0 )
	            bindings.remove( key, 0, key.length );
	    } else if( (obj instanceof IBinding && obj == binding) ||
                   (obj instanceof IASTName && ((IASTName)obj).getBinding() == binding) )
	    {
	        bindings.remove( key, 0, key.length );
	    }
		isfull = false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public IBinding[] find(String name) throws DOMException {
	    return CPPSemantics.findBindings( this, name, false );
	}
	
	public void flushCache() {
		isfull = false;
		if( bindings != null )
			bindings.clear();
	}
    
    public void addBinding(IBinding binding) {
        if( bindings == null )
            bindings = new CharArrayObjectMap(1);
        char [] c = binding.getNameCharArray();
        Object o = bindings.get( c );
        if( o != null ){
            if( o instanceof ObjectSet ){
                ((ObjectSet)o).put( binding );
            } else {
                ObjectSet set = new ObjectSet(2);
                set.put( o );
                set.put( binding );
                bindings.put( c, set );
            }
        } else {
            bindings.put( c, binding );
        }
    }
}
