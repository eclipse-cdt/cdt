/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;

/**
 * Adds method that assist in finding the relation-ship between a template declaration
 * and the names of the nested declaration.
 */
public interface ICPPASTInternalTemplateDeclaration extends ICPPASTTemplateDeclaration {
	/**
	 * Returns whether this template declaration is associated with the last name of
	 * the possibly qualified name of the enclosing declaration. If this template declaration
	 * encloses another one, <code>false</code> is returned.
	 */
	boolean isAssociatedWithLastName();

	/**
	 * Returns the nesting level of this template declaration.
	 * @see ICPPTemplateParameter#getTemplateNestingLevel()
	 */
	short getNestingLevel();

	/**
	 * Sets the nesting level, once it is determined
	 */
	void setNestingLevel(short level);

	/**
	 * Sets whether the template declaration is associated with the last name.
	 */
	void setAssociatedWithLastName(boolean value);
}
