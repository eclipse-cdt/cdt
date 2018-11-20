/*******************************************************************************
 *  Copyright (c) 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.xlc.ast;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

public interface IXlcCPPNodeFactory extends ICPPNodeFactory {

	public IXlcCPPASTVectorTypeSpecifier newVectorTypeSpecifier();

	public IXlcCPPASTModifiedArrayModifier newModifiedArrayModifier(IASTExpression expr);
}
