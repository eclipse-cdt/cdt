/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.core.runtime.CoreException;

public interface IIndexFragmentName extends IIndexName {
	public static final IIndexFragmentName[] EMPTY_NAME_ARRAY = {};

	/**
	 * Returns the fragment that owns this name.
	 */
	IIndexFragment getIndexFragment();

	/**
	 * Returns the (proxy) binding the name resolves to.
	 */
	IIndexFragmentBinding getBinding() throws CoreException;
}
