/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;

/**
 * @author Lukas Felber
 */
public class ParameterInfo {
	private IASTParameterDeclaration parameter;
	private boolean hasNewName;
	private String parameterName;

	public ParameterInfo(IASTParameterDeclaration parameter, String parameterName, boolean hasNewName) {
		this.parameter = parameter;
		this.hasNewName = hasNewName;
		this.parameterName = parameterName;
	}

	public boolean hasNewName() {
		return hasNewName;
	}

	public boolean hasDefaultValue() {
		return getDefaultValueNode() != null;
	}

	public String getTypeName() {
		return NameHelper.getTypeName(parameter);
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String newName) {
		this.parameterName = newName;
	}

	public IASTParameterDeclaration getParameter() {
		return parameter;
	}

	public IASTName getNameNode() {
		return parameter.getDeclarator().getName();
	}

	public IASTName getNewNameNode() {
		return new CPPASTName(parameterName.toCharArray());
	}

	public IASTNode getDefaultValueNode() {
		return parameter.getDeclarator().getInitializer();
	}
}
