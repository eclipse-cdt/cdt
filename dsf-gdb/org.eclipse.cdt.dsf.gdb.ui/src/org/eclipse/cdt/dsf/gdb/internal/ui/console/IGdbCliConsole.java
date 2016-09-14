/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.ui.console.IConsole;

public interface IGdbCliConsole extends IConsole {
	/**
	 * Enable or disable the inverted color option of the console.
	 */
    void setInvertedColors(boolean enable);
    
    /**
     * Reflect this preference in the console UI
     */
    void setAutoTerminateGDB(boolean autoTerminate);
    
    /**
     * Update the console to hold the specified number of lines in the buffer
     */
    void setBufferLineLimit(int bufferLines);
}
