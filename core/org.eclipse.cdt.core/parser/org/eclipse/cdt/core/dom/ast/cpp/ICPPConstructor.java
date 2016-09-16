/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 * For a constexpr constructor returns the ICPPExecution for its constructor chain. Otherwise returns
	 * {@code null}.
	 * @param point The point of instantiation for name lookups.
	 * @since 6.0
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public ICPPExecution getConstructorChainExecution(IASTNode point);
}
