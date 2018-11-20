/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumerationSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTypeSpecialization;

/**
 * Represents parameters and state of template instantiation.
 */
public final class InstantiationContext {
	private CPPTemplateParameterMap parameterMap;
	private int packOffset;
	private final ICPPSpecialization contextSpecialization;
	private boolean expandPack;
	private boolean packExpanded;

	// During the instantiation of a function body, stores mapping from local variables
	// (including function parameters) to their instantiated versions.
	// TODO(nathanridge): Get rid of this, replacing it with something analogous to
	// ICPPClassSpecialization.specializeMember() but for function scopes.
	private Map<IBinding, IBinding> fInstantiatedLocals = null;

	/**
	 * @param parameterMap mapping of template parameters to arguments, may be {@code null}.
	 * @param packOffset parameter pack offset, or -1 if expansion of a parameter pack is not desired pack.
	 * @param contextSpecialization the specialization if instantiation happens inside a specialized
	 *     type or function, otherwise {@code null}.
	 */
	public InstantiationContext(ICPPTemplateParameterMap parameterMap, int packOffset,
			ICPPSpecialization contextSpecialization) {
		this.parameterMap = (CPPTemplateParameterMap) parameterMap;
		this.packOffset = packOffset;
		this.contextSpecialization = contextSpecialization;
	}

	/**
	 * @param parameterMap mapping of template parameters to arguments, may be {@code null}.
	 * @param contextSpecialization the specialization if instantiation happens inside a specialized
	 *     type or function, otherwise {@code null}.
	 */
	public InstantiationContext(ICPPTemplateParameterMap parameterMap, ICPPSpecialization contextSpecialization) {
		this(parameterMap, -1, contextSpecialization);
	}

	/**
	 * @param parameterMap mapping of template parameters to arguments, may be {@code null}.
	 * @param packOffset parameter pack offset, or -1 if not known
	 */
	public InstantiationContext(ICPPTemplateParameterMap parameterMap, int packOffset) {
		this(parameterMap, packOffset, null);
	}

	/**
	 * @param parameterMap mapping of template parameters to arguments, may be {@code null}.
	 */
	public InstantiationContext(ICPPTemplateParameterMap parameterMap) {
		this(parameterMap, -1, null);
	}

	/**
	 * Returns the mapping of template parameters to arguments, possibly {@code null} if the context doesn't
	 * contain it.
	 */
	public ICPPTemplateParameterMap getParameterMap() {
		return parameterMap;
	}

	/**
	 * Adds a parameter mapping.
	 */
	public void addToParameterMap(ICPPTemplateParameter par, ICPPTemplateArgument arg) {
		if (parameterMap == null) {
			parameterMap = new CPPTemplateParameterMap(1);
		}
		parameterMap.put(par, arg);
	}

	/**
	 * Adds a parameter mapping.
	 */
	public void addToParameterMap(ICPPTemplateParameter par, ICPPTemplateArgument[] args) {
		if (parameterMap == null) {
			parameterMap = new CPPTemplateParameterMap(1);
		}
		parameterMap.put(par, args);
	}

	/**
	 * Puts all mappings from the supplied map into the parameter map of the context.
	 */
	public void addToParameterMap(ICPPTemplateParameterMap toAdd) {
		if (parameterMap == null) {
			parameterMap = new CPPTemplateParameterMap((CPPTemplateParameterMap) toAdd);
		} else {
			parameterMap.putAll(toAdd);
		}
	}

	/**
	 * Returns the specialization if instantiation happens inside a specialized type or function, otherwise
	 * {@code null}
	 */
	public final ICPPSpecialization getContextSpecialization() {
		return contextSpecialization;
	}

	/**
	 * Returns the type specialization if instantiation happens inside a specialized type, otherwise
	 * {@code null}.
	 */
	public final ICPPTypeSpecialization getContextTypeSpecialization() {
		return contextSpecialization instanceof ICPPTypeSpecialization ? (ICPPTypeSpecialization) contextSpecialization
				: null;
	}

	/**
	 * Returns the class specialization if instantiation happens inside a specialized class, otherwise
	 * {@code null}.
	 */
	public ICPPClassSpecialization getContextClassSpecialization() {
		return getContextClassSpecialization(contextSpecialization);
	}

	/**
	 * Returns {@code true} if the pack offset is specified.
	 */
	public boolean hasPackOffset() {
		return packOffset != -1;
	}

	/**
	 * Returns the pack offset if specified, otherwise -1.
	 */
	public int getPackOffset() {
		return packOffset;
	}

	/**
	 * Sets the pack offset.
	 */
	public void setPackOffset(int packOffset) {
		this.packOffset = packOffset;
	}

	/**
	 * Returns {@code true} if a parameter pack should be expanded by substituting individual template
	 * arguments in place of a template parameter that represents a pack.
	 */
	public boolean shouldExpandPack() {
		return expandPack;
	}

	/**
	 * Sets the flag that indicates that a parameter pack should be expanded by substituting individual
	 * template arguments in place of a template parameter that represents a pack.
	 */
	public void setExpandPack(boolean expand) {
		this.expandPack = expand;
	}

	/**
	 * Returns {@code true} if individual template argument substitution in place of a template parameter that
	 * represents a pack actually happened.
	 */
	public boolean isPackExpanded() {
		return packExpanded;
	}

	/**
	 * Indicates that individual template argument substitution in place of a template parameter that
	 * represents a pack actually happened.
	 */
	public void setPackExpanded(boolean expanded) {
		this.packExpanded = expanded;
	}

	/**
	 * @see ICPPTemplateParameterMap#getArgument(ICPPTemplateParameter, int)
	 */
	public ICPPTemplateArgument getArgument(ICPPTemplateParameter param) {
		return parameterMap == null ? null : parameterMap.getArgument(param, packOffset);
	}

	/**
	 * @see ICPPTemplateParameterMap#getPackExpansion(ICPPTemplateParameter)
	 */
	public ICPPTemplateArgument[] getPackExpansion(ICPPTemplateParameter param) {
		return parameterMap == null ? null : parameterMap.getPackExpansion(param);
	}

	/**
	 * Returns the class specialization that the given binding is or is owned by, otherwise {@code null}.
	 */
	public static ICPPClassSpecialization getContextClassSpecialization(IBinding owner) {
		if (owner instanceof ICPPEnumerationSpecialization)
			owner = owner.getOwner();
		if (owner instanceof ICPPClassSpecialization)
			return (ICPPClassSpecialization) owner;
		return null;
	}

	public void putInstantiatedLocal(IBinding local, IBinding instantiatedLocal) {
		if (fInstantiatedLocals == null) {
			fInstantiatedLocals = new HashMap<>();
		}
		fInstantiatedLocals.put(local, instantiatedLocal);
	}

	public IBinding getInstantiatedLocal(IBinding local) {
		return fInstantiatedLocals == null ? null : fInstantiatedLocals.get(local);
	}

}
