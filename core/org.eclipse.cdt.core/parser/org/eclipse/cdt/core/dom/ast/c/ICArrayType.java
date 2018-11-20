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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IArrayType;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICArrayType extends IArrayType {
	public boolean isConst();

	public boolean isRestrict();

	public boolean isVolatile();

	public boolean isStatic();

	public boolean isVariableLength();
}
