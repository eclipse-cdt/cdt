/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;

/**
 * C++ specific initializer clause.
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.5
 */
public interface ICPPASTInitializerClause extends IASTInitializerClause {
	/**
	 * Returns the evaluation object for this expression.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	ICPPEvaluation getEvaluation();
}
