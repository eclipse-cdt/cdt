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


public interface IMethodStub {
    public String getName();
    public String getDescription();

    public ASTAccessVisibility getAccess();
    public boolean canModifyAccess();
    public void setAccess(ASTAccessVisibility access);

    public boolean isVirtual();
    public boolean canModifyVirtual();
    public void setVirtual(boolean isVirtual);

    public boolean isInline();
    public boolean canModifyInline();
    public void setInline(boolean isVirtual);
    
    public boolean isConstructor();
    public boolean isDestructor();
    
    public String createMethodDeclaration(ITranslationUnit tu, String className, IBaseClassInfo[] baseClasses, String lineDelimiter) throws CoreException;
    public String createMethodImplementation(ITranslationUnit tu, String className, IBaseClassInfo[] baseClasses, String lineDelimiter) throws CoreException;
}
