/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.LookupData;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Base class for c++-scopes of the ast.
 */
abstract public class CPPScope implements ICPPScope, ICPPASTInternalScope {
	private static final IProgressMonitor NPM = new NullProgressMonitor();
    private IASTNode physicalNode;
	private boolean isCached = false;
	protected CharArrayObjectMap bindings = null;

	public static class CPPScopeProblem extends ProblemBinding implements ICPPScope {
        public CPPScopeProblem(IASTNode node, int id, char[] arg) {
            super(node, id, arg);
        }
        public CPPScopeProblem(IASTName name, int id) {
            super(name, id);
        }
    }

	public CPPScope(IASTNode physicalNode) {
		this.physicalNode = physicalNode;
	}

	public IScope getParent() throws DOMException {
		return CPPVisitor.getContainingNonTemplateScope(physicalNode);
	}

	public IASTNode getPhysicalNode() {
		return physicalNode;
	}

	@SuppressWarnings("unchecked")
	public void addName(IASTName name) throws DOMException {
		if (bindings == null)
			bindings = new CharArrayObjectMap(1);
		if (name instanceof ICPPASTQualifiedName) {
			if (!(physicalNode instanceof ICPPASTCompositeTypeSpecifier) &&
					!(physicalNode instanceof ICPPASTNamespaceDefinition)) {
				return;
			}

			// name belongs to a different scope, don't add it here except it names this scope
		    if (!canDenoteScopeMember((ICPPASTQualifiedName) name))
		    	return;
		} 
		final char[] c= name.getLookupKey();
		if (c.length == 0)
			return;
		Object o = bindings.get(c);
		if (o != null) {
		    if (o instanceof ObjectSet) {
		    	((ObjectSet<Object>)o).put(name);
		    } else {
		    	ObjectSet<Object> temp = new ObjectSet<Object>(2);
		    	temp.put(o);
		    	temp.put(name);
		        bindings.put(c, temp);
		    }
		} else {
		    bindings.put(c, name);
		}
	}

	public boolean canDenoteScopeMember(ICPPASTQualifiedName name) {
		IScope scope= this;
		IASTName[] na= name.getNames();
		try {
			for (int i= na.length - 2; i >= 0; i--) {
				if (scope == null) 
					return false;
				IName scopeName = scope.getScopeName();
				if (scopeName == null)
					return false;
				
				if (!CharArrayUtils.equals(scopeName.getSimpleID(), na[i].getSimpleID())) 
					return false;
				scope= scope.getParent();
			}
			if (!name.isFullyQualified() || scope == null) {
				return true;
			}
			return ASTInternal.getPhysicalNodeOfScope(scope) instanceof IASTTranslationUnit;
		} catch (DOMException e) {
			return false;
		}
	}

	public IBinding getBinding(IASTName name, boolean forceResolve, IIndexFileSet fileSet) throws DOMException {
		IBinding binding= getBindingInAST(name, forceResolve);
		if (binding == null && forceResolve) {
			final IASTTranslationUnit tu = name.getTranslationUnit();
			IIndex index = tu == null ? null : tu.getIndex();
			if (index != null) {
				final char[] nchars = name.getLookupKey();
				// Try looking this up in the PDOM
				if (physicalNode instanceof IASTTranslationUnit) {
					try {
						IBinding[] bindings= index.findBindings(nchars, 
								IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE, NPM);
						if (fileSet != null) {
							bindings= fileSet.filterFileLocalBindings(bindings);
						}
						binding= CPPSemantics.resolveAmbiguities(name, bindings);
			        	if (binding instanceof ICPPUsingDeclaration) {
			        		binding= CPPSemantics.resolveAmbiguities(name,
			        				((ICPPUsingDeclaration)binding).getDelegates());
			        	}
					} catch (CoreException e) {
		        		CCorePlugin.log(e);
					}
				} else if (physicalNode instanceof ICPPASTNamespaceDefinition) {
					ICPPASTNamespaceDefinition nsdef = (ICPPASTNamespaceDefinition)physicalNode;
					IASTName nsname = nsdef.getName();
					IBinding nsbinding= nsname.resolveBinding();
					if (nsbinding instanceof ICPPNamespace) {
						ICPPNamespace nsbindingAdapted = (ICPPNamespace) index.adaptBinding(nsbinding);
						if (nsbindingAdapted!=null) {
							return nsbindingAdapted.getNamespaceScope().getBinding(name, forceResolve, fileSet);
						}
					}
				}
			}
		}
		return binding;
	}

