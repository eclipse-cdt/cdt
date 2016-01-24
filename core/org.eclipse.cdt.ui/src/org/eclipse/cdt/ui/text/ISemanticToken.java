/*******************************************************************************
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.ui.text;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * An interface for accessing details of the token that is being highlighted.
 *
 * @since 5.6
 */
public interface ISemanticToken {
	/**
	 * @return Returns the binding, can be <code>null</code>.
	 */
	public IBinding getBinding();

	/**
	 * @return the AST node
	 */
	public IASTNode getNode();

	/**
	 * @return the AST root
	 */
	public IASTTranslationUnit getRoot();
}
