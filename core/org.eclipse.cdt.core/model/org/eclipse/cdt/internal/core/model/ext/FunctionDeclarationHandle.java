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
 *    Markus Schorn - initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.internal.core.model.FunctionDeclaration;

public class FunctionDeclarationHandle extends CElementHandle implements IFunctionDeclaration {

	private String[] fParameterTypes;
	private String fReturnType;
	private boolean fIsStatic;

	public FunctionDeclarationHandle(ICElement parent, IFunction func) {
		this(parent, ICElement.C_FUNCTION_DECLARATION, func);
	}

	protected FunctionDeclarationHandle(ICElement parent, int type, IFunction func) {
		super(parent, type, func.getName());
		fParameterTypes = extractParameterTypes(func);
		fReturnType = ASTTypeUtil.getType(func.getType().getReturnType(), false);
		fIsStatic = func.isStatic();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IFunctionDeclaration) {
			return FunctionDeclaration.equals(this, (IFunctionDeclaration) obj);
		}
		return false;
	}

	@Override
	public int getNumberOfParameters() {
		return fParameterTypes.length;
	}

	@Override
	public String[] getParameterTypes() {
		return fParameterTypes;
	}

	@Override
	public String getReturnType() {
		return fReturnType;
	}

	@Override
	public String getSignature() throws CModelException {
		return FunctionDeclaration.getSignature(this);
	}

	@Override
	public boolean isStatic() throws CModelException {
		return fIsStatic;
	}

}
