/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	private PDOMCGlobalScope() {}
}
