/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalNameOwner;
import org.eclipse.cdt.internal.core.dom.parser.IRecursionResolvingBinding;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.core.runtime.Assert;

/**
 * Common base class for all sorts of c++ names: unqualified, qualified, operator and conversion
 * names plus template-ids
 */
public abstract class CPPASTNameBase extends ASTNode implements ICPPASTName {
	protected static final Pattern WHITESPACE_SEQ = Pattern.compile("\\s+"); //$NON-NLS-1$

	/**
	 * For test-purposes, only.
	 */
	public static boolean sAllowRecursionBindings = true;
	public static boolean sAllowNameComputation = true;
	private static final byte MAX_RESOLUTION_DEPTH= 6;

	protected final static class RecursionResolvingBinding extends ProblemBinding implements IRecursionResolvingBinding {
		public RecursionResolvingBinding(IASTName node, char[] arg) {
			super(node, IProblemBinding.SEMANTIC_RECURSION_IN_LOOKUP, arg);
			Assert.isTrue(sAllowRecursionBindings, getMessage());
		}
	}

	private RecursionResolvingBinding createRecursionResolvingBinding() {
		// We create a recursion resolving binding when the resolution depth
		// exceeds MAX_RESOLUTION_DEPTH. If the resolution depth exceeds
		// MAX_RESOLUTION_DEPTH + 1, it means that attempting to create the
		// recursion resolving binding has led us back to trying to resolve
		// the binding for this name again, so the recursion isn't broken.
		// This can happen because the constructor of RecursionResolvingBinding
		// calls ProblemBinding.getMessage(), which can try to do name
		// resolution to build an argument string if one wasn't provided in the
		// ProblemBinding constructor. To break the recursion in a case
		// like, this we provide the argument string "(unknown)" instead.
		char[] args = (fResolutionDepth > MAX_RESOLUTION_DEPTH + 1) ? "(unknown)".toCharArray() : null;  //$NON-NLS-1$
		return new RecursionResolvingBinding(this, args);
	}

	private IBinding fBinding;
	private byte fResolutionDepth;
	private boolean fIsFinal;

	public final void incResolutionDepth() {
		if (fBinding == null && ++fResolutionDepth > MAX_RESOLUTION_DEPTH) {
			setBinding(createRecursionResolvingBinding());
		}
	}

	/**
	 * Called to perform the binding resolution. Subclasses may return lazy bindings that
	 * will not be exposed within public API.
	 */
	protected abstract IBinding createIntermediateBinding();

	/**
	 * Resolves the name at least up to the intermediate binding and returns it.
	 * @see ICPPTwoPhaseBinding
	 */
	@Override
	public IBinding resolvePreBinding() {
    	if (fBinding == null) {
    		if (++fResolutionDepth > MAX_RESOLUTION_DEPTH) {
    			setBinding(createRecursionResolvingBinding());
    		} else {
    			setBinding(createIntermediateBinding());
    		}
    	}
    	return fBinding;
	}

	private IBinding resolveBindingPromiscuous() {
		// If we haven't already resolved the non-promiscuous binding, do so now.
		if (fBinding == null) {
			try {
				CPPSemantics.disablePromiscuousBindingResolution();
				resolveBindingNormal();
			} finally {
				CPPSemantics.enablePromiscuousBindingResolution();
			}
		}

		// If the non-promiscuous binding is not a ProblemBinding, the promiscuous
		// binding will be the same, so just return it.
		if (!(fBinding instanceof ProblemBinding)) {
			return fBinding;
		}

		// Otherwise, the non-promiscuous binding is a ProblemBinding.
		ProblemBinding problem = (ProblemBinding) fBinding;
		IBinding promiscuous = problem.getPromiscuousBinding();
		if (promiscuous == null) {
			// Clear the cached ProblemBinding.
			setBinding(null);

			// Resolve the promiscuous binding, and store it in the ProblemBinding.
			promiscuous = resolveBindingNormal();
			problem.setPromiscuousBinding(promiscuous);
			
			// Restore the cached ProblemBinding.
			setBinding(problem);
		}
		return promiscuous;
	}
	
