/*******************************************************************************
 * Copyright (c) 2010 Nokia, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia
 *     Wind River Systems
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.provisional.service;

import org.eclipse.cdt.dsf.debug.service.IInstruction;

/**
 * a provisional API to provide access to size for use in CDT 7.0.x releases
 * to be replaced in CDT 8.x by moving getSize() to IInstruction
 * @author kirk.beitz@nokia.com
 * @since 2.1.1
 */
public interface IInstructionWithSize extends IInstruction {

    /**
    * @return size of the instruction
    */
    Integer getSize();

}
