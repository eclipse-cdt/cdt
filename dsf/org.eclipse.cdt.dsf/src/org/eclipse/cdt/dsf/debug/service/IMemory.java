/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson AB - extended the API for IMemoryBlockExtension
 *     Ericsson AB - added support for 64 bit processors
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * Service for accessing memory.  Memory contexts are not meant to be 
 * represented in tree or table views, so it doesn't need to implement
 * IDMService interface. 
 * 
 * @since 1.0
 */
public interface IMemory extends IDsfService {

    public interface IMemoryDMContext extends IDMContext {}

    /**
     * Event generated every time a range of bytes is modified.
     * 
     * A client wishing to receive such events has to register as a service
     * event listener and implement the corresponding eventDispatched method.
     * 
     * E.g.:
     *
     *   MyMemoryBlock(MIRunControl fRunControl)
     *   {
     *       ...
     *       fRunControl.getSession().addServiceEventListener(MyMemoryBlock.this, null);
     *       ...
     *   }
     *     
     *     @DsfServiceEventHandler
     *     public void eventDispatched(MemoryChangedEvent e) {
     *        IDMContext<?> context = e.getContext();
     *        IAddress[] addresses = e.getAddresses();
     *        // do whatever...
     *     }
     */
    public interface IMemoryChangedEvent extends IDMEvent<IMemoryDMContext> {
        IAddress[] getAddresses();
    }
    
    /**
     * Reads a memory block from the target.
     * 
     * An asynchronous memory read request at [address] + [offset] for
     * [count] memory items, each of size [word_size] bytes, will be
     * issued to the target. The result will be stored in [drm] upon
     * completion of the call.
     * 
     * The [drm] result buffer will be of size [word_size] * [count]. The
     * successfully read bytes will have their MemoryByte.READABLE flag
     * set while the bytes in error (unreachable/bad memory) will have their
     * flag byte set to 0. The bytes will respect the target "endianness".
     * 
     * @param context	the context of the target memory block
     * @param address	the memory block address (on the target)
     * @param offset	the offset from the start address
     * @param word_size	the size, in bytes, of an addressable item
     * @param count		the number of data elements to read
     * @param drm		the asynchronous data request monitor
     */
    public void getMemory(IMemoryDMContext context, IAddress address, long offset,
    		int word_size, int count, DataRequestMonitor<MemoryByte[]> drm);

    /**
     * Writes a memory block on the target.
     * 
     * An asynchronous memory write request at [address] + [offset] for
     * [count] * [word_size] bytes will be issued to the target.
     * 
     * The [buffer] must hold at least [count] * [word_size] bytes.
     * 
     * A MemoryChangedEvent will be generated for the range of addresses.
     * 
     * @param context	the context of the target memory block
     * @param address	the memory block address (on the target)
     * @param offset	the offset from the start address
     * @param word_size	the size, in bytes, of an addressable item
     * @param count		the number of data elements to write
     * @param buffer	the source buffer
     * @param rm		the asynchronous data request monitor
     */
    public void setMemory(IMemoryDMContext context, IAddress address, long offset,
    		int word_size, int count, byte[] buffer, RequestMonitor rm);

    /**
     * Writes [pattern] at memory [address] + [offset], [count] times.
     * 
     * A MemoryChangedEvent will be generated for the range of addresses.
     * 
     * @param context	the context of the target memory block
     * @param address	the memory block address (on the target)
     * @param offset	the offset from the start address
     * @param word_size	the size, in bytes, of an addressable item
     * @param count		the number of times [pattern] will be written
     * @param pattern	the source buffer
     * @param rm		the asynchronous data request monitor
     */
    public void fillMemory(IMemoryDMContext context, IAddress address, long offset,
    		int word_size, int count, byte[] pattern, RequestMonitor rm);

}
