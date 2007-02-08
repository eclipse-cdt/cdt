/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents the semantics of a name in the index.
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public interface IIndexBinding extends IBinding {
	IIndexBinding[] EMPTY_INDEX_BINDING_ARRAY = new IIndexBinding[0];

	/**
	 * Returns the qualified name of this binding as array of strings.
	 */
	String[] getQualifiedName();
	
	/**
	 * Returns whether the scope of the binding is file-local. A file local
	 * binding is private to an index and should not be adapted to a binding
	 * in another index.
	 */
	boolean isFileLocal() throws CoreException;
}
