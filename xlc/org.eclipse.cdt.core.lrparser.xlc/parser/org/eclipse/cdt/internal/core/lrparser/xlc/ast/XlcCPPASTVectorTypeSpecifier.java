/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
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

import org.eclipse.cdt.core.lrparser.xlc.ast.IXlcCPPASTVectorTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;

@SuppressWarnings("restriction")
public class XlcCPPASTVectorTypeSpecifier extends CPPASTSimpleDeclSpecifier implements IXlcCPPASTVectorTypeSpecifier {

	private boolean isPixel;

	public XlcCPPASTVectorTypeSpecifier() {
		super();
	}

	@Override
	public XlcCPPASTVectorTypeSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public XlcCPPASTVectorTypeSpecifier copy(CopyStyle style) {
		XlcCPPASTVectorTypeSpecifier copy = new XlcCPPASTVectorTypeSpecifier();
		copy.isPixel = isPixel;
		return copy(copy, style);
	}

	@Override
	public boolean isPixel() {
		return isPixel;
	}

	@Override
	public void setPixel(boolean isPixel) {
		this.isPixel = isPixel;
	}

}
