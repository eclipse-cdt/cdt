/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.service.stack;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.dd.examples.pda.service.command.commands.PDAFrame;

/**
 * 
 */
public class FrameDMData implements IFrameDMData {

    final private PDAFrame fFrame;
    
    FrameDMData(PDAFrame frame) {
        fFrame = frame;
    }
    
    public String getFile() {
        return fFrame.fFilePath.lastSegment();
    }

    public String getFunction() {
        return fFrame.fFunction;
    }

    public int getLine() {
        return fFrame.fLine + 1;
    }

    public int getColumn() {
        return 0;
    }

    public IAddress getAddress() {
        return null;
    }
}
