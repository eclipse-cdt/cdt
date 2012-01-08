/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.parser.tests.scanner;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

final class TestMacro implements IMacroBinding {
	private String fName;
	private String fExpansion;
	private String[] fParams;

	public TestMacro(String name, String expansion, String[] params) {
		fName= name;
		fExpansion= expansion;
		fParams= params;
	}

	@Override
	public char[] getExpansion() {
		return fExpansion.toCharArray();
	}

	@Override
	public boolean isFunctionStyle() {
		return fParams != null;
	}

	@Override
	public ILinkage getLinkage() {
		return null;
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public char[] getNameCharArray() {
		return fName.toCharArray();
	}

	@Override
	public IScope getScope() throws DOMException {
		return null;
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public char[][] getParameterList() {
		if (fParams == null) {
			return null;
		}
		char[][] result= new char[fParams.length][];
		for (int i = 0; i < result.length; i++) {
			result[i]= fParams[i].toCharArray();
		}
		return result;
	}

	@Override
	public char[] getExpansionImage() {
		return getExpansion();
	}

	@Override
	public char[][] getParameterPlaceholderList() {
		return getParameterList();
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public IBinding getOwner() {
		return null;
	}
}