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


/**
 * The Data Model Context representing the owner service, which is returned by 
 * {@link IDMService#getServiceContext()} methods.  The service DM Context
 * should be the parent of all contexts originating from the given service.
 */
public class ServiceDMContext extends AbstractDMContext {
    String fServiceDMID;
    
    public ServiceDMContext(IDMService service, String serviceDMID) {
        super(service, new IDMContext[0]);
        fServiceDMID = serviceDMID; 
    }
    
    @Override
    public String toString() { return baseToString() + fServiceDMID; }

    @Override
    public boolean equals(Object obj) { 
        return obj instanceof ServiceDMContext && fServiceDMID.equals(((ServiceDMContext)obj).fServiceDMID); 
    }
    
    @Override
    public int hashCode() { 
        return fServiceDMID.hashCode(); 
    }

}
