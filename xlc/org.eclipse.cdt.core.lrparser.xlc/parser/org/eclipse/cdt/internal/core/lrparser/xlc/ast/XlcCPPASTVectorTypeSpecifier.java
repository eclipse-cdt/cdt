/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	public boolean isPixel() {
		return isPixel;
	}

	public void setPixel(boolean isPixel) {
		this.isPixel = isPixel;
	}
	
}
