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
package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.*;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;

/**
 * The default behaviour of the register is autoupdate on.
 */
public interface ICDIRegisterManager extends ICDIManager {

	/**
	 * Method getRegisterObjects.
	 * @return ICDIRegisterObject[]
	 * @throws CDIException
	 */
	ICDIRegisterObject[] getRegisterObjects() throws CDIException;

	/**
	 * Method createRegister.
	 * @param stack
	 * @param reg
	 * @return ICDIRegister
	 * @throws CDIException
	 */
	ICDIRegister createRegister(ICDIRegisterObject reg) throws CDIException;

	/**
	 * Method removeRegister, remove register from the manager list.
	 * @param reg
	 */
	void destroyRegister(ICDIRegister reg);

}
