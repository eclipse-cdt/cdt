/*******************************************************************************
 * Copyright (c) 2009, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPPartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface for partial specializations in the PDOM.
 */
interface IPDOMPartialSpecialization extends ICPPPartialSpecialization, IPDOMBinding {
	/**
	 * Allows for setting the arguments after the binding has been added to the PDOM.
	 */
	void setTemplateArguments(ICPPTemplateArgument[] args) throws CoreException;
}
