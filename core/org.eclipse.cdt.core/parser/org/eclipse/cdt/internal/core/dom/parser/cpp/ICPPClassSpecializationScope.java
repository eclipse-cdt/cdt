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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;

/**
 * Composite scope of a class specialization. Supports creating instances for bindings found
 * in the scope of the specialized class template.
 *
 * @since 5.0
 */
public interface ICPPClassSpecializationScope extends ICPPClassScope {
	/**
	 * Returns the class that was specialized to get this scope.
	 */
	ICPPClassType getOriginalClassType();

	/**
	 * Returns the instance for a binding that belongs to the scope of the original
	 * class type.
	 */
	IBinding getInstance(IBinding original);
}
