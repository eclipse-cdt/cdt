/*******************************************************************************
 * Copyright (c) 2008, 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;

/**
 * Represents a partially instantiated C++ class template, declaration of which is not yet available.
 *
 * This interface should be made public.
 * @since 5.0
 */
public interface ICPPUnknownMemberClassInstance extends ICPPUnknownMemberClass {
	/**
	 * Returns the arguments of the instantiation
	 */
	public ICPPTemplateArgument[] getArguments();
}
