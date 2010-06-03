/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.service.commands;

import org.eclipse.cdt.dsf.concurrent.Immutable;


/**
 * @see PDAFrameCommand
 */
@Immutable
public class PDAFrameCommandResult extends PDACommandResult {
    
    /**
     * Frame data return by the frame command.
     */
    final public PDAFrame fFrame;
    
    PDAFrameCommandResult(String response) {
        super(response);
        fFrame = new PDAFrame(response);
    }
}
