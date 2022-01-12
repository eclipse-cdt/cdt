/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPTemplateTypeParameter extends ICPPTemplateParameter, IType {
	/**
	 * The default type for this parameter. May be {@code null}.
	 */
	public IType getDefault() throws DOMException;

	/**
	 * Types containing template parameters need to be compared even before it is known to which
	 * binding the template parameter belongs to. Therefore {@link #isSameType(IType)} compares
	 * the kind and the parameter position of the template parameter, only. The name and the owner
	 * is ignored.
	 *
	 * @since 5.1
	 */
	@Override
	public boolean isSameType(IType type);
}
