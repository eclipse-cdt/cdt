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
 * Common interface for events that signify changes in the data model.  
 * The sub-classes should contain specific information about the event, while
 * this base class only identifies the DMC that is affected.
 * @param <V> DMC that is affected by this event.
 */
public interface IDataModelEvent <V extends IDataModelContext> {
    V getDMC();
}
