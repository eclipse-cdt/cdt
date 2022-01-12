/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

import org.eclipse.cdt.core.index.IIndexMacroContainer;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;

/**
 * Composite binding for macro containers.
 *
 * @since 5.0
 */
public class CompositeMacroContainer extends CompositeIndexBinding implements IIndexMacroContainer {
	public CompositeMacroContainer(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}
}
