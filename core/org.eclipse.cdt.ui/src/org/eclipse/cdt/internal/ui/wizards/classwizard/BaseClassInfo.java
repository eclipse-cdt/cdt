/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;


public class BaseClassInfo implements IBaseClassInfo {
    
    private ASTAccessVisibility fAccess;
    private boolean fIsVirtual;
    
    public BaseClassInfo(ASTAccessVisibility access, boolean isVirtual) {
        fAccess = access;
        fIsVirtual = isVirtual;
    }

    public ASTAccessVisibility getAccess() {
        return fAccess;
    }
    
	public boolean isVirtual() {
	    return fIsVirtual;
	}

    public void setAccess(ASTAccessVisibility access) {
        fAccess = access;
    }

    public void setVirtual(boolean isVirtual) {
        fIsVirtual = isVirtual;
    }
}
