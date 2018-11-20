/*
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.cdt.ui.text;

/**
 * Interface that must be implemented by contributors to the org.eclipse.cdt.ui.semanticHighlighting extension
 * point.
 *
 * @since 5.6
 */
public interface ISemanticHighlighter {
	/**
	 * Returns <code>true</code> iff the semantic highlighting consumes the semantic token.
	 * <p>
	 * NOTE: Implementors are not allowed to keep a reference on the token or on any object retrieved from the
	 * token.
	 * </p>
	 *
	 * @param token
	 *            the semantic token for a {@link org.eclipse.cdt.core.dom.ast.IASTName}
	 * @return <code>true</code> iff the semantic highlighting consumes the semantic token
	 */
	public boolean consumes(ISemanticToken token);
}
