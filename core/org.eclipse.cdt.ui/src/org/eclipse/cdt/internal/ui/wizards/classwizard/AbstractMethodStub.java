/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
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

public abstract class AbstractMethodStub implements IMethodStub {
	private String fName;
	private String fDescription;
	private ASTAccessVisibility fAccess;
	private boolean fIsVirtual;
	private EImplMethod fImplMethod;

	public AbstractMethodStub(String name, ASTAccessVisibility access, boolean isVirtual, EImplMethod impl) {
		fName = name;
		fAccess = access;
		fIsVirtual = isVirtual;
		fImplMethod = impl;
	}

	@Override
	public EImplMethod getImplMethod() {
		return fImplMethod;
	}

	@Override
	public boolean isDeleted() {
		return fImplMethod == EImplMethod.DELETED;
	}

	@Override
	public boolean isDefault() {
		return fImplMethod == EImplMethod.DEFAULT;
	}

	@Override
	public boolean hasDefinition() {
		return fImplMethod == EImplMethod.DEFINITION;
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public String getDescription() {
		return fDescription;
	}

	@Override
	public ASTAccessVisibility getAccess() {
		return fAccess;
	}

	@Override
	public void setAccess(ASTAccessVisibility access) {
		fAccess = access;
	}

	@Override
	public boolean isVirtual() {
		return fIsVirtual;
	}

	@Override
	public void setVirtual(boolean isVirtual) {
		fIsVirtual = isVirtual;
	}

	@Override
	public boolean isInline() {
		return fImplMethod == EImplMethod.INLINE;
	}

	@Override
	public void setImplMethod(EImplMethod method) {
		fImplMethod = method;
	}

	@Override
	public boolean canModifyAccess() {
		return true;
	}

	@Override
	public boolean canModifyVirtual() {
		return true;
	}

	@Override
	public boolean canModifyImplementation() {
		return true;
	}

	@Override
	public boolean isConstructor() {
		return false;
	}

	@Override
	public boolean isDestructor() {
		return false;
	}

	@Override
	public abstract String createMethodDeclaration(ITranslationUnit tu, String className, IBaseClassInfo[] baseClasses,
			String lineDelimiter) throws CoreException;

	@Override
	public abstract String createMethodImplementation(ITranslationUnit tu, String className,
			IBaseClassInfo[] baseClasses, String lineDelimiter) throws CoreException;

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}
}
