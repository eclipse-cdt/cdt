/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPCompositeBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUsingDeclaration;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;

public abstract class CompositeScope implements IIndexScope {
	/**
	 * The factory used for obtaining further composite bindings
	 */
	protected final ICompositesFactory cf;
	/**
	 * The representative binding for this composite binding. Most cases are simple
	 * enough that this becomes a delegate, some need to use it as a search key over fragments,
	 * and some ignore it as a representative binding from each fragment is needed to meet interface
	 * contracts.
	 */
	protected final IIndexFragmentBinding rbinding;

	public CompositeScope(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		if (cf == null || rbinding == null)
			throw new NullPointerException();
		this.cf = cf;
		this.rbinding = rbinding;
	}
	
	@Override
	public IIndexScope getParent() {
		IIndexScope rscope = rbinding.getScope();
		if (rscope != null) {
			return cf.getCompositeScope(rscope);
		}
		return null;
	}

	@Override
	public IIndexName getScopeName() {
		if (rbinding instanceof IIndexScope)
			return ((IIndexScope) rbinding).getScopeName();
		if (rbinding instanceof ICPPClassType) 
			return (IIndexName) ((ICPPClassType) rbinding).getCompositeScope().getScopeName();
		return null;
	}

	protected final void fail() {
		throw new CompositingNotImplementedError();
	}
	
	
	public IBinding getRawScopeBinding() {
		return rbinding;
	}
	
	/**
	 * For bindings that are not known statically to be index bindings, we must decide how to
	 * process them by run-time type. This method processes a single binding accordingly.
	 * @param binding a binding from the fragment layer
	 * @return a suitable binding at the composite layer 
	 */
	protected final IBinding processUncertainBinding(IBinding binding) {
		if (binding instanceof IIndexFragmentBinding) {
			return cf.getCompositeBinding((IIndexFragmentBinding)binding);				
		} else if (binding instanceof ProblemBinding) {
			return binding;
		} else if (binding instanceof CPPCompositeBinding /* AST composite */) {
			return new CPPCompositeBinding(
				processUncertainBindings(((CPPCompositeBinding) binding).getBindings())
			);
		} else if (binding instanceof CPPUsingDeclaration) {
			return binding;
		} else if (binding == null) {
			return null;
		} else if (binding instanceof ICPPSpecialization) {
			return binding;
		}
		CCorePlugin.log("CompositeFactory unsure how to process: " + binding.getClass().getName()); //$NON-NLS-1$
		return binding;
	}
	
	/**
	 * A convenience method for processing an array of bindings with {@link CompositeScope#processUncertainBinding(IBinding)}
     * Returns an empty array if the input parameter is null
	 * @param frgBindings
	 * @return a non-null IBinding[] 
	 */
	protected final IBinding[] processUncertainBindings(IBinding[] frgBindings) {
		if (frgBindings != null) {
			IBinding[] result= new IBinding[frgBindings.length];
			for(int i= 0; i < result.length; i++) {
				result[i]= processUncertainBinding(frgBindings[i]);
			}
			return result;
		}
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public final IBinding getBinding(IASTName name, boolean resolve) {
		return getBinding(name, resolve, IIndexFileSet.EMPTY);
	}

	@Override
	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) {
		return getBindings(name, resolve, prefix, IIndexFileSet.EMPTY);
	}
	
	/**
	 * The c++-name resolution stores scopes in hash-maps, we need to make sure equality is detected
	 * in order to prevent infinite loops.
	 */
	@Override
	public final boolean equals(Object other) {
		if (other instanceof CompositeScope) {
			return rbinding.equals(((CompositeScope)other).rbinding);
		}
		return false;
	}
	
	/**
	 * The c++-name resolution stores scopes in hash-maps, we need to make sure equality is detected
	 * in order to prevent infinite loops.
	 */
	@Override
	public final int hashCode() {
		return rbinding.hashCode();
	}
}
