/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	
	public IXlcCPPASTVectorTypeSpecifier newVectorTypeSpecifier() {
		return new XlcCPPASTVectorTypeSpecifier();
	}
	
	public IXlcCPPASTModifiedArrayModifier newModifiedArrayModifier(IASTExpression expr) {
		return new XlcCPPASTModifiedArrayModifier(expr);
	}
}
