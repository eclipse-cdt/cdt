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

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;


public interface IBaseClassInfo {
	public ITypeInfo getType();
    public ASTAccessVisibility getAccess();
    public boolean isVirtual();
    
    public void setAccess(ASTAccessVisibility access);
    public void setVirtual(boolean isVirtual);
}