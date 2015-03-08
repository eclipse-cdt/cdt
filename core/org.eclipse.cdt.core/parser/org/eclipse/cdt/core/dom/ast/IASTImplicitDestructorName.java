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
package org.eclipse.cdt.core.dom.ast;

/**
 * An implicit name corresponding to a destructor call for a temporary or a variable going out of scope.
 *
 * @since 5.11
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTImplicitDestructorName extends IASTImplicitName {
	public static final IASTImplicitDestructorName[] EMPTY_NAME_ARRAY = {}; 

	/**
	 * Returns the name corresponding to the constructor call.
	 */
	IASTImplicitName getConstructionPoint();
}
