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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
abstract public class CPPScope implements ICPPScope{
    public static class CPPScopeProblem extends ProblemBinding implements ICPPScope {
        public CPPScopeProblem( IASTNode node, int id, char[] arg ) {
            super( node, id, arg );
        }
        public void addName( IASTName name ) throws DOMException {
            throw new DOMException( this );
        }

        public IBinding getBinding( IASTName name, boolean resolve ) throws DOMException {
            throw new DOMException( this );
        }

        public IScope getParent() throws DOMException {
            throw new DOMException( this );
        }

        public IBinding[] find( String name ) throws DOMException {
            throw new DOMException( this );
        }
		public void setFullyCached(boolean b) {
		}
		public boolean isFullyCached() {
			return false;
		}
        public IASTName getScopeName() throws DOMException {
            throw new DOMException( this );
        }
    }
    public static class CPPTemplateProblem extends CPPScopeProblem {
		public CPPTemplateProblem( IASTNode node, int id, char[] arg) {
			super( node, id, arg);
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
	
	public IASTNode getPhysicalNode() throws DOMException{
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
		if( name instanceof ICPPASTTemplateId )
			name = ((ICPPASTTemplateId)name).getTemplateName();
		
		char [] c = name.toCharArray();
		Object o = bindings.get( c );
		if( o != null ){
		    if( o instanceof ObjectSet ){
		    	((ObjectSet)o).put( name );
		        //bindings.put( c, ArrayUtil.append( Object.class, (Object[]) o, name ) );
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
	    if( c.length == 0 || bindings == null )
	        return null;
	    
	    Object obj = bindings.get( c );
	    if( obj != null ){
	        if( obj instanceof ObjectSet ) {
	        	if( forceResolve )
	        		return CPPSemantics.resolveAmbiguities( name,  ((ObjectSet) obj).keyArray() );
	        	IBinding [] bs = null;
        		Object [] os = ((ObjectSet) obj).keyArray();
        		for( int i = 0; i < os.length; i++ ){
        			if( os[i] instanceof IASTName ){
        				IASTName n = (IASTName) os[i];
        				if( n instanceof ICPPASTQualifiedName ){
        					IASTName [] ns = ((ICPPASTQualifiedName)n).getNames();
        					n = ns[ ns.length - 1 ];
        				}
        				bs = (IBinding[]) ArrayUtil.append( IBinding.class, bs, n.getBinding() );
        			} else
						bs = (IBinding[]) ArrayUtil.append( IBinding.class, bs, os[i] );
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
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public IBinding[] find(String name) throws DOMException {
	    return CPPSemantics.findBindings( this, name, false );
	}
}
