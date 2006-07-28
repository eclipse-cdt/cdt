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
    public interface RegisterGroupDMC extends IDataModelContext<RegisterGroupData> {}
    
    /** 
     * Register groups only have a name.  Sub groups and registered are retrieved
     * through the service interface. 
     */
    public interface RegisterGroupData extends IDataModelData {
        public String getName();
    }

    /** Register context */
    public interface RegisterDMC extends IDataModelContext<RegisterData> {}
    
    /** Event indicating register value changed. */
    public interface RegisterChangedEvent extends IDataModelEvent<RegisterDMC> {}
        
    /** Register information */
    public interface RegisterData extends IDataModelData, NumericalValue {
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
    public interface BitFieldDMC extends IDataModelContext<BitFieldData> {}

    /** 
     * Bitfield data, big groups and mnemonics are retrieved at the same 
     * time as rest of bit field data 
     */
    public interface BitFieldData extends IDataModelData, NumericalValue {
        String getName();
        String getDescription();
        boolean isReadable();
        boolean isReadOnce();
        boolean isWriteable();
        boolean isWriteOnce();
        boolean hasSideEffects();
        boolean isZeroBasedNumbering();
        boolean isZeroBitLeftMost();
        BitGroup[] getBitGroup();
        Mnemonic[] getMnemonics();
    }

    /** Bit group definition */
    public interface BitGroup {
        int startBit();
        int bitCount();
    }
 
    /** Bit field mnemonic */
    public interface Mnemonic extends NumericalValue {
        String getShortName();
        String getLongName();
    }
    
    /** 
     * Common interface for describing a number value for various register 
     * data objects 
     */
    public interface NumericalValue {
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
    void getRegisterGroups(IRunControl.IExecutionDMC execCtx, IStack.IFrameDMC frameCtx, GetDataDone<RegisterGroupDMC[]> done);
    
    /** Retrieves list of sub-groups of given register group. */
    void getRegisterSubGroups(RegisterGroupDMC groupCtx, GetDataDone<RegisterGroupDMC[]> done);
    
    /** Retrieves registers in given register group. */
    void getRegisters(RegisterGroupDMC groupCtx, GetDataDone<RegisterDMC[]> done);
    
    /** Retrieves bit fields for given register */
    void getBitFields(RegisterDMC regCtx, GetDataDone<BitFieldDMC[]> done);
}
