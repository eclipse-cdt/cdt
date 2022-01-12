/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marco Stornelli - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.ui.CodeGeneration;
import org.eclipse.core.runtime.CoreException;

public final class AssignOpMethodStub extends AbstractMethodStub {
	private static String NAME = NewClassWizardMessages.NewClassCodeGeneration_stub_assign_op_name;

	public AssignOpMethodStub() {
		this(ASTAccessVisibility.PUBLIC, EImplMethod.DEFINITION);
	}

	public AssignOpMethodStub(ASTAccessVisibility access, EImplMethod method) {
		super(NAME, access, false, method);
	}

	@Override
	public String createMethodDeclaration(ITranslationUnit tu, String className, IBaseClassInfo[] baseClasses,
			String lineDelimiter) throws CoreException {
		StringBuilder buf = new StringBuilder();
		buf.append(className);
		buf.append("& operator=(const "); //$NON-NLS-1$
		buf.append(className);
		buf.append("& other)"); //$NON-NLS-1$
		if (isInline()) {
			buf.append('{');
			buf.append(lineDelimiter);
			String body = CodeGeneration.getMethodBodyContent(tu, className, "operator=", null, lineDelimiter); //$NON-NLS-1$
			if (body != null) {
				buf.append(body);
				buf.append(lineDelimiter);
			}
			buf.append('}');
		} else if (isDefault()) {
			buf.append(" = default;"); //$NON-NLS-1$
		} else if (isDeleted()) {
			buf.append(" = delete;"); //$NON-NLS-1$
		} else {
			buf.append(";"); //$NON-NLS-1$
		}
		return buf.toString();
	}

	@Override
	public String createMethodImplementation(ITranslationUnit tu, String className, IBaseClassInfo[] baseClasses,
			String lineDelimiter) throws CoreException {
		if (!hasDefinition()) {
			return ""; //$NON-NLS-1$
		}
		StringBuilder buf = new StringBuilder();
		buf.append(className);
		buf.append("& "); //$NON-NLS-1$
		buf.append(className);
		buf.append("::"); //$NON-NLS-1$
		buf.append("operator=(const "); //$NON-NLS-1$
		buf.append(className);
		buf.append("& other)"); //$NON-NLS-1$
		buf.append(lineDelimiter);
		buf.append('{');
		buf.append(lineDelimiter);
		String body = CodeGeneration.getMethodBodyContent(tu, className, "operator=", null, lineDelimiter); //$NON-NLS-1$
		if (body != null) {
			buf.append(body);
			buf.append(lineDelimiter);
		}
		buf.append('}');
		return buf.toString();
	}

	@Override
	public boolean isConstructor() {
		return false;
	}

	@Override
	public boolean isEnabledByDefault() {
		return false;
	}

	@Override
	public boolean canModifyVirtual() {
		return false;
	}
}
