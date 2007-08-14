/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson Communication - extended the API for IMemoryBlockExtension
 *     Ericsson Communication - added support for 64 bit processors
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.service;

import java.math.BigInteger;

import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * Service for accessing memory.  Memory contexts are not meant to be 
 * represented in tree or table views, so it doesn't need to implement
 * IDMService interface. 
 */
public interface IMemory extends IDsfService {
    
    public class MemoryChangedEvent {
        BigInteger fAddress;

        public MemoryChangedEvent(BigInteger address) {
            fAddress = address;
        }

        public BigInteger getAddress() {
            return fAddress;
        }
    }

    
    /** Reads memory at the given location */
    public void getMemory(IDMContext<?> ctx, BigInteger addr, 
    	int word_size, MemoryByte[] buf, int offs, int size, int mode, RequestMonitor rm);

    /**  Writes the given value to the given memory location. */
    public void setMemory(IDMContext<?> ctx, BigInteger addr, 
        int word_size, byte[] buf, int offs, int size, int mode, RequestMonitor rm);

    /**
     * Fill target memory with given pattern.
     * 'size' is number of bytes to fill.
     * Parameter 0 of sequent 'done' is assigned with Throwable if
     * there was an error.
     */
    public void fillMemory(IDMContext<?> ctx, BigInteger addr,
        int word_size, byte[] value, int size, int mode, RequestMonitor rm);

}
