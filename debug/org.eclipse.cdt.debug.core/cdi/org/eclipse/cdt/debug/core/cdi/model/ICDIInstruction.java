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

import java.math.BigInteger;


/**
 * 
 * Represents a machine instruction.
 * 
 * @since Jul 10, 2002
 */
public interface ICDIInstruction extends ICDIObject {
	/**
	 * Returns the Address.
	 * @return the address.
	 */
	BigInteger getAdress();
	
	/**
	 * @return the function name.
	 */
	String getFuntionName();
	
	/**
	 * @return the instruction.
	 */
	String getInstruction();
  
	/**
	* @return the opcode
	*/
	String getOpcode();

	/**
	* @return any arguments to the opcode
	*/
	String getArgs();

	/**
	 * @return the offset of this machine instruction
	 * Returns the instruction's offset.
	 * 
	 * @return the offset of this machine instruction
	 */
	long getOffset();
}
