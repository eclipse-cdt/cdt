/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.core.runtime.Assert;

/**
 * Common base class for all sorts of c++ names: unqualified, qualified, operator and conversion
 * names plus template-ids
 */
public abstract class AbstractCPPASTName extends ASTNode implements IASTName {

	/**
	 * For test-purposes, only.
	 */
	public static boolean sAllowRecursionBindings = true;
	private static final byte MAX_RESOLUTION_DEPTH= 6;

	protected final static class RecursionResolvingBinding extends ProblemBinding {
		public RecursionResolvingBinding(IASTName node) {
			super(node, IProblemBinding.SEMANTIC_RECURSION_IN_LOOKUP, node.toCharArray());
			Assert.isTrue(sAllowRecursionBindings, getMessage());
		}
	}
	
	/**
	 * Helper method to resolve intermediate bindings without casting the name.
	 */
	public static IBinding resolveIntermediateBinding(IASTName name) {
		if (name == null)
			return null;
		if (name instanceof AbstractCPPASTName)
			return ((AbstractCPPASTName) name).resolveIntermediateBinding();
		return name.resolveBinding();
	}

	/**
	 * Helper method to get intermediate bindings without casting the name.
	 */
	public static IBinding getIntermediateBinding(IASTName name) {
		if (name == null)
			return null;
		if (name instanceof AbstractCPPASTName)
			return ((AbstractCPPASTName) name).getIntermediateBinding();
		return name.resolveBinding();
	}

	private IBinding fBinding = null;
	private byte fResolutionDepth = 0;
	private boolean fIsFinal= false;

	public final void incResolutionDepth() {
		if (fBinding == null && ++fResolutionDepth > MAX_RESOLUTION_DEPTH) {
			fBinding = new RecursionResolvingBinding(this);
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
	public IBinding resolveIntermediateBinding() {
    	if (fBinding == null) {
    		if (++fResolutionDepth > MAX_RESOLUTION_DEPTH) {
    			fBinding= new RecursionResolvingBinding(this);
    		} else {
    			fBinding= createIntermediateBinding();
    		}
    	}
    	return fBinding;
	}
	
    public IBinding resolveBinding() {
    	if (fBinding == null) {
    		if (++fResolutionDepth > MAX_RESOLUTION_DEPTH) {
    			fBinding= new RecursionResolvingBinding(this);
    		} else {
    			fBinding= createIntermediateBinding();
    		}
    	}
    	if (!fIsFinal)
    		resolveFinalBinding();
    	
    	return fBinding;
    }

    /**
     * If this name has not yet been resolved at all, <code>null</code> will be returned.
     * Otherwise the intermediate or final binding for this name is returned.
     * @see ICPPTwoPhaseBinding
     */
    public IBinding getIntermediateBinding() {
    	final IBinding cand= fBinding;
        if (cand == null)
        	return null;
        
        return fBinding;
    }

    public IBinding getBinding() {
    	final IBinding cand= fBinding;
        if (cand == null)
        	return null;
        
        if (!fIsFinal)
        	resolveFinalBinding();
        
        return fBinding;
    }

	private void resolveFinalBinding() {
		if (fBinding instanceof ICPPTwoPhaseBinding) {
    		ICPPTwoPhaseBinding lazyBinding= (ICPPTwoPhaseBinding) fBinding;
    		if (++fResolutionDepth > MAX_RESOLUTION_DEPTH) {
    			fBinding= new RecursionResolvingBinding(this);
    		} else {
    			IBinding finalBinding= lazyBinding.resolveFinalBinding();
    			assert finalBinding.getClass().equals(lazyBinding.getClass());
    			
    			fBinding= finalBinding;
    		}
    	}
		fIsFinal= true;
	}
	
	public void setBinding(IBinding binding) {
		fBinding= binding;
		fResolutionDepth= 0;
	}
	
	public IASTName getLastName() {
		return this;
	}
}
