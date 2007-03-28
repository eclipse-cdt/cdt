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
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;

/**
 * Service for accessing register data.
 */
public interface IRegisters extends IDMService {

    /** Register group context */
    public interface IRegisterGroupDMContext extends IDMContext<IRegisterGroupDMData> {}
    
    /** 
     * Register groups only have a name.  Sub groups and registers are retrieved
     * through the service interface. 
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
    
    /** Retrieves list of sub-groups of given register group. */
    void getRegisterSubGroups(IRegisterGroupDMContext groupCtx, DataRequestMonitor<IRegisterGroupDMContext[]> rm);
    
    /** Retrieves registers in given register group. */
    void getRegisters(IRegisterGroupDMContext groupCtx, DataRequestMonitor<IRegisterDMContext[]> rm);
    
    /** Retrieves bit fields for given register */
    void getBitFields(IRegisterDMContext regCtx, DataRequestMonitor<IBitFieldDMContext[]> rm);
}
