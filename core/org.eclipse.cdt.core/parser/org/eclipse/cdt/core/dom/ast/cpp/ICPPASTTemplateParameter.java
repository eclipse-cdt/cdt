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
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Base interface for all template parameters.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTTemplateParameter extends IASTNode {
	public static final ICPPASTTemplateParameter[] EMPTY_TEMPLATEPARAMETER_ARRAY = {};

	/**
	 * Returns whether this template parameter is a parameter pack.
	 * @since 5.2
	 */
	public boolean isParameterPack();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTTemplateParameter copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTTemplateParameter copy(CopyStyle style);
}
