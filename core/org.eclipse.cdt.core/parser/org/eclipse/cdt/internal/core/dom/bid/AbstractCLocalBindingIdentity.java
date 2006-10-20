/*******************************************************************************
 * Copyright (c) 2006 Symbian Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.bid;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 */
public abstract class AbstractCLocalBindingIdentity implements ICLocalBindingIdentity {
	protected static final String SEP = " | "; //$NON-NLS-1$
	
	protected PDOMLinkage linkage;
	protected IBinding binding;

	protected String extendedType; // cached

	protected AbstractCLocalBindingIdentity(IBinding binding, PDOMLinkage linkage) {
		if(binding==null || linkage==null)
			throw new IllegalArgumentException();
		this.binding = binding;
		this.linkage = linkage;
	}

	public String getName() {
		return binding.getName();
	}

	public abstract int getTypeConstant() throws CoreException;

	public abstract String getExtendedType() throws CoreException;

	public String toString() {
		try {
			return getName()+SEP+getTypeConstant()+SEP+getExtendedType();
		} catch(CoreException ce) {
			throw new RuntimeException(ce);
		}
	}
	
	public char[] getNameCharArray() throws CoreException {
		return binding.getNameCharArray();
	}
}
