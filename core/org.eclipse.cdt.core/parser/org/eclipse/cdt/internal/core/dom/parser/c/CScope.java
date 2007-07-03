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
 * Created on Nov 25, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICScope;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author aniefer
 */
public class CScope implements ICScope, IASTInternalScope {
	/**
	 * ISO C:99 6.2.3 there are separate namespaces for various categories of
	 * identifiers: - label names ( labels have ICFunctionScope ) - tags of
	 * structures or unions : NAMESPACE_TYPE_TAG - members of structures or
	 * unions ( members have ICCompositeTypeScope ) - all other identifiers :
	 * NAMESPACE_TYPE_OTHER
	 */
	public static final int NAMESPACE_TYPE_TAG = 0;
	public static final int NAMESPACE_TYPE_OTHER = 1;
	public static final int NAMESPACE_TYPE_BOTH = 2;
	
    private IASTNode physicalNode = null;
    private boolean isFullyCached = false;
    
    private CharArrayObjectMap [] bindings = { CharArrayObjectMap.EMPTY_MAP, CharArrayObjectMap.EMPTY_MAP };
    
    public CScope( IASTNode physical ){
        physicalNode = physical;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
     */
    public IScope getParent() {
        return CVisitor.getContainingScope( physicalNode );
    }

    protected static class CollectNamesAction extends CASTVisitor {
        private char [] name;
        private IASTName [] result = null;
        CollectNamesAction( char [] n ){
            name = n;
            shouldVisitNames = true;
        }
        public int visit( IASTName n ){
            ASTNodeProperty prop = n.getPropertyInParent();
            if( prop == IASTElaboratedTypeSpecifier.TYPE_NAME ||
                prop == IASTCompositeTypeSpecifier.TYPE_NAME ||
                prop == IASTDeclarator.DECLARATOR_NAME )
            {
                if( CharArrayUtils.equals( n.toCharArray(), name ) )
                    result = (IASTName[]) ArrayUtil.append( IASTName.class, result, n );    
            }
            
            return PROCESS_CONTINUE; 
        }
        public int visit( IASTStatement statement ){
            if( statement instanceof IASTDeclarationStatement )
                return PROCESS_CONTINUE;
            return PROCESS_SKIP;
        }
        public IASTName [] getNames(){
            return (IASTName[]) ArrayUtil.trim( IASTName.class, result );
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    public IBinding[] find( String name ) throws DOMException {
    	return CVisitor.findBindings( this, name, false );
    }

    public IBinding getBinding( int namespaceType, char [] name ){
        IASTName n = (IASTName) bindings[namespaceType].get( name );
        return ( n != null ) ? n.resolveBinding() : null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICScope#removeBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public void removeBinding(IBinding binding) {
        int type = ( binding instanceof ICompositeType || binding instanceof IEnumeration ) ? 
				NAMESPACE_TYPE_TAG : NAMESPACE_TYPE_OTHER;

		if( bindings[type] != CharArrayObjectMap.EMPTY_MAP ) {
			bindings[type].remove( binding.getNameCharArray(), 0, binding.getNameCharArray().length);
		}
		isFullyCached = false;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getPhysicalNode()
     */
    public IASTNode getPhysicalNode() {
        return physicalNode;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#addName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void addName( IASTName name ) {
        int type = getNamespaceType( name );
        if( bindings[type] == CharArrayObjectMap.EMPTY_MAP )
            bindings[type] = new CharArrayObjectMap(1);
        
        char [] n = name.toCharArray();
        Object current= bindings[type].get( n );
        if( !(current instanceof IASTName) || ((CASTName)current).getOffset() > ((CASTName) name).getOffset() ){
            bindings[type].put( n, name );
        }
    }

    private int getNamespaceType( IASTName name ){
        ASTNodeProperty prop = name.getPropertyInParent();
        if( prop == IASTCompositeTypeSpecifier.TYPE_NAME ||
            prop == IASTElaboratedTypeSpecifier.TYPE_NAME ||
            prop == IASTEnumerationSpecifier.ENUMERATION_NAME ||
            prop == CVisitor.STRING_LOOKUP_TAGS_PROPERTY )
        {
            return NAMESPACE_TYPE_TAG;
        }
        
        return NAMESPACE_TYPE_OTHER;
    }
    public IBinding getBinding( IASTName name, boolean resolve ) {
	    char [] c = name.toCharArray();
	    if( c.length == 0  ){
	        return null;
	    }
	    
	    int type = getNamespaceType( name );
	    Object o = bindings[type].get( name.toCharArray() );
	    
	    if( o == null || name == o) {
	    	IBinding result= null;
	    	if(physicalNode instanceof IASTTranslationUnit) {
	    		IIndex index= ((IASTTranslationUnit)physicalNode).getIndex();
	    		if(index!=null) {
	    			try {
	    				IBinding[] bindings= index.findBindings(name.toCharArray(), getIndexFilter(type), new NullProgressMonitor());
	    				result= processIndexResults(name, bindings);
	    			} catch(CoreException ce) {
	    				CCorePlugin.log(ce);
	    			}
	    		}
	    	}
	    	return result;
	    }
	        
	    
	    if( o instanceof IBinding )
	        return (IBinding) o;
	
	    IASTName foundName= (IASTName) o;
	    if( (resolve || foundName.getBinding() != null) && ( foundName != name ) ) {
	    	if(!isTypeDefinition(name) || CVisitor.declaredBefore(foundName, name)) {
	    		return foundName.resolveBinding();
	    	}
	    }
	
	    return null;
	}

    private boolean isTypeDefinition(IASTName name) {
    	return name.getPropertyInParent()==IASTNamedTypeSpecifier.NAME;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#getBinding(org.eclipse.cdt.core.dom.ast.IASTName, boolean)
     */
    public IBinding[] getBindings( IASTName name, boolean resolve, boolean prefixLookup ) {
        char [] c = name.toCharArray();
        
        Object[] obj = null;
        
        for (int i = 0; i < bindings.length; i++) {
        	if (prefixLookup) {
        		Object[] keys = bindings[i].keyArray();
        		for (int j = 0; j < keys.length; j++) {
        			char[] key = (char[]) keys[j];
        			if (CharArrayUtils.equals(key, 0, c.length, c, true)) {
        				obj = ArrayUtil.append(obj, bindings[i].get(key));
        			}
        		}
        	} else {
        		obj = ArrayUtil.append(obj, bindings[i].get(c));
        	}
        }

       	if(physicalNode instanceof IASTTranslationUnit) {
        	IIndex index= ((IASTTranslationUnit)physicalNode).getIndex();
        	if(index!=null) {
        		try {
        			IBinding[] bindings = prefixLookup ?
							index.findBindingsForPrefix(name.toCharArray(), true, getIndexFilter(NAMESPACE_TYPE_BOTH), null) :
							index.findBindings(name.toCharArray(), getIndexFilter(NAMESPACE_TYPE_BOTH), null);
					obj = ArrayUtil.addAll(Object.class, obj, bindings);
        		} catch(CoreException ce) {
        			CCorePlugin.log(ce);
        		}
        	}
        }
       	obj = ArrayUtil.trim(Object.class, obj);
       	IBinding[] result = null;
        
       	for (int i = 0; i < obj.length; i++) {
            if( obj[i] instanceof IBinding )
            	result = (IBinding[]) ArrayUtil.append(IBinding.class, result, obj[i]);

            if( (resolve || ((IASTName)obj[i]).getBinding() != null) && ( obj[i] != name ) )
            	result = (IBinding[]) ArrayUtil.append(IBinding.class, result, ((IASTName)obj[i]).resolveBinding());
       	}

        return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
    }
    
    /**
     * Index results from global scope, differ from ast results from translation unit scope. This routine
     * is intended to fix results from the index to be consistent with ast scope behaviour.
     * @param name the name as it occurs in the ast
     * @param bindings the set of candidate bindings
     * @return the appropriate binding, or null if no binding is appropriate for the ast name
     */
    private IBinding processIndexResults(IASTName name, IBinding[] bindings) {
    	if(bindings.length!=1)
    		return null;

    	IBinding candidate= bindings[0];
    	if(candidate instanceof IFunction) {
    		IASTNode parent= name.getParent();
    		if(parent instanceof IASTFunctionDeclarator) {
    			IASTNode parent2= parent.getParent();
    			if(parent2 instanceof IASTFunctionDefinition) {
    				IASTFunctionDefinition def= (IASTFunctionDefinition) parent2;
    				if(def.getDeclSpecifier().getStorageClass()==IASTDeclSpecifier.sc_static) {
    					try {
    						if(!((IFunction)candidate).isStatic()) {
    							return null;
    						}
    					} catch(DOMException de) {
    						CCorePlugin.log(de);
    					}
    				}
    			}
    		}
    	}

    	return candidate;
    }
    

    /**
     * Returns a C-linkage filter suitable for searching the index for the types of bindings
     * specified
     * @param type the types of bindings to search for. One of {@link CScope#NAMESPACE_TYPE_TAG}
     * or {@link CScope#NAMESPACE_TYPE_OTHER}, otherwise the C-linkage will not be filtered
     * @return a C-linkage filter suitable for searching the index for the types of bindings
     * specified
     */
    private IndexFilter getIndexFilter(final int type) {
    	switch(type) {
    		case NAMESPACE_TYPE_TAG:
    		return new IndexFilter() {
    			public boolean acceptBinding(IBinding binding) {
    				return binding instanceof ICompositeType || binding instanceof IEnumeration;
    			}
    			public boolean acceptLinkage(ILinkage linkage) {
    				return linkage.getID().equals(ILinkage.C_LINKAGE_ID);
    			}
    		};
    		case NAMESPACE_TYPE_OTHER:
    			return new IndexFilter() {
        			public boolean acceptBinding(IBinding binding) {
        				return !(binding instanceof ICompositeType || binding instanceof IEnumeration);
        			}
        			public boolean acceptLinkage(ILinkage linkage) {
        				return linkage.getID().equals(ILinkage.C_LINKAGE_ID);
        			}
        		};
        	default:
        		return IndexFilter.getFilter(ILinkage.C_LINKAGE_ID);
    	}
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#setFullyCached(boolean)
     */
    public void setFullyCached( boolean b ){
        isFullyCached = b;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#isFullyCached()
     */
    public boolean isFullyCached(){
        return isFullyCached;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getScopeName()
	 */
	public IName getScopeName() {
		if( physicalNode instanceof IASTCompositeTypeSpecifier ){
			return ((IASTCompositeTypeSpecifier) physicalNode).getName();
		}
		return null;
	}

	public void flushCache() {
		bindings[0].clear();
		bindings[1].clear();
		isFullyCached = false;
	}

	public void addBinding(IBinding binding) {
		int type = NAMESPACE_TYPE_OTHER;
        if (binding instanceof ICompositeType || binding instanceof IEnumeration) {
            type = NAMESPACE_TYPE_TAG;
        }
            
        if( bindings[type] == CharArrayObjectMap.EMPTY_MAP )
           bindings[type] = new CharArrayObjectMap(2);
        
		bindings[type].put(binding.getNameCharArray(), binding);
	}
}
