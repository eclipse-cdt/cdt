/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * An interface for bindings that are resolvable in two steps. The binding computed
 * by the first step is an intermediate binding that can be replaced in a second
 * step before the binding is exposed via public API.
 * <p>
 * The bindings of the two phases may not be equal, but they must implement the
 * same public interfaces.
 * <p>
 * This allows for having multiple bindings for one final binding and deferring
 * the act of unifying them to a later point in time.
 */
public interface ICPPTwoPhaseBinding extends IBinding {

	/**
	 * Triggers the second step of the resolution where the binding that will be
	 * exposed via public API has to be computed. If this binding is already final
	 * {@code this} must be returned.
	 * <p> Note, that the result of this operation is an instance of
	 * {@link ICPPTwoPhaseBinding}, however it must resolve to itself using
	 * {@link #resolveFinalBinding(CPPASTNameBase)}.
	 */
	IBinding resolveFinalBinding(CPPASTNameBase astName);
}
