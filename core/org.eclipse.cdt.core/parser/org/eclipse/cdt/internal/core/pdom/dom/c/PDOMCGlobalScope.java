/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.dom.ast.c.ICScope;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMGlobalScope;

/**
 * Represents the global C index scope.
 */
public class PDOMCGlobalScope extends PDOMGlobalScope implements ICScope {
	public static final PDOMCGlobalScope INSTANCE = new PDOMCGlobalScope();

	private PDOMCGlobalScope() {
	}
}
