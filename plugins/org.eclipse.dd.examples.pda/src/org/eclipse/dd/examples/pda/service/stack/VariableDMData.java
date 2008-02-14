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

import org.eclipse.dd.dsf.debug.service.IStack.IVariableDMData;

/**
 * 
 */
public class VariableDMData implements IVariableDMData {

    final private String fVariable;
    
    VariableDMData(String variable) {
        fVariable = variable;
    }
    
    public String getName() {
        return fVariable;
    }

    public String getValue() {
        return null;
    }
}
