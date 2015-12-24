/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.lrparser.xlc.ast;

import org.eclipse.cdt.core.lrparser.xlc.ast.IXlcCASTVectorTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclSpecifier;

@SuppressWarnings("restriction")
public class XlcCASTVectorTypeSpecifier extends CASTSimpleDeclSpecifier implements IXlcCASTVectorTypeSpecifier {
	private boolean isPixel;
	private boolean isBool;

	public XlcCASTVectorTypeSpecifier() {
		super();
	}

	@Override
	public XlcCASTVectorTypeSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public XlcCASTVectorTypeSpecifier copy(CopyStyle style) {
		XlcCASTVectorTypeSpecifier copy = new XlcCASTVectorTypeSpecifier();
		copy.isPixel = isPixel;
		copy.isBool = isBool;
		return copy(copy, style);
	}

	public boolean isPixel() {
		return isPixel;
	}

	public void setPixel(boolean isPixel) {
		this.isPixel = isPixel;
	}

	public boolean isBool() {
		return isBool;
	}

	public void setBool(boolean isBool) {
		this.isBool = isBool;
	}
}
