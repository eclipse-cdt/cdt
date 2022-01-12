/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * Represents a reference to a function which cannot be resolved
 * because  an argument depends on a template parameter.
 *
 * @since 5.6
 */
public interface ICPPDeferredFunction extends ICPPFunction {
	/**
	 * Returns the candidate functions the reference might resolve to
	 * after template instantiation.
	 */
	public ICPPFunction[] getCandidates();
}