	public IBinding getBindingInAST(IASTName name, boolean forceResolve) throws DOMException {
		IBinding[] bs= getBindingsInAST(name, forceResolve, false, false, false);
		return CPPSemantics.resolveAmbiguities(name, bs);
	}

	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) throws DOMException {
		return getBindings(name, resolve, prefixLookup, fileSet, true);
	}
	
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet,
			boolean checkPointOfDecl) throws DOMException {
		IBinding[] result = getBindingsInAST(name, resolve, prefixLookup, checkPointOfDecl, true);
		final IASTTranslationUnit tu = name.getTranslationUnit();
		if (tu != null) {
			IIndex index = tu.getIndex();
			if (index != null) {
				if (physicalNode instanceof IASTTranslationUnit) {
					try {
						IndexFilter filter = IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE;
						final char[] nchars = name.getLookupKey();
						IBinding[] bindings = prefixLookup ?
								index.findBindingsForPrefix(nchars, true, filter, null) :
								index.findBindings(nchars, filter, null);
						if (fileSet != null) {
							bindings= fileSet.filterFileLocalBindings(bindings);
						}
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
							if (fileSet != null) {
								bindings= fileSet.filterFileLocalBindings(bindings);
							}
							result = (IBinding[]) ArrayUtil.addAll(IBinding.class, result, bindings);
						}
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			}
		}

		return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	}

	public IBinding[] getBindingsInAST(IASTName name, boolean forceResolve, boolean prefixLookup, 
			boolean checkPointOfDecl, boolean expandUsingDirectives) throws DOMException {
		populateCache();
	    final char[] c = name.getLookupKey();
	    IBinding[] result = null;
	    
	    Object[] obj = null;
	    if (prefixLookup) {
	    	Object[] keys = bindings != null ? bindings.keyArray() : new Object[0];
	    	for (int i = 0; i < keys.length; i++) {
	    		final char[] key = (char[]) keys[i];
	    		if (CharArrayUtils.equals(key, 0, c.length, c, true)) {
	    			obj = ArrayUtil.append(obj, bindings.get(key));
	    		}
	    	}
	    } else {
	    	obj = bindings != null ? new Object[] {bindings.get(c)} : null;
	    }
	    
	    obj = ArrayUtil.trim(Object.class, obj);
	    for (Object element : obj) {
	        if (element instanceof ObjectSet<?>) {
	        	ObjectSet<?> os= (ObjectSet<?>) element;
        		for (int j = 0; j < os.size(); j++) {
        			result= addCandidate(os.keyAt(j), name, forceResolve, checkPointOfDecl, expandUsingDirectives, result);
        		}
	        } else {
	        	result = addCandidate(element, name, forceResolve, checkPointOfDecl, expandUsingDirectives, result);
	        }
	    }
	    return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	}

	private IBinding[] addCandidate(Object candidate, IASTName name, boolean forceResolve, 
			boolean checkPointOfDecl, boolean expandUsingDirectives, IBinding[] result) {
		if (checkPointOfDecl) {
			IASTTranslationUnit tu= name.getTranslationUnit();
			if (!CPPSemantics.declaredBefore(candidate, name, tu != null && tu.getIndex() != null)) {
				if (!(this instanceof ICPPClassScope) || ! LookupData.checkWholeClassScope(name))
					return result;
			}
		}

		IBinding binding;
		if (candidate instanceof IASTName) {
			final IASTName candName= (IASTName) candidate;
			if (forceResolve && candName != name && candName != name.getParent()) {
				binding = candName.resolvePreBinding();
			} else {
				binding = candName.getLastName().getBinding();
			}
		} else {
			binding= (IBinding) candidate;
		}

		if (expandUsingDirectives && binding instanceof ICPPUsingDeclaration) {
			IBinding[] delegates = ((ICPPUsingDeclaration) binding).getDelegates();
			result= (IBinding[]) ArrayUtil.addAll(IBinding.class, result, delegates);
		} else {
			result = (IBinding[]) ArrayUtil.append(IBinding.class, result, binding);
		}
		return result;
	}
	
	public final void populateCache() {
		if (!isCached) {
			CPPSemantics.populateCache(this);
			isCached= true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public IBinding[] find(String name) throws DOMException {
	    return CPPSemantics.findBindings(this, name, false);
	}

	@SuppressWarnings("unchecked")
    public void addBinding(IBinding binding) {
        if (bindings == null)
            bindings = new CharArrayObjectMap(1);
        char[] c = binding.getNameCharArray();
        if (c.length == 0) {
        	return;
        }
        Object o = bindings.get(c);
        if (o != null) {
            if (o instanceof ObjectSet) {
                ((ObjectSet<Object>)o).put(binding);
            } else {
                ObjectSet<Object> set = new ObjectSet<Object>(2);
                set.put(o);
                set.put(binding);
                bindings.put(c, set);
            }
        } else {
            bindings.put(c, binding);
        }
    }

	public final IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		return getBinding(name, resolve, IIndexFileSet.EMPTY);
	}

	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) throws DOMException {
		return getBindings(name, resolve, prefix, IIndexFileSet.EMPTY, true);
	}

	public IName getScopeName() throws DOMException {
		return null;
	}

	@Override
	public String toString() {
		IName name = null;
		try {
			name = getScopeName();
		} catch (DOMException e) {
		}
		
		final String n= name != null ? name.toString() : "<unnamed scope>"; //$NON-NLS-1$
		return getKind().toString() + ' ' + n + ' ' + '(' + super.toString() + ')';
	}
}
