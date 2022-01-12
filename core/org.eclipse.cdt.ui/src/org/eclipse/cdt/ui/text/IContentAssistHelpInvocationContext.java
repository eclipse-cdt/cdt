/**********************************************************************
 * Copyright (c) 2009 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui.text;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;

/**
 * Invocation context for content assist.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.1
 */
public interface IContentAssistHelpInvocationContext extends ICHelpInvocationContext {

	/**
	 * @return the offset of the content assist.
	 */
	int getInvocationOffset();

	/**
	 * @return the AST completion node or null.
	 */
	IASTCompletionNode getCompletionNode();

}
