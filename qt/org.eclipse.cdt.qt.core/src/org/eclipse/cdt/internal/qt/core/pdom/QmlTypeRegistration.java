/*
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.qt.core.ASTUtil;
import org.eclipse.cdt.internal.qt.core.QtKeywords;
import org.eclipse.cdt.internal.qt.core.index.IQmlRegistration;
import org.eclipse.core.runtime.CoreException;

public class QmlTypeRegistration extends ASTDelegatedName implements IQtASTName {

	private final ICPPTemplateInstance functionInstanceBinding;
	private final IASTFunctionCallExpression fnCall;
	private final IQmlRegistration.Kind kind;
	private char[] simpleID;

	public QmlTypeRegistration(IASTName ast, ICPPTemplateInstance functionInstanceBinding,
			IASTFunctionCallExpression fnCall) {
		super(ast);
		this.functionInstanceBinding = functionInstanceBinding;
		this.fnCall = fnCall;

		if (QtKeywords.QML_REGISTER_UNCREATABLE_TYPE.equals(functionInstanceBinding.getName()))
			this.kind = IQmlRegistration.Kind.Uncreatable;
		else
			this.kind = IQmlRegistration.Kind.Type;
	}

	@Override
	public char[] getSimpleID() {
		if (simpleID == null) {
			IASTInitializerClause[] args = fnCall.getArguments();
			simpleID = (functionInstanceBinding.getName()
					+ ASTTypeUtil.getArgumentListString(functionInstanceBinding.getTemplateArguments(), true) + "\0("
					+ asStringForName(args, 0) + ',' + asStringForName(args, 1) + ',' + asStringForName(args, 2) + ','
					+ asStringForName(args, 3) + ')').toCharArray();
		}

		return simpleID;
	}

	@Override
	public QtPDOMBinding createPDOMBinding(QtPDOMLinkage linkage) throws CoreException {
		switch (kind) {
		case Type:
			return new QtPDOMQmlRegistration(linkage, this, delegate);
		case Uncreatable:
			return new QtPDOMQmlUncreatable(linkage, this, delegate);
		}
		return null;
	}

	public String getQObjectName() {
		ICPPTemplateArgument[] args = functionInstanceBinding.getTemplateArguments();
		if (args.length < 1)
			return null;

		IType type = args[0].getTypeValue();
		return type instanceof ICPPBinding ? ASTUtil.getFullyQualifiedName((ICPPBinding) type) : null;
	}

	public Long getVersion() {
		ICPPTemplateArgument[] args = functionInstanceBinding.getTemplateArguments();
		if (args.length < 2)
			return null;

		IValue val = args[1].getNonTypeValue();
		return val == null ? null : val.numberValue().longValue();
	}

	public String getUri() {
		return getArgAsStringOrNull(0);
	}

	public Long getMajor() {
		return getArgAsLongOrNull(1);
	}

	public Long getMinor() {
		return getArgAsLongOrNull(2);
	}

	public String getQmlName() {
		return getArgAsStringOrNull(3);
	}

	public String getReason() {
		return getArgAsStringOrNull(4);
	}

	private String asStringForName(IASTInitializerClause[] args, int index) {
		String arg = args.length <= index ? null : asString(args[index]);
		return arg == null ? "" : arg;
	}

	private String getArgAsStringOrNull(int index) {
		IASTInitializerClause[] args = fnCall.getArguments();
		if (args.length <= index)
			return null;

		return asString(args[index]);
	}

	private Long getArgAsLongOrNull(int index) {
		IASTInitializerClause[] args = fnCall.getArguments();
		if (args.length <= index)
			return null;

		String str = asString(args[index]);
		if (str != null)
			try {
				return Long.parseLong(str);
			} catch (NumberFormatException e) {
				// This is caused by invalid user code, do not log it
			}

		return null;
	}

	private static String asString(IASTInitializerClause init) {
		if (init instanceof IASTLiteralExpression) {
			IASTLiteralExpression literal = (IASTLiteralExpression) init;
			switch (literal.getKind()) {
			case IASTLiteralExpression.lk_integer_constant:
				return new String(literal.getValue());
			case IASTLiteralExpression.lk_string_literal:
				char[] value = literal.getValue();
				return new String(value, 1, value.length - 2);
			}
		}
		return null;
	}
}
