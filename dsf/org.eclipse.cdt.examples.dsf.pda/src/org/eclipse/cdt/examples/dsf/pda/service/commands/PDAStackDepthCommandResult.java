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
 * @see PDADataCommand
 */
@Immutable
public class PDAStackDepthCommandResult extends PDACommandResult {
    
    final public int fDepth;
    
    PDAStackDepthCommandResult(String response) {
        super(response);
        int depth = 1; // default to something that won't cause NPEs
        try {
            depth = Integer.parseInt(response);
        } catch (NumberFormatException e) {}
        fDepth = depth;
    }
}
