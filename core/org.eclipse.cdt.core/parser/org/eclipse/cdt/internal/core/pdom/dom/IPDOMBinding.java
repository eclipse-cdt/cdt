/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;

/**
 * Interface for bindings in the PDOM.
 */
public interface IPDOMBinding extends IPDOMNode, IIndexFragmentBinding {
	/**
	 * Returns the database this binding belongs to.
	 */
	PDOM getPDOM();

	/**
	 * Returns the database record for this binding.
	 */
	long getRecord();
	
	/**
	 * Returns the linkage of the binding.
	 */
	@Override
	PDOMLinkage getLinkage();
}
