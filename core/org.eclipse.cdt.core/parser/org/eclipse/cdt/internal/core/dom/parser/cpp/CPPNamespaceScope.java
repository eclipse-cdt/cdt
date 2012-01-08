/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPScopeMapper.InlineNamespaceDirective;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.core.runtime.CoreException;

/**
 * Implementation of namespace scopes, including global scope.
 */
public class CPPNamespaceScope extends CPPScope implements ICPPInternalNamespaceScope {
	private static final ICPPInternalNamespaceScope[] NO_NAMESPACE_SCOPES = new ICPPInternalNamespaceScope[0];

	private List<ICPPUsingDirective> fUsingDirectives = null;

	private boolean fIsInline;
	private boolean fIsInlineInitialized;
	private ICPPNamespaceScope[] fEnclosingNamespaceSet;
	private List<ICPPASTNamespaceDefinition> fInlineNamespaceDefinitions;
	private ICPPInternalNamespaceScope[] fInlineNamespaces;
	
	public CPPNamespaceScope(IASTNode physicalNode) {
		super(physicalNode);
	}

	@Override
	public EScopeKind getKind() {
		if (getPhysicalNode() instanceof IASTTranslationUnit)
			return  EScopeKind.eGlobal;
		
		return EScopeKind.eNamespace;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope#getUsingDirectives()
	 */
	@Override
	public ICPPUsingDirective[] getUsingDirectives() {
		initUsingDirectives();
		populateCache();
		return fUsingDirectives.toArray(new ICPPUsingDirective[fUsingDirectives.size()]);
	}
	
	private void initUsingDirectives() {
		if (fUsingDirectives == null) {
			fUsingDirectives= new ArrayList<ICPPUsingDirective>(1);
			// Insert a using directive for every inline namespace found in the index
			for (ICPPInternalNamespaceScope inline: getIndexInlineNamespaces()) {
				if (!(inline instanceof CPPNamespaceScope)) {
					fUsingDirectives.add(new InlineNamespaceDirective(this, inline));
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope#addUsingDirective(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective)
	 */
	@Override
	public void addUsingDirective(ICPPUsingDirective directive) {
		initUsingDirectives();
		fUsingDirectives.add(directive);
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getScopeName()
     */
    @Override
	public IName getScopeName() {
        IASTNode node = getPhysicalNode();
        if (node instanceof ICPPASTNamespaceDefinition) {
            return ((ICPPASTNamespaceDefinition) node).getName();
        }
        return null;
    }

    public IScope findNamespaceScope(IIndexScope scope) {
    	final String[] qname= CPPVisitor.getQualifiedName(scope.getScopeBinding());
    	final IScope[] result= {null};
    	final ASTVisitor visitor= new ASTVisitor() {
    		private int depth= 0;
    		{
    			shouldVisitNamespaces= shouldVisitDeclarations= true;
    		}
    		@Override
    		public int visit( IASTDeclaration declaration ){
    			if (declaration instanceof ICPPASTLinkageSpecification)
    				return PROCESS_CONTINUE;
    			return PROCESS_SKIP;
    		}
    		@Override
    		public int visit(ICPPASTNamespaceDefinition namespace) {
    			final String name = namespace.getName().toString();
    			if (name.length() == 0) {
    				return PROCESS_CONTINUE;
    			}
    			if (qname[depth].equals(name)) {
    				if (++depth == qname.length) {
    					result[0]= namespace.getScope();
    					return PROCESS_ABORT;
    				}
    				return PROCESS_CONTINUE;
    			}
    			return PROCESS_SKIP;
    		}
    		@Override
    		public int leave(ICPPASTNamespaceDefinition namespace) {
    			if (namespace.getName().getLookupKey().length > 0) {
    				--depth;
    			}
    			return PROCESS_CONTINUE;
    		}
    	};
    	getPhysicalNode().accept(visitor);
    	return result[0];
    }

	@Override
	public boolean isInlineNamepace() {
		if (!fIsInlineInitialized) {
			fIsInline= computeIsInline();
			fIsInlineInitialized= true;
		}
		return fIsInline;
	}

	public boolean computeIsInline() {
		final IASTNode node= getPhysicalNode();
		if (!(node instanceof ICPPASTNamespaceDefinition)) {
			return false;
		}

		if (((ICPPASTNamespaceDefinition) node).isInline())
			return true;
		
		IASTTranslationUnit tu= node.getTranslationUnit();
		if (tu != null) {
			final IIndex index= tu.getIndex();
			IIndexFileSet fileSet= tu.getASTFileSet();
			if (index != null && fileSet != null) {
				fileSet= fileSet.invert();
				ICPPNamespace nsBinding = getNamespaceIndexBinding(index);
				if (nsBinding != null && nsBinding.isInline()) {
					try {
						IIndexName[] names = index.findDefinitions(nsBinding);
						for (IIndexName name : names) {
							if (name.isInlineNamespaceDefinition() && fileSet.contains(name.getFile())) {
								return true;
							}
						}
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			}
		}
		return false;
	}

	@Override
	public ICPPNamespaceScope[] getEnclosingNamespaceSet() {
		if (fEnclosingNamespaceSet == null) {
			return fEnclosingNamespaceSet= computeEnclosingNamespaceSet(this);
		}
		return fEnclosingNamespaceSet;
	}
	
	@Override
	public ICPPInternalNamespaceScope[] getInlineNamespaces() {
		if (getKind() == EScopeKind.eLocal)
			return NO_NAMESPACE_SCOPES;
		
		if (fInlineNamespaces == null) {
			fInlineNamespaces= computeInlineNamespaces();
		}
		return fInlineNamespaces;
	}
	
	ICPPInternalNamespaceScope[] computeInlineNamespaces() {
		populateCache();
		Set<ICPPInternalNamespaceScope> result= null;
		if (fInlineNamespaceDefinitions != null) {
			result= new HashSet<ICPPInternalNamespaceScope>(fInlineNamespaceDefinitions.size());
			for (ICPPASTNamespaceDefinition nsdef : fInlineNamespaceDefinitions) {
				final IScope scope = nsdef.getScope();
				if (scope instanceof ICPPInternalNamespaceScope) {
					result.add((ICPPInternalNamespaceScope) scope);
				}
			}
		}
		
		for (ICPPInternalNamespaceScope inline : getIndexInlineNamespaces()) {
			if (result == null)
				result = new HashSet<ICPPInternalNamespaceScope>();
			result.add(inline);
		}
		
		if (result == null) {
			return NO_NAMESPACE_SCOPES;
		}
		return result.toArray(new ICPPInternalNamespaceScope[result.size()]);
	}

	private ICPPInternalNamespaceScope[] getIndexInlineNamespaces() {
		IASTTranslationUnit tu= getPhysicalNode().getTranslationUnit();
		if (tu instanceof CPPASTTranslationUnit) { 
			CPPASTTranslationUnit cpptu= (CPPASTTranslationUnit) tu;
			IIndex index= tu.getIndex();
			if (index != null) {
				IScope[] inlineScopes= null;
				ICPPNamespace namespace= getNamespaceIndexBinding(index);
				try {
					if (namespace != null) {
						ICPPNamespaceScope scope = namespace.getNamespaceScope();
						inlineScopes= scope.getInlineNamespaces();
					} else if (getKind() == EScopeKind.eGlobal) {
						inlineScopes= index.getInlineNamespaces();
					}
				} catch (CoreException e) {
				}
				if (inlineScopes != null) {
					List<ICPPInternalNamespaceScope> result= null;
					for (IScope scope : inlineScopes) {
						if (scope instanceof IIndexScope) {
							scope= cpptu.mapToASTScope((IIndexScope) scope);
						}
						if (scope instanceof ICPPInternalNamespaceScope) {
							if (result == null) {
								result= new ArrayList<ICPPInternalNamespaceScope>();
							}
							result.add((ICPPInternalNamespaceScope) scope);
						}
					}
					if (result != null) {
						return result.toArray(new ICPPInternalNamespaceScope[result.size()]);
					}
				}
			}
		}
		return NO_NAMESPACE_SCOPES;
	}

	/**
	 * Called while populating scope.
	 */
	public void addInlineNamespace(ICPPASTNamespaceDefinition nsDef) {
		if (fInlineNamespaceDefinitions == null) {
			fInlineNamespaceDefinitions= new ArrayList<ICPPASTNamespaceDefinition>();
		}
		fInlineNamespaceDefinitions.add(nsDef);
	}

	
	public static ICPPNamespaceScope[] computeEnclosingNamespaceSet(ICPPInternalNamespaceScope nsScope) {
		if (nsScope.isInlineNamepace()) {
			try {
				IScope parent= nsScope.getParent();
				if (parent instanceof ICPPInternalNamespaceScope) {
					return ((ICPPInternalNamespaceScope) parent).getEnclosingNamespaceSet();
				}
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}
		
		Set<ICPPInternalNamespaceScope> result= new HashSet<ICPPInternalNamespaceScope>();
		result.add(nsScope);
		addInlineNamespaces(nsScope, result);
		return result.toArray(new ICPPNamespaceScope[result.size()]);
	}

	private static void addInlineNamespaces(ICPPInternalNamespaceScope nsScope, Set<ICPPInternalNamespaceScope> result) {
		ICPPInternalNamespaceScope[] inlineNss = nsScope.getInlineNamespaces();
		for (ICPPInternalNamespaceScope inlineNs : inlineNss) {
			if (result.add(inlineNs)) {
				addInlineNamespaces(inlineNs, result);
			}
		}
	}
}
