/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
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
