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
package org.eclipse.dd.dsf.debug.service;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;

/**
 * Service for accessing register data.
 */
public interface IRegisters extends IDMService {
    
    /** Event indicating groups have changed. */
    public interface IGroupsChangedDMEvent extends IDMEvent<IRunControl.IExecutionDMContext> {}

    /** Register group context */
    public interface IRegisterGroupDMContext extends IDMContext<IRegisterGroupDMData> {}
    
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
    public interface IRegisterDMContext extends IDMContext<IRegisterDMData> {}
    
    /** Event indicating register value changed. */
    public interface IRegisterChangedDMEvent extends IDMEvent<IRegisterDMContext> {}
        
    /** Register information */
    public interface IRegisterDMData extends IDMData, INumericalValue {
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
    public interface IBitFieldDMContext extends IDMContext<IBitFieldDMData> {}

    /** Event indicating register value changed. */
    public interface IBitFieldChangedDMEvent extends IDMEvent<IBitFieldDMContext> {}
  
    /** 
     * Bitfield data, big groups and mnemonics are retrieved at the same 
     * time as rest of bit field data 
     */
    public interface IBitFieldDMData extends IDMData, INumericalValue {
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
    }

    /** Bit group definition */
    public interface IBitGroup {
        int startBit();
        int bitCount();
    }
 
    /** Bit field mnemonic */
    public interface IMnemonic extends INumericalValue {
        String getShortName();
        String getLongName();
    }
    
    /** 
     * Common interface for describing a number value for various register 
     * data objects 
     */
    public interface INumericalValue {
        String getNaturalValue();
        String getHexValue();
        String getOctalValue();
        String getBinaryValue();
    }
    
    /** 
     * Retrieves the list of register groups.
     * @param execCtx Execution DMC, this is required.
     * @param frameCtx Stack frame DMC, this is optional and may be null.
     * @param rm Request completion monitor.
     */
    void getRegisterGroups(IRunControl.IExecutionDMContext execCtx, IStack.IFrameDMContext frameCtx, DataRequestMonitor<IRegisterGroupDMContext[]> rm);
    
    /** 
     * Retrieves list of sub-groups of given register group. 
     * @param groupCtx Group DMC, this is required.
     * @param rm Request completion monitor.
     */
    void getRegisterSubGroups(IRegisterGroupDMContext groupCtx, DataRequestMonitor<IRegisterGroupDMContext[]> rm);
    
    /** 
     * Retrieves registers in given register group.
     * @param groupCtx Group DMC, this is required.
     * @param rm Request completion monitor.
     */
    void getRegisters(IRegisterGroupDMContext groupCtx, DataRequestMonitor<IRegisterDMContext[]> rm);
    
    /** 
     * Retrieves bit fields for given register
     * @param regCtx Register DMC, this is required.
     * @param rm Request completion monitor.
     */
    void getBitFields(IRegisterDMContext regCtx, DataRequestMonitor<IBitFieldDMContext[]> rm);
    
    /** 
     * Writes a register value for a given register to the target
     * @param regCtx Register DMC, this is required.
     * @param regValue Value of the register to be written.
     * @param formatId Format of the value to be written.
     * @param rm Request completion monitor.
     */
    void writeRegister(IRegisterDMContext regCtx, String regValue, String formatId, RequestMonitor rm);
    
    /** 
     * Writes a bit field value for a given bit field to the target
     * @param bitFieldCtx Bit field DMC, this is required.
     * @param bitFieldValue Value of the bit field to be written.
     * @param formatId Format of the value to be written.
     * @param rm Request completion monitor.
     */
    void writeBitField(IBitFieldDMContext bitFieldCtx, String bitFieldValue, String formatId, RequestMonitor rm);
    
    /** 
     * Writes a bit field value for a given bit field to the target
     * @param bitFieldCtx Bit field DMC, this is required.
     * @param mnemonic Mnemonic which represents the value to be written.
     * @param rm Request completion monitor.
     */
    void writeBitField(IBitFieldDMContext bitFieldCtx, IMnemonic mnemonic, RequestMonitor rm);
}
