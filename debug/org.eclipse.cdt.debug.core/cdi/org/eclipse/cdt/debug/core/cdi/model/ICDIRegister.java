/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

/**
 * 
 * A register is a special kind of variable that is contained
 * in a register group. Each register has a name and a value.
 * 
 * @since Jul 9, 2002
 */
public interface ICDIRegister extends ICDIVariable, ICDIRegisterDescriptor {
}
