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
package org.eclipse.dd.dsf.model;

/**
 * Base implementation of the IDataModelContext interface.
 * <p>
 * TODO: consider merging the event interface with this class.
 */
public class DataModelEvent<V extends IDataModelContext> implements IDataModelEvent<V> {

    private V fModelContext;
    public DataModelEvent(V context) {
        fModelContext = context;
    }
    
    public V getDMC() {
        return fModelContext;
    }

}
