/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * A separate template scope is used for each template declaration. This leads to a
 * non-hierarchical structure of scopes. E.g. for a method of a class-template the
 * declaration and definition will nests in different template-scopes. Therefore
 * {@link IScope#getParent()} will never return a template scope unless the method is
 * called for a template-scope that directly nests in another one.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPTemplateScope extends ICPPScope {
	/**
	 * Returns the corresponding template declaration
	 * @since 5.1
	 */
	public ICPPASTTemplateDeclaration getTemplateDeclaration();
}
