/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
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
package org.eclipse.cdt.core.browser;

/**
 * @deprecated not used anywhere
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface ITypeInfoVisitor {
	public boolean visit(ITypeInfo info);

	public boolean shouldContinue();
}
