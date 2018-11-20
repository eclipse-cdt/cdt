/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;

/**
 * @deprecated use {@link ICPPBasicType}, instead.
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface IGPPBasicType extends ICPPBasicType {
	/**
	 * @deprecated don't use this constant.
	 */
	@Deprecated
	public static final int t_typeof = IGPPASTSimpleDeclSpecifier.t_typeof;

	/**
	 * @deprecated don't use this method.
	 */
	@Deprecated
	public IType getTypeofType() throws DOMException;
}
