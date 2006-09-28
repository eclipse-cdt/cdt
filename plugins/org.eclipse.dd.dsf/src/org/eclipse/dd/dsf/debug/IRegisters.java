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
package org.eclipse.dd.dsf.debug;

import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.model.IDataModelContext;
import org.eclipse.dd.dsf.model.IDataModelData;
import org.eclipse.dd.dsf.model.IDataModelEvent;
import org.eclipse.dd.dsf.model.IDataModelService;

/**
 * Service for accessing register data.
 */
public interface IRegisters extends IDataModelService {

    /** Register group context */
    public interface IRegisterGroupDMC extends IDataModelContext<IRegisterGroupData> {}
    
    /** 
     * Register groups only have a name.  Sub groups and registered are retrieved
     * through the service interface. 
     */
    public interface IRegisterGroupData extends IDataModelData {
        public String getName();
        public String getDescription();
    }

    /** Register context */
    public interface IRegisterDMC extends IDataModelContext<IRegisterData> {}
    
    /** Event indicating register value changed. */
    public interface IRegisterChangedEvent extends IDataModelEvent<IRegisterDMC> {}
        
    /** Register information */
    public interface IRegisterData extends IDataModelData, INumericalValue {
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
    public interface IBitFieldDMC extends IDataModelContext<IBitFieldData> {}

    /** 
     * Bitfield data, big groups and mnemonics are retrieved at the same 
     * time as rest of bit field data 
     */
    public interface IBitFieldData extends IDataModelData, INumericalValue {
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
     * @param done Return token.
     */
    void getRegisterGroups(IRunControl.IExecutionDMC execCtx, IStack.IFrameDMC frameCtx, GetDataDone<IRegisterGroupDMC[]> done);
    
    /** Retrieves list of sub-groups of given register group. */
    void getRegisterSubGroups(IRegisterGroupDMC groupCtx, GetDataDone<IRegisterGroupDMC[]> done);
    
    /** Retrieves registers in given register group. */
    void getRegisters(IRegisterGroupDMC groupCtx, GetDataDone<IRegisterDMC[]> done);
    
    /** Retrieves bit fields for given register */
    void getBitFields(IRegisterDMC regCtx, GetDataDone<IBitFieldDMC[]> done);
}
