/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
/*
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author aniefer
 */
abstract public class CPPScope implements ICPPScope, IASTInternalScope {
	private static final IProgressMonitor NPM = new NullProgressMonitor();

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
		IBinding binding= getBindingInAST(name, forceResolve);
		if (binding == null) {
			IIndex index = name.getTranslationUnit().getIndex();
			if (index != null) {
				// Try looking this up in the PDOM
				if (physicalNode instanceof IASTTranslationUnit) {
					try {
						IBinding[] bindings= index.findBindings(name.toCharArray(), IndexFilter.CPP_DECLARED_OR_IMPLICIT, NPM);
						binding= CPPSemantics.resolveAmbiguities(name, bindings);
					} catch (CoreException e) {
		        		CCorePlugin.log(e);
					}
				}
				else if (physicalNode instanceof ICPPASTNamespaceDefinition) {
					ICPPASTNamespaceDefinition nsdef = (ICPPASTNamespaceDefinition)physicalNode;
					IASTName nsname = nsdef.getName();
					IBinding nsbinding= nsname.resolveBinding();
					if(nsbinding instanceof ICPPNamespace) {
						ICPPNamespace nsbindingAdapted = (ICPPNamespace) index.adaptBinding(nsbinding);
						if(nsbindingAdapted!=null) {
							IBinding[] bindings = nsbindingAdapted.getNamespaceScope().find(name.toString());
							binding= CPPSemantics.resolveAmbiguities(name, bindings);
						}
					}
				}
			}
		}
		return binding;
	}
	
	public IBinding getBindingInAST(IASTName name, boolean forceResolve) throws DOMException {
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
	        		binding = CPPSemantics.resolveAmbiguities(name, new Object[] { obj });
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

	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup) throws DOMException {
		IBinding[] result = getBindingsInAST(name, resolve, prefixLookup);

		IIndex index = name.getTranslationUnit().getIndex();
		if (index != null) {
			if (physicalNode instanceof IASTTranslationUnit) {
				try {
					IndexFilter filter = IndexFilter.CPP_DECLARED_OR_IMPLICIT;
					IBinding[] bindings = prefixLookup ?
							index.findBindingsForPrefix(name.toCharArray(), true, filter, null) :
							index.findBindings(name.toCharArray(), filter, null);
					result = (IBinding[]) ArrayUtil.addAll(IBinding.class, result, bindings);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			} else if (physicalNode instanceof ICPPASTNamespaceDefinition) {
				ICPPASTNamespaceDefinition ns = (ICPPASTNamespaceDefinition) physicalNode;
				try {
					IIndexBinding binding = index.findBinding(ns.getName());
					if (binding instanceof ICPPNamespace) {
						ICPPNamespaceScope indexNs = ((ICPPNamespace)binding).getNamespaceScope();
						IBinding[] bindings = indexNs.getBindings(name, resolve, prefixLookup);
						result = (IBinding[]) ArrayUtil.addAll(IBinding.class, result, bindings);
					}
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}

		return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	}
	
	public IBinding[] getBindingsInAST(IASTName name, boolean forceResolve, boolean prefixLookup) throws DOMException {
	    char [] c = name.toCharArray();
	    IBinding[] result = null;
	    
	    Object[] obj = null;
	    if (prefixLookup) {
	    	Object[] keys = bindings != null ? bindings.keyArray() : new Object[0];
	    	for (int i = 0; i < keys.length; i++) {
	    		char[] key = (char[]) keys[i];
	    		if (CharArrayUtils.equals(key, 0, c.length, c, true)) {
	    			obj = ArrayUtil.append(obj, bindings.get(key));
	    		}
	    	}
	    } else {
	    	obj = bindings != null ? new Object[] {bindings.get( c )} : null;
	    }
	    
	    obj = ArrayUtil.trim(Object.class, obj);
	    for (int i = 0; i < obj.length; i++) {
	        if( obj[i] instanceof ObjectSet ) {
	        	ObjectSet os = (ObjectSet) obj[i];
        		for( int j = 0; j < os.size(); j++ ){
        			Object o = os.keyAt( j );
        			if( o instanceof IASTName ){
        				IASTName n = (IASTName) o;
        				if( n instanceof ICPPASTQualifiedName ){
        					IASTName [] ns = ((ICPPASTQualifiedName)n).getNames();
        					n = ns[ ns.length - 1 ];
        				}
        				IBinding binding = forceResolve ? n.resolveBinding() : n.getBinding();
        				result = (IBinding[]) ArrayUtil.append( IBinding.class, result, binding );
        			} else
        				result = (IBinding[]) ArrayUtil.append( IBinding.class, result, o );
        		}
	        } else if( obj[i] instanceof IASTName ){
	        	IBinding binding = null;
	        	if( forceResolve && obj[i] != name && obj[i] != name.getParent())
	        		binding = ((IASTName) obj[i]).resolveBinding();
	        	else {
	        		IASTName n = (IASTName) obj[i];
    				if( n instanceof ICPPASTQualifiedName ){
    					IASTName [] ns = ((ICPPASTQualifiedName)n).getNames();
    					n = ns[ ns.length - 1 ];
    				}
	        		binding = n.getBinding();
	        	}
	        	if( binding instanceof ICPPUsingDeclaration ){
	        		result = (IBinding[]) ArrayUtil.addAll(IBinding.class, result, ((ICPPUsingDeclaration)binding).getDelegates());
	        	}
	        	result = (IBinding[]) ArrayUtil.append(IBinding.class, result, binding);
	        } else {
	        	result = (IBinding[]) ArrayUtil.append(IBinding.class, result, obj[i]);
	        }
	    }
	    return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
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
