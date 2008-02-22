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
package org.eclipse.dd.examples.pda.service;

import org.eclipse.dd.dsf.datamodel.AbstractDMEvent;

/**
 * Event issued when the PDA debugger exits.
 */
public class PDATerminatedEvent extends AbstractDMEvent<PDAProgramDMContext> {
    PDATerminatedEvent(PDAProgramDMContext context) {
        super(context);
    }
}
