/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
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