/*******************************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public abstract class AbstractMethodStub implements IMethodStub {
    protected String fName;
    protected String fDescription;
    protected ASTAccessVisibility fAccess;
    protected boolean fIsVirtual;
    protected boolean fIsInline;
    
    public AbstractMethodStub(String name, ASTAccessVisibility access, boolean isVirtual, boolean isInline) {
        fName = name;
        fAccess = access;
        fIsVirtual = isVirtual;
        fIsInline = isInline;
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
        return fIsInline;
    }

    @Override
	public void setInline(boolean isInline) {
        fIsInline = isInline;
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
	public boolean canModifyInline() {
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
	public abstract String createMethodDeclaration(ITranslationUnit tu, String className, IBaseClassInfo[] baseClasses, String lineDelimiter) throws CoreException;
    
    @Override
	public abstract String createMethodImplementation(ITranslationUnit tu, String className, IBaseClassInfo[] baseClasses, String lineDelimiter) throws CoreException;
}
