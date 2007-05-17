/*******************************************************************************
 * Copyright (c) 2007 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ARM Limited - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.model.IRegister;

/**
 * C/C++ specific extension of <code>IRegister</code>.
 * Added to be able to contribute a label provider. 
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=186981 
 */
public interface ICRegister extends ICVariable, IRegister {
}
