/*******************************************************************************
 * Copyright (c) 2010 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeParameter;
import org.eclipse.core.runtime.CoreException;

/**
 * This class represents a template function used for deducing 'auto' types (C++0x: 7.1.6.4).
 */
class AutoTypeResolver implements ICPPFunctionTemplate {
	// Template parameter of the function. This parameter is used in place of 'auto' keyword.
	public static final ICPPTemplateTypeParameter AUTO_TYPE =
			new CPPTemplateTypeParameter(new CPPASTName(), false);
	private static final ICPPTemplateTypeParameter[] TEMPLATE_PARAMETERS =
			new ICPPTemplateTypeParameter[] { AUTO_TYPE };
	private static final String UNEXPECTED_CALL = "Unexpected call"; //$NON-NLS-1$
	private final CPPFunctionType functionType;

	public AutoTypeResolver(IType paramType) {
		functionType = new CPPFunctionType(new CPPBasicType(Kind.eVoid, 0), new IType[] { paramType });
	}
	
	public ICPPTemplateParameter[] getTemplateParameters() {
		return TEMPLATE_PARAMETERS;
	}
	
	public ICPPFunctionType getType() {
		return functionType;
	}

	public boolean isMutable() {
		return false;
	}

	public boolean isInline() {
		return false;
	}

	public boolean isExternC() {
		return false;
	}

	public boolean isDeleted() {
		return false;
	}

	public IType[] getExceptionSpecification() {
		return null;
	}

	public ICPPParameter[] getParameters() throws DOMException {
		throw new UnsupportedOperationException(UNEXPECTED_CALL);
	}

	public int getRequiredArgumentCount() throws DOMException {
		return 1;
	}

	public boolean hasParameterPack() {
		return false;
	}

	public IScope getFunctionScope() throws DOMException {
		throw new UnsupportedOperationException(UNEXPECTED_CALL);
	}

	public boolean isStatic() {
		return false;
	}

	public boolean isExtern() {
		return false;
	}

	public boolean isAuto() {
		return false;
	}

	public boolean isRegister() {
		return false;
	}

	public boolean takesVarArgs() {
		return false;
	}

	public String getName() {
		throw new UnsupportedOperationException(UNEXPECTED_CALL);
	}

	public char[] getNameCharArray() {
		throw new UnsupportedOperationException(UNEXPECTED_CALL);
	}

	public ILinkage getLinkage() throws CoreException {
		throw new UnsupportedOperationException(UNEXPECTED_CALL);
	}

	public IBinding getOwner() {
		throw new UnsupportedOperationException(UNEXPECTED_CALL);
	}

	public IScope getScope() throws DOMException {
		throw new UnsupportedOperationException(UNEXPECTED_CALL);
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		throw new UnsupportedOperationException(UNEXPECTED_CALL);
	}

	public String[] getQualifiedName() throws DOMException {
		throw new UnsupportedOperationException(UNEXPECTED_CALL);
	}

	public char[][] getQualifiedNameCharArray() throws DOMException {
		throw new UnsupportedOperationException(UNEXPECTED_CALL);
	}

	public boolean isGloballyQualified() throws DOMException {
		throw new UnsupportedOperationException(UNEXPECTED_CALL);
	}
}