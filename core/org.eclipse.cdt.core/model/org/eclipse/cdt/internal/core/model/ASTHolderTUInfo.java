/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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