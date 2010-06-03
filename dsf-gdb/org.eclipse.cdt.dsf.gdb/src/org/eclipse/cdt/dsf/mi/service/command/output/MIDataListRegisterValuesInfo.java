/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataListRegisterValues;

/**
 * GDB/MI data list register values extraction.
 */
public class MIDataListRegisterValuesInfo extends MIInfo {

    MIRegisterValue[] registers;

    public MIDataListRegisterValuesInfo(MIOutput rr) {
        super(rr);
        registers = null;
        if (isDone()) {
            MIOutput out = getMIOutput();
            MIResultRecord outr = out.getMIResultRecord();
            if (outr != null) {
                MIResult[] results =  outr.getMIResults();
                for (int i = 0; i < results.length; i++) {
                    String var = results[i].getVariable();
                    if (var.equals("register-values")) { //$NON-NLS-1$
                        MIValue value = results[i].getMIValue();
                        if (value instanceof MIList) {
                            registers = MIRegisterValue.getMIRegisterValues((MIList)value);
                        }
                    }
                }
            }
        }
        if (registers == null) {
            registers = new MIRegisterValue[0];
        }
    }
    
    /*
     * Returns the array of registers values.
     */

    public MIRegisterValue[] getMIRegisterValues() {
        
        /*
         * The expectation is that we return an empty list. The
         * constructor quarantees this so we are good here.
         */
        return registers;
    }

    /**
     * Returns the desired subset of results. When this function is being called
     * the data here represents a coalesced request which is a superset of at 
     * least two original requests. We are extracting the data associated with 
     * the specified original request which we know is contained in this result.
     */
    @Override
    public <V extends ICommandResult> V getSubsetResult(ICommand<V> cmd) {
        if (cmd instanceof MIDataListRegisterValues) {
            MIDataListRegisterValues command = (MIDataListRegisterValues) cmd;
            List<MIRegisterValue> aList = new ArrayList<MIRegisterValue>();
            int[] wantedRegNos = command.getRegList();
            
            /*
             * Search through the larger answer set finding the ones we want.
             */
            for (MIRegisterValue regVal : registers) {
                for ( int curRegNo : wantedRegNos  ) {
                    if ( regVal.getNumber() == curRegNo ) {
                        aList.add( regVal );
                    }
                }
            }
            
            /*
             * Now construct a new complete answer.
             */
            MIRegisterValue[] finalRegSet = aList.toArray(new MIRegisterValue[aList.size()]);
            MIDataListRegisterValuesInfo finalSubset = new MIDataListRegisterValuesInfo( getMIOutput());
            finalSubset.registers = finalRegSet;
            
            @SuppressWarnings("unchecked")
            V vFinalSubset = (V)finalSubset;
            return vFinalSubset ;
        } else {
            return super.getSubsetResult(cmd);
        }
    }
}
