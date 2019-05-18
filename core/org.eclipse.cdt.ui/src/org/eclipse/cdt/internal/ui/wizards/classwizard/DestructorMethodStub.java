/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.ui.CodeGeneration;
import org.eclipse.core.runtime.CoreException;

public final class DestructorMethodStub extends AbstractMethodStub {

	private static String NAME = NewClassWizardMessages.NewClassCodeGeneration_stub_destructor_name;

	public DestructorMethodStub() {
		this(ASTAccessVisibility.PUBLIC, true, EImplMethod.DEFINITION);
	}

	public DestructorMethodStub(ASTAccessVisibility access, boolean isVirtual, EImplMethod method) {
		super(NAME, access, isVirtual, method);
	}

	@Override
	public String createMethodDeclaration(ITranslationUnit tu, String className, IBaseClassInfo[] baseClasses,
			String lineDelimiter) throws CoreException {
		StringBuilder buf = new StringBuilder();
		if (isVirtual()) {
			buf.append("virtual "); //$NON-NLS-1$
		}
		buf.append("~"); //$NON-NLS-1$
		buf.append(className);
		buf.append("()"); //$NON-NLS-1$
		if (isInline()) {
			buf.append('{');
			buf.append(lineDelimiter);
			String body = CodeGeneration.getDestructorBodyContent(tu, className, null, lineDelimiter);
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
		buf.append("::~"); //$NON-NLS-1$
		buf.append(className);
		buf.append("()"); //$NON-NLS-1$
		buf.append(lineDelimiter);
		buf.append('{');
		buf.append(lineDelimiter);
		String body = CodeGeneration.getDestructorBodyContent(tu, className, null, lineDelimiter);
		if (body != null) {
			buf.append(body);
			buf.append(lineDelimiter);
		}
		buf.append('}');
		return buf.toString();
	}

	@Override
	public boolean isDestructor() {
		return true;
	}
}
