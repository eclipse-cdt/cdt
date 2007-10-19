/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.datamodel;

import org.eclipse.dd.dsf.concurrent.Immutable;

/**
 * Base implementation of the IDMEvent interface.  It only handles the
 * required DM-Context reference.
 */
@Immutable
abstract public class AbstractDMEvent<V extends IDMContext> implements IDMEvent<V> {

    private final V fModelContext;
    public AbstractDMEvent(V context) {
        fModelContext = context;
    }
    
    public V getDMContext() {
        return fModelContext;
    }

}
