/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.cdt.core.browser.IQualifiedTypeName;
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
    
    public String getName() {
        return fName;
    }

    public String getDescription() {
        return fDescription;
    }

    public ASTAccessVisibility getAccess() {
        return fAccess;
    }

    public void setAccess(ASTAccessVisibility access) {
        fAccess = access;
    }

    public boolean isVirtual() {
        return fIsVirtual;
    }
    
    public void setVirtual(boolean isVirtual) {
        fIsVirtual = isVirtual;
    }

    public boolean isInline() {
        return fIsInline;
    }

    public void setInline(boolean isInline) {
        fIsInline = isInline;
    }
    
    public boolean canModifyAccess() {
        return true;
    }

    public boolean canModifyVirtual() {
        return true;
    }

    public boolean canModifyInline() {
        return true;
    }

    public boolean isConstructor() {
        return false;
    }

    public boolean isDestructor() {
        return false;
    }
	
    public abstract String createMethodDeclaration(IQualifiedTypeName className, IBaseClassInfo[] baseClasses, String lineDelimiter);
    
    public abstract String createMethodImplementation(IQualifiedTypeName className, IBaseClassInfo[] baseClasses, String lineDelimiter);
}