    private IBinding resolveBindingNormal() {	
    	if (fBinding == null) {
    		if (++fResolutionDepth > MAX_RESOLUTION_DEPTH) {
    			setBinding(createRecursionResolvingBinding());
    		} else {
    			fIsFinal= false;
    			final IBinding b= createIntermediateBinding();
    			if (b instanceof ProblemBinding) {
    				ProblemBinding pb= (ProblemBinding) b;
    				final IASTNode node= pb.getASTNode();
    				if (node == null || node.getParent() == null) {
    					pb.setASTNode(this);
    				}
    			}
    			setBinding(b);
    		}
    	}
    	if (!fIsFinal)
    		resolveFinalBinding(this);

    	return fBinding;
    }
	
    @Override
	public IBinding resolveBinding() {
    	return CPPSemantics.isUsingPromiscuousBindingResolution()
    		? resolveBindingPromiscuous()
    	    : resolveBindingNormal();
    }
    
    /**
     * If this name has not yet been resolved at all, <code>null</code> will be returned.
     * Otherwise the intermediate or final binding for this name is returned.
     * @see ICPPTwoPhaseBinding
     */
    @Override
	public IBinding getPreBinding() {
        return fBinding;
    }

    /**
     * If this name has not yet been resolved at all, <code>null</code> will be returned.
     * Otherwise the final binding for this name is returned.
     * @see ICPPTwoPhaseBinding
     */
    @Override
	public IBinding getBinding() {
    	final IBinding cand= fBinding;
        if (cand == null)
        	return null;

        if (!fIsFinal)
        	resolveFinalBinding(this);

        return fBinding;
    }

	private void resolveFinalBinding(CPPASTNameBase astName) {
		if (fBinding instanceof ICPPTwoPhaseBinding) {
    		ICPPTwoPhaseBinding intermediateBinding= (ICPPTwoPhaseBinding) fBinding;
    		if (++fResolutionDepth > MAX_RESOLUTION_DEPTH) {
    			setBinding(createRecursionResolvingBinding());
    		} else {
    			setBinding(intermediateBinding.resolveFinalBinding(astName));
    		}
    	}
		fIsFinal= true;
	}

	@Override
	public void setBinding(IBinding binding) {
		fBinding= binding;
		fResolutionDepth= 0;
	}

	@Override
	public IASTName getLastName() {
		return this;
	}

	@Override
	public boolean isQualified() {
		IASTNode parent= getParent();
		if (parent instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName qn= (ICPPASTQualifiedName) parent;
			if (qn.isFullyQualified())
				return true;
			ICPPASTNameSpecifier[] qualifier = qn.getQualifier();
			if (qualifier.length > 0 && qualifier[0] == this)
				return false;
			return true;
		}
		return false;
	}

	@Override
	public final String toString() {
		return new String(toCharArray());
	}

	@Override
	public IASTCompletionContext getCompletionContext() {
        IASTNode node = getParent();
    	while (node != null) {
    		if (node instanceof IASTCompletionContext) {
    			return (IASTCompletionContext) node;
    		}
    		node = node.getParent();
    	}

    	return null;
	}

	@Override
	public int getRoleOfName(boolean allowResolution) {
        IASTNode parent = getParent();
        if (parent instanceof IASTInternalNameOwner) {
        	return ((IASTInternalNameOwner) parent).getRoleForName(this, allowResolution);
        }
        if (parent instanceof IASTNameOwner) {
            return ((IASTNameOwner) parent).getRoleForName(this);
        }
        return IASTNameOwner.r_unclear;
	}

    @Override
	public boolean isDeclaration() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            switch (role) {
            case IASTNameOwner.r_reference:
            case IASTNameOwner.r_unclear:
                return false;
            default:
                return true;
            }
        }
        return false;
    }

    @Override
	public boolean isReference() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            return role == IASTNameOwner.r_reference;
        }
        return false;
    }

    @Override
	public boolean isDefinition() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            return role == IASTNameOwner.r_definition;
        }
        return false;
    }

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
}
