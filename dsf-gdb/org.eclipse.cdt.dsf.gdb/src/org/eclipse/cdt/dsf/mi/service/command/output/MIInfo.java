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

import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;

/**
 * Base class for teh parsing/info GDB/MI classes.
 */
public class MIInfo implements ICommandResult {

    private final MIOutput miOutput;

    public MIInfo(MIOutput record) {
        miOutput = record;
    }

    public MIOutput getMIOutput () {
        return miOutput;
    }

    public boolean isDone() {
        return isResultClass(MIResultRecord.DONE);
    }

    public boolean isRunning() {
        return isResultClass(MIResultRecord.RUNNING);
    }

    public boolean isConnected() {
        return isResultClass(MIResultRecord.CONNECTED);
    }

    public boolean isError() {
        return isResultClass(MIResultRecord.ERROR);
    }

    public boolean isExit() {
        return isResultClass(MIResultRecord.EXIT);
    }

    @Override
    public String toString() {
        if (miOutput != null) {
            return miOutput.toString();
        }
        return ""; //$NON-NLS-1$
    }

    boolean isResultClass(String rc) {
        if (miOutput != null) {
            MIResultRecord rr = miOutput.getMIResultRecord();
            if (rr != null) {
                String clazz =  rr.getResultClass();
                return clazz.equals(rc);
            }
        }
        return false;
    }

    public String getErrorMsg() {
        if (miOutput != null) {
            MIResultRecord rr = miOutput.getMIResultRecord();
            if (rr != null) {
                MIResult[] results =  rr.getMIResults();
                for (int i = 0; i < results.length; i++) {
                    String var = results[i].getVariable();
                    if (var.equals("msg")) { //$NON-NLS-1$
                        MIValue value = results[i].getMIValue();
                        if (value instanceof MIConst) {
                            String s = ((MIConst)value).getCString();
                            return s;
                        }
                    }
                }
            }
        }
        return ""; //$NON-NLS-1$
    }
    
    /**
     * Default implementation of this method returns null, which means that a subset
     * result canot be calculated for the given command.  Deriving classes should
     * override this method as needed.
     */
    public <V extends ICommandResult> V getSubsetResult(ICommand<V> command) {
        return null;
    }
}
