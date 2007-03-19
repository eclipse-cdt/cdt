/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.c;

import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.CompositeIndexBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

abstract class CompositeCBinding extends CompositeIndexBinding {
	public CompositeCBinding(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}
}
