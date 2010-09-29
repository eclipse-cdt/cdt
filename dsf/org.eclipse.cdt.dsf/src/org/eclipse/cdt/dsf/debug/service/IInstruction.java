/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service;

import java.math.BigInteger;

/**
 * Represents an assembly instruction.
 * <p>
 * Implementers should extend {@link AbstractInstruction} instead of
 * implementing this interface directly.
 * </p>
 * 
 * @since 1.0
 * @see IInstructionWithSize
 */
public interface IInstruction {

    /**
     * @return the instruction address.
     */
    BigInteger getAdress();
    
    /**
     * @return the function name.
     */
    String getFuntionName();
    
    /**
     * @return the offset of this machine instruction
     */
    long getOffset();

    /**
     * @return the instruction.
     */
    String getInstruction();
  
    /**
    * @return the opcode
    */
    String getOpcode();

    /**
    * @return any arguments to the instruction
    */
    String getArgs();

}
