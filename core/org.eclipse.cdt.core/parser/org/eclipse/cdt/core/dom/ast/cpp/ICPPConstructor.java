/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPConstructor extends ICPPMethod {
	public static final ICPPConstructor[] EMPTY_CONSTRUCTOR_ARRAY = {};

	/**
	 * @since 6.0
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated use {@link ICPPConstructor#getConstructorChainExecution()} instead.
	 */
	@Deprecated
	public ICPPExecution getConstructorChainExecution(IASTNode point);

	/**
	 * For a constexpr constructor returns the ICPPExecution for its constructor chain. Otherwise returns
	 * {@code null}.
	 * @since 6.3
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public ICPPExecution getConstructorChainExecution();
}
