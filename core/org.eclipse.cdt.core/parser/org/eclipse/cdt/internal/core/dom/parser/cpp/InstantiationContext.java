/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumerationSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTypeSpecialization;

/**
 * Represents parameters and state of template instantiation.
 */
public class InstantiationContext {
	private CPPTemplateParameterMap parameterMap;
	private int packOffset;
	private final ICPPTypeSpecialization contextTypeSpecialization;
	private final IASTNode point;

	/**
	 * @param parameterMap mapping of template parameters to arguments, may be {@code null}.
	 * @param packOffset parameter pack offset, or -1 if expansion of a parameter pack is not desired
	 *     pack.
	 * @param contextTypeSpecialization the type specialization if instantiation happens inside a specialized
	 *     type, otherwise {@code null}.
	 * @param point the point of instantiation
	 */
	public InstantiationContext(ICPPTemplateParameterMap parameterMap, int packOffset,
			ICPPTypeSpecialization contextTypeSpecialization, IASTNode point) {
		this.parameterMap = (CPPTemplateParameterMap) parameterMap;
		this.packOffset = packOffset;
		this.contextTypeSpecialization = contextTypeSpecialization;
		this.point = point;
	}

	/**
	 * @param parameterMap mapping of template parameters to arguments, may be {@code null}.
	 * @param contextTypeSpecialization the type specialization if instantiation happens inside a specialized
	 *     type, otherwise {@code null}.
	 * @param point the point of instantiation
	 */
	public InstantiationContext(ICPPTemplateParameterMap parameterMap,
			ICPPTypeSpecialization contextTypeSpecialization, IASTNode point) {
		this(parameterMap, -1, contextTypeSpecialization, point);
	}

	/**
	 * @param parameterMap mapping of template parameters to arguments, may be {@code null}.
	 * @param packOffset parameter pack offset, or -1 if not known
	 * @param point the point of instantiation
	 */
	public InstantiationContext(ICPPTemplateParameterMap parameterMap, int packOffset, IASTNode point) {
		this(parameterMap, packOffset, null, point);
	}

	/**
	 * @param parameterMap mapping of template parameters to arguments, may be {@code null}.
	 * @param point the point of instantiation
	 */
	public InstantiationContext(ICPPTemplateParameterMap parameterMap, IASTNode point) {
		this(parameterMap, -1, null, point);
	}

	/**
	 * Returns the mapping of template parameters to arguments, possibly {@code null} if the context doesn't
	 * contain it.
	 */
	public final ICPPTemplateParameterMap getParameterMap() {
		return parameterMap;
	}

	/**
	 * Adds a parameter mapping.
	 */
	public final void addToParameterMap(ICPPTemplateParameter par, ICPPTemplateArgument arg) {
		if (parameterMap == null) {
			parameterMap = new CPPTemplateParameterMap(1);
		}
		parameterMap.put(par, arg);
	}

	/**
	 * Adds a parameter mapping.
	 */
	public final void addToParameterMap(ICPPTemplateParameter par, ICPPTemplateArgument[] args) {
		if (parameterMap == null) {
			parameterMap = new CPPTemplateParameterMap(1);
		}
		parameterMap.put(par, args);
	}

	/**
	 * Puts all mappings from the supplied map into the parameter map of the context.
	 */
	public final void addToParameterMap(ICPPTemplateParameterMap toAdd) {
		if (parameterMap == null) {
			parameterMap = new CPPTemplateParameterMap((CPPTemplateParameterMap) toAdd);
		} else {
			parameterMap.putAll(toAdd);
		}
	}

	/**
	 * Returns the type specialization if instantiation happens inside a specialized type, otherwise
	 * {@code null}.
	 */
	public final ICPPTypeSpecialization getContextTypeSpecialization() {
		return contextTypeSpecialization;
	}

	/**
	 * Returns the class specialization if instantiation happens inside a specialized class, otherwise
	 * {@code null}.
	 */
	public ICPPClassSpecialization getContextClassSpecialization() {
		return getContextClassSpecialization(contextTypeSpecialization);
	}

	/**
	 * Returns the point of instantiation
	 */
	public final IASTNode getPoint() {
		return point;
	}

	/**
	 * Returns true if the pack offset is specified.
	 */
	public final boolean hasPackOffset() {
		return packOffset != -1;
	}

	/**
	 * Returns the pack offset if specified, otherwise -1.
	 */
	public final int getPackOffset() {
		return packOffset;
	}

	/**
	 * Sets the pack offset.
	 */
	public void setPackOffset(int packOffset) {
		this.packOffset = packOffset;
	}

	/**
	 * @see ICPPTemplateParameterMap#getArgument(ICPPTemplateParameter, int)
	 */
	public ICPPTemplateArgument getArgument(ICPPTemplateParameter param) {
		return parameterMap.getArgument(param, packOffset);
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
}
