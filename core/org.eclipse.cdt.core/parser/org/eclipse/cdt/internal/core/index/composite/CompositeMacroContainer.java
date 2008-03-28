/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
