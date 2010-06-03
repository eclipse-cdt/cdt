/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;

/**
 * Service for accessing register data.
 * 
 * @since 1.0
 */
public interface IRegisters extends IFormattedValues {
    
    /** 
     * Event indicating groups have changed.  The type of context returned by this 
     * event is generic, because different implementations of the the register service 
     * could configure register groups using different contexts.  Some implementations
     * could configure different register groups for each execution context, other 
     * services may have a global list of groups.   
     */
    public interface IGroupsChangedDMEvent extends IDMEvent<IDMContext> {}

    /** Register group context */
    public interface IRegisterGroupDMContext extends IFormattedDataDMContext {
    }
    
    /** Event indicating values for the group have changed. */
    public interface IGroupChangedDMEvent extends IDMEvent<IRegisterGroupDMContext> {}
    
    /** Event indicating registers in a group have changed. */
    public interface IRegistersChangedDMEvent extends IDMEvent<IRegisterGroupDMContext> {}
    
    /** 
     * Register groups only have a name and description.  Sub groups and registers are
     * retrieved through the service interface. 
     */
    public interface IRegisterGroupDMData extends IDMData {
        public String getName();
        public String getDescription();
    }

    /** Register context */
    public interface IRegisterDMContext extends IFormattedDataDMContext {
    }
    
    /** Event indicating register value changed. */
    public interface IRegisterChangedDMEvent extends IDMEvent<IRegisterDMContext> {}
        
    /** Register information */
    public interface IRegisterDMData extends IDMData {
        String getName();
        String getDescription();
        boolean isReadable();
        boolean isReadOnce();
        boolean isWriteable();
        boolean isWriteOnce();
        boolean hasSideEffects();
        boolean isVolatile();
        boolean isFloat();
    }

    /** Bit field context */
    public interface IBitFieldDMContext extends IFormattedDataDMContext {
    }

    /** Event indicating register value changed. */
    public interface IBitFieldChangedDMEvent extends IDMEvent<IBitFieldDMContext> {}
  
    /** 
     * Bitfield data, big groups and mnemonics are retrieved at the same 
     * time as rest of bit field data 
     */
    public interface IBitFieldDMData extends IDMData {
        String getName();
        String getDescription();
        boolean isReadable();
        boolean isReadOnce();
        boolean isWriteable();
        boolean isWriteOnce();
        boolean hasSideEffects();
        boolean isZeroBasedNumbering();
        boolean isZeroBitLeftMost();
        IBitGroup[] getBitGroup();
        IMnemonic[] getMnemonics();
        IMnemonic getCurrentMnemonicValue();
    }

    /** Bit group definition */
    public interface IBitGroup {
        int startBit();
        int bitCount();
    }
 
    /** Bit field mnemonic */
    public interface IMnemonic {
        String getShortName();
        String getLongName();
    }
    
    /** 
     * Retrieves the list of register groups.
     * @param ctx Context for the returned data.
     * @param rm Request completion monitor.
     */
    void getRegisterGroups(IDMContext ctx, DataRequestMonitor<IRegisterGroupDMContext[]> rm);
    
    /** 
     * Retrieves the list of registers for the given context.  The given context could include
     * a register group and an execution context or just an execution context, in which case all
     * registers for all groups should be returned.
     * @param ctx Context for the returned data.
     * @param rm Request completion monitor.
     */
    void getRegisters(IDMContext ctx, DataRequestMonitor<IRegisterDMContext[]> rm);
    
    /** 
     * Retrieves bit fields for given register
     * @param ctx Context for the returned data.
     * @param rm Request completion monitor.
     */
    void getBitFields(IDMContext ctx, DataRequestMonitor<IBitFieldDMContext[]> rm);
    
    /** 
     * Retrieves a Register Group context. The given context could include a register
     * group and an execution context or just an execution context.
     * @param ctx Context for the returned data.
     * @param name Name of group being requested
     * @param rm Request completion monitor.
     */
    void findRegisterGroup(IDMContext ctx, String name, DataRequestMonitor<IRegisterGroupDMContext> rm);
    
    /** 
     * Retrieves a Register context. The given context could include a register group and an execution 
     * context or just an execution context.
     * @param ctx Context for the returned data.
     * @param name Name of register being requested
     * @param rm Request completion monitor.
     */
    void findRegister(IDMContext ctx, String name, DataRequestMonitor<IRegisterDMContext> rm);
    
    /** 
     * Retrieves bit field context. The given context could include a register and an execution 
     * context or just an execution context.
     * @param ctx Context for the returned data.
     * @param name Name of bit field being requested
     * @param rm Request completion monitor.
     */
    void findBitField(IDMContext ctx, String name, DataRequestMonitor<IBitFieldDMContext> rm);
    
    /**
     * Retrieves register group data for given context.
     * @param dmc Context to retrieve data for.
     * @param rm Request completion monitor.
     */
    void getRegisterGroupData(IRegisterGroupDMContext dmc, DataRequestMonitor<IRegisterGroupDMData> rm);

    /**
     * Retrieves register data for given context.
     * @param dmc Context to retrieve data for.
     * @param rm Request completion monitor.
     */
    void getRegisterData(IRegisterDMContext dmc , DataRequestMonitor<IRegisterDMData> rm);

    /**
     * Retrieves bit field data for given context.
     * @param dmc Context to retrieve data for.
     * @param rm Request completion monitor.
     */
    void getBitFieldData(IBitFieldDMContext dmc , DataRequestMonitor<IBitFieldDMData> rm);
    
    
    
    /** 
     * Writes a register value for a given register to the target
     * @param regCtx Context containing the register.
     * @param regValue Value of the register to be written.
     * @param formatId Format of the value to be written.
     * @param rm Request completion monitor.
     */
    void writeRegister(IRegisterDMContext regCtx, String regValue, String formatId, RequestMonitor rm);
    
    /** 
     * Writes a bit field value for a given bit field to the target
     * @param bitFieldCtx Context containing the bit field.
     * @param bitFieldValue Value of the bit field to be written.
     * @param formatId Format of the value to be written.
     * @param rm Request completion monitor.
     */
    void writeBitField(IBitFieldDMContext bitFieldCtx, String bitFieldValue, String formatId, RequestMonitor rm);
    
    /** 
     * Writes a bit field value for a given bit field to the target
     * @param bitFieldCtx Context containing the bit field.
     * @param mnemonic Mnemonic which represents the value to be written.
     * @param rm Request completion monitor.
     */
    void writeBitField(IBitFieldDMContext bitFieldCtx, IMnemonic mnemonic, RequestMonitor rm);
}
