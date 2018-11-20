/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.lrparser.xlc.ast;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.lrparser.xlc.ast.IXlcCPPASTModifiedArrayModifier;
import org.eclipse.cdt.core.lrparser.xlc.ast.IXlcCPPASTVectorTypeSpecifier;
import org.eclipse.cdt.core.lrparser.xlc.ast.IXlcCPPNodeFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

@SuppressWarnings("restriction")
public class XlcCPPNodeFactory extends CPPNodeFactory implements IXlcCPPNodeFactory {

	private static final XlcCPPNodeFactory DEFAULT_INSTANCE = new XlcCPPNodeFactory();

	public static XlcCPPNodeFactory getDefault() {
		return DEFAULT_INSTANCE;
	}

	@Override
	public IXlcCPPASTVectorTypeSpecifier newVectorTypeSpecifier() {
		return new XlcCPPASTVectorTypeSpecifier();
	}

	@Override
	public IXlcCPPASTModifiedArrayModifier newModifiedArrayModifier(IASTExpression expr) {
		return new XlcCPPASTModifiedArrayModifier(expr);
	}
}
