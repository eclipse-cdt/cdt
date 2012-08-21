/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Specialization of a method.
 * @since 5.5
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPMethodSpecialization extends ICPPSpecialization, ICPPMethod {
	/**
	 * Similar to {@link ICPPFunction#getExceptionSpecification()} but a accepts a starting point
	 * for template instantiation.
	 */
	IType[] getExceptionSpecification(IASTNode point);
}
