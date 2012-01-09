/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;


public class BaseClassInfo implements IBaseClassInfo {
    
    private ITypeInfo fType;
    private ASTAccessVisibility fAccess;
    private boolean fIsVirtual;
    
    public BaseClassInfo(ITypeInfo type, ASTAccessVisibility access, boolean isVirtual) {
        fType = type;
        fAccess = access;
        fIsVirtual = isVirtual;
    }

    @Override
	public ITypeInfo getType() {
        return fType;
    }
    
    @Override
	public ASTAccessVisibility getAccess() {
        return fAccess;
    }
    
	@Override
	public boolean isVirtual() {
	    return fIsVirtual;
	}

    @Override
	public void setAccess(ASTAccessVisibility access) {
        fAccess = access;
    }

    @Override
	public void setVirtual(boolean isVirtual) {
        fIsVirtual = isVirtual;
    }
}
