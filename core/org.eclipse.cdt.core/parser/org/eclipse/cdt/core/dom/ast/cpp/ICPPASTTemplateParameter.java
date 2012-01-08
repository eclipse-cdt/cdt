/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
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
	public static final ICPPASTTemplateParameter[] EMPTY_TEMPLATEPARAMETER_ARRAY = new ICPPASTTemplateParameter[0];

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
