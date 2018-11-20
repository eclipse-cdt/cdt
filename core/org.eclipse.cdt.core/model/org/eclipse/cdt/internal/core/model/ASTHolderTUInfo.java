/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class ASTHolderTUInfo extends TranslationUnitInfo {
	public IASTTranslationUnit fAST;

	/**
	 * @param translationUnit
	 */
	public ASTHolderTUInfo(TranslationUnit translationUnit) {
		super(translationUnit);
	}
}