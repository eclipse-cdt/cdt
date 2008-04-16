/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.model;

import java.io.File;

/**
 * Represents a source line in disassembly.
 */
public interface IDisassemblySourceLine extends IAsmSourceLine, IDisassemblyLine {
    
    public File getFile();
}
