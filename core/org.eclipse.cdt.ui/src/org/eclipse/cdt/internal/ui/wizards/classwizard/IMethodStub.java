/*******************************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems and others.
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
import org.eclipse.core.runtime.CoreException;

public interface IMethodStub {
	public enum EImplMethod {
		DEFINITION, INLINE, DEFAULT, DELETED
	}

	public String getName();

	public String getDescription();

	public ASTAccessVisibility getAccess();

	public boolean canModifyAccess();

	public void setAccess(ASTAccessVisibility access);

	public boolean isVirtual();

	public boolean canModifyVirtual();

	public void setVirtual(boolean isVirtual);

	public EImplMethod getImplMethod();

	public boolean isInline();

	public boolean isDeleted();

	public boolean isDefault();

	public boolean hasDefinition();

	public boolean canModifyImplementation();

	public void setImplMethod(EImplMethod method);

	public boolean isConstructor();

	public boolean isDestructor();

	public boolean isEnabledByDefault();

	public String createMethodDeclaration(ITranslationUnit tu, String className, IBaseClassInfo[] baseClasses,
			String lineDelimiter) throws CoreException;

	public String createMethodImplementation(ITranslationUnit tu, String className, IBaseClassInfo[] baseClasses,
			String lineDelimiter) throws CoreException;
}
