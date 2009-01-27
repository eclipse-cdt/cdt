/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface for partial specializations in the pdom.
 */
interface IPDOMPartialSpecialization extends ICPPClassTemplatePartialSpecialization, IPDOMBinding {

	/**
	 * Allows for setting the arguments after the binding has been added to the pdom.
	 */
	void setArguments(ICPPTemplateArgument[] args) throws CoreException;
}
