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

import org.eclipse.dd.dsf.service.AbstractDsfService;

/**
 * The Data Model Context representing the owner service.  The service DM Context
 * should be the parent of all contexts originating from the given service.
 * <p>
 * Note: there should be only one instance of ServiceContext per service, so there
 * is no need to implement the equals() methods.
 */
public class ServiceDMContext<V extends IDMService> extends AbstractDMContext<V> {
    String fServiceDMID;
    
    public ServiceDMContext(AbstractDsfService service, String serviceDMID) {
        super(service, null);
        fServiceDMID = serviceDMID; 
    }
    
    public String toString() { return baseToString() + fServiceDMID; }

}
