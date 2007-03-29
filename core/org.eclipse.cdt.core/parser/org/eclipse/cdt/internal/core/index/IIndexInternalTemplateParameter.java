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
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * This interface is possibly interim. It allows determining the owner of a parameter.
 */
public interface IIndexInternalTemplateParameter {
	ICPPBinding getParameterOwner() throws CoreException;
}
