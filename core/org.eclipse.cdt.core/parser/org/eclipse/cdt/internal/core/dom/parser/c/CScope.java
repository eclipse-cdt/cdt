/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICScope;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTUnaryExpression;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
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
	
	private static final IndexFilter[] INDEX_FILTERS = {
		new IndexFilter() {	// namespace type tag
			@Override
			public boolean acceptBinding(IBinding binding) throws CoreException {
				return IndexFilter.C_DECLARED_OR_IMPLICIT.acceptBinding(binding) &&
					(binding instanceof ICompositeType || binding instanceof IEnumeration);
			}
			@Override
			public boolean acceptLinkage(ILinkage linkage) {
				return IndexFilter.C_DECLARED_OR_IMPLICIT.acceptLinkage(linkage);
			}
		},
		new IndexFilter() { // namespace type other
			@Override
			public boolean acceptBinding(IBinding binding) throws CoreException {
				return IndexFilter.C_DECLARED_OR_IMPLICIT.acceptBinding(binding) &&
					!(binding instanceof ICompositeType || binding instanceof IEnumeration);
			}
			@Override
			public boolean acceptLinkage(ILinkage linkage) {
				return IndexFilter.C_DECLARED_OR_IMPLICIT.acceptLinkage(linkage);
			}
		},
		// namespace type both
		IndexFilter.C_DECLARED_OR_IMPLICIT
	};
	
    private IASTNode physicalNode = null;
    private boolean isFullyCached = false;
    
    private CharArrayObjectMap[] mapsToNameOrBinding = { CharArrayObjectMap.EMPTY_MAP, CharArrayObjectMap.EMPTY_MAP };
    private ObjectMap reuseBindings= null;
    
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
        @Override
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
        @Override
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
    	Object o= mapsToNameOrBinding[namespaceType].get(name);
    	if (o instanceof IBinding)
    		return (IBinding) o;
    	
    	if (o instanceof IASTName) 
    		return ((IASTName) o).resolveBinding();

    	return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICScope#removeBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public void removeBinding(IBinding binding) {
        int type = ( binding instanceof ICompositeType || binding instanceof IEnumeration ) ? 
				NAMESPACE_TYPE_TAG : NAMESPACE_TYPE_OTHER;

		final CharArrayObjectMap bindingsMap = mapsToNameOrBinding[type];
		if( bindingsMap != CharArrayObjectMap.EMPTY_MAP ) {
			Object o= bindingsMap.remove( binding.getNameCharArray(), 0, binding.getNameCharArray().length);
			if (o != null && reuseBindings != null) {
				reuseBindings.remove(o);
			}
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
        CharArrayObjectMap map = mapsToNameOrBinding[type];
		if( map == CharArrayObjectMap.EMPTY_MAP )
            map= mapsToNameOrBinding[type] = new CharArrayObjectMap(1);
        
        final char [] n = name.toCharArray();
        final Object current= map.get( n );
        if (current instanceof IASTName) {
        	final CASTName currentName = (CASTName)current;
			if (currentName.getOffset() <= ((CASTName) name).getOffset() ){
        		return;
        	}
			if (name.getBinding() == null) {
        		// bug 232300: we need to make sure that the binding is picked up even if the name is removed
        		// from the cache. Simply assigning it to the name is not enough, because a declaration or 
        		// definition needs to be added to the binding.
				IBinding reuseBinding= currentName.getBinding();
				if (reuseBinding == null && reuseBindings != null) {
					reuseBinding= (IBinding) reuseBindings.get(currentName);
				}
				if (reuseBinding != null) {
	        		if (reuseBindings == null) {
	        			reuseBindings= new ObjectMap(1);
	        		}
	        		reuseBindings.put(name, reuseBinding);
	        		reuseBindings.remove(currentName);
				} 
			}
        }
        map.put(n, name);
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
    public final IBinding getBinding( IASTName name, boolean resolve ) {
    	return getBinding(name, resolve, IIndexFileSet.EMPTY);
    }
    
	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) throws DOMException {
		return getBindings(name, resolve, prefix, IIndexFileSet.EMPTY);
	}

    public IBinding getBinding( IASTName name, boolean resolve, IIndexFileSet fileSet ) {
	    char [] c = name.toCharArray();
	    if( c.length == 0  ){
	        return null;
	    }
	    
	    final int type = getNamespaceType( name );
	    Object o = mapsToNameOrBinding[type].get( name.toCharArray() );
	    
	    if( o instanceof IBinding )
	        return (IBinding) o;
	
	    if (o instanceof IASTName) {
	    	final IASTName n= (IASTName) o;
	    	if (!isTypeDefinition(name) || CVisitor.declaredBefore(n, name)) {
	    		IBinding b= n.getBinding();
	    		if (b != null)
	    			return b;

	    		if (reuseBindings != null) {
	    			b= (IBinding) reuseBindings.get(n);
	    			if (b != null)
	    				return b;
	    		}
	    		if (resolve && n != name) {
	    			return n.resolveBinding();
	    		}
	    	}
	    }
	    
    	IBinding result= null;
    	if(physicalNode instanceof IASTTranslationUnit) {
    		final IASTTranslationUnit tu = (IASTTranslationUnit)physicalNode;
			IIndex index= tu.getIndex();
    		if(index!=null) {
    			try {
    				IBinding[] bindings= index.findBindings(name.toCharArray(), INDEX_FILTERS[type], new NullProgressMonitor());
    				if (fileSet != null) {
    					bindings= fileSet.filterFileLocalBindings(bindings);
    				}
    				result= processIndexResults(name, bindings);
    			} catch(CoreException ce) {
    				CCorePlugin.log(ce);
    			}
    		}
    	}
    	return result;
	}

    private boolean isTypeDefinition(IASTName name) {
    	if (name.getPropertyInParent()==IASTNamedTypeSpecifier.NAME) {
    		return true;
    	}
    	IASTNode parent= name.getParent();
    	while (parent != null) {
    		if (parent instanceof IASTUnaryExpression) {
    			if (((IASTUnaryExpression) parent).getOperator() == IGNUASTUnaryExpression.op_typeof)
    				return true;
    		}
    		else if (parent instanceof IASTTypeIdExpression) {
    			if (((IASTTypeIdExpression) parent).getOperator() == IASTTypeIdExpression.op_typeof)
    				return true;
    		}
    		parent= parent.getParent();
    	}
    	return false;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#getBinding(org.eclipse.cdt.core.dom.ast.IASTName, boolean)
     */
    public IBinding[] getBindings( IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet ) {
        char [] c = name.toCharArray();
        
        Object[] obj = null;
        
        for (CharArrayObjectMap map : mapsToNameOrBinding) {
        	if (prefixLookup) {
        		Object[] keys = map.keyArray();
        		for (Object key2 : keys) {
        			char[] key = (char[]) key2;
        			if (CharArrayUtils.equals(key, 0, c.length, c, true)) {
        				obj = ArrayUtil.append(obj, map.get(key));
        			}
        		}
        	} else {
        		obj = ArrayUtil.append(obj, map.get(c));
        	}
        }

       	if(physicalNode instanceof IASTTranslationUnit) {
        	final IASTTranslationUnit tu = (IASTTranslationUnit)physicalNode;
			IIndex index= tu.getIndex();
        	if(index!=null) {
        		try {
        			IBinding[] bindings = prefixLookup ?
							index.findBindingsForPrefix(name.toCharArray(), true, INDEX_FILTERS[NAMESPACE_TYPE_BOTH], null) :
							index.findBindings(name.toCharArray(), INDEX_FILTERS[NAMESPACE_TYPE_BOTH], null);
					if (fileSet != null) {
						bindings= fileSet.filterFileLocalBindings(bindings);
					}
							
					obj = ArrayUtil.addAll(Object.class, obj, bindings);
        		} catch(CoreException ce) {
        			CCorePlugin.log(ce);
        		}
        	}
        }
       	obj = ArrayUtil.trim(Object.class, obj);
       	IBinding[] result = null;
        
       	for (Object element : obj) {
            if( element instanceof IBinding ) {
            	result = (IBinding[]) ArrayUtil.append(IBinding.class, result, element);
            } else if (element instanceof IASTName) {
    	    	final IASTName n= (IASTName) element;
    	    	IBinding b= n.getBinding();
    	    	if (b == null) {
    	    		if (reuseBindings != null) {
    	    			b= (IBinding) reuseBindings.get(n);
    	    		}
    	    		if (resolve && b == null && n != name) {
    	    			b= n.resolveBinding();
    	    		}
    	    	}
            	if (b != null) {
                	result = (IBinding[]) ArrayUtil.append(IBinding.class, result, b);
            	}
            		
            }
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
    	
    	return bindings[0];
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
		CharArrayObjectMap map= mapsToNameOrBinding[0];
		CharArrayObjectMap builtins= null;
		for (int i = 0; i < map.size(); i++) {
			Object obj= map.getAt(i);
			if (obj instanceof IASTName) {
				((IASTName) obj).setBinding(null);
			} else if (obj instanceof IBinding) {
				if (builtins == null) {
					builtins= new CharArrayObjectMap(2);
				}
				builtins.put(((IBinding) obj).getNameCharArray(), obj);
			}
		}
		mapsToNameOrBinding[0]= builtins == null ? CharArrayObjectMap.EMPTY_MAP : builtins;

		map= mapsToNameOrBinding[1];
		builtins= null;
		for (int i = 0; i < map.size(); i++) {
			Object obj= map.getAt(i);
			if (obj instanceof IASTName) {
				((IASTName) obj).setBinding(null);
			} else if (obj instanceof IBinding) {
				if (builtins == null) {
					builtins= new CharArrayObjectMap(2);
				}
				builtins.put(((IBinding) obj).getNameCharArray(), obj);
			}
		}
		mapsToNameOrBinding[1]= builtins == null ? CharArrayObjectMap.EMPTY_MAP : builtins;
		reuseBindings= null;
		isFullyCached = false;
	}

	public void addBinding(IBinding binding) {
		int type = NAMESPACE_TYPE_OTHER;
        if (binding instanceof ICompositeType || binding instanceof IEnumeration) {
            type = NAMESPACE_TYPE_TAG;
        }
            
        CharArrayObjectMap map = mapsToNameOrBinding[type];
		if( map == CharArrayObjectMap.EMPTY_MAP )
           map= mapsToNameOrBinding[type] = new CharArrayObjectMap(2);
        
		map.put(binding.getNameCharArray(), binding);
	}
	
	public void clearBindingsToReuse() {
		reuseBindings= null;
	}
}
