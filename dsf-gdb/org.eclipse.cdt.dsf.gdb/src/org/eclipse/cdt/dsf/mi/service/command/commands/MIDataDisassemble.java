/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson  - Modified for DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataDisassembleInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * -data-disassemble
 *       [ -s START-ADDR -e END-ADDR ]
 *     | [ -f FILENAME -l LINENUM [ -n LINES ] ]
 *       -- MODE
 *
 * Where:
 *
 * '-s START-ADDR'
 *     is the beginning address (or '$pc')
 *
 * '-e END-ADDR'
 *     is the end address
 *
 * '-f FILENAME'
 *     is the name of the file to disassemble
 *
 * '-l LINENUM'
 *     is the line number to disassemble around
 *
 * -n LINES'
 *     is the the number of disassembly lines to be produced.  If it is
 *     -1, the whole function will be disassembled, in case no END-ADDR is
 *     specified.  If END-ADDR is specified as a non-zero value, and
 *     LINES is lower than the number of disassembly lines between
 *     START-ADDR and END-ADDR, only LINES lines are displayed; if LINES
 *     is higher than the number of lines between START-ADDR and
 *     END-ADDR, only the lines up to END-ADDR are displayed.
 *
 * '-- MODE'
 *     is either 0 (meaning only disassembly) or 1 (meaning mixed source
 *     and disassembly).
 */

public class MIDataDisassemble extends MICommand<MIDataDisassembleInfo> {

    public MIDataDisassemble(IDisassemblyDMContext ctx, String start, String end, boolean mode) {
        super(ctx, "-data-disassemble"); //$NON-NLS-1$
        setOptions(new String[]{"-s", start, "-e", end}); //$NON-NLS-1$ //$NON-NLS-2$
        String mixed = "0"; //$NON-NLS-1$
        if (mode) {
            mixed = "1"; //$NON-NLS-1$
        }
        setParameters(new String[]{mixed});
    }

    public MIDataDisassemble(IDisassemblyDMContext ctx, String file, int linenum, int lines, boolean mode) {
        super(ctx, "-data-disassemble"); //$NON-NLS-1$
        setOptions(new String[]{"-f", file, "-l", //$NON-NLS-1$ //$NON-NLS-2$
             Integer.toString(linenum), "-n", Integer.toString(lines)}); //$NON-NLS-1$
        String mixed = "0"; //$NON-NLS-1$
        if (mode) {
            mixed = "1"; //$NON-NLS-1$
        }
        setParameters(new String[]{mixed}); 
    }

    /*
     * GDB the -data-disassemble uses "--" as a separator wit only the MODE
     * So override the MICommand
     */
    @Override
    protected String parametersToString() {
        String[] parameters = getParameters();
        if (parameters != null && parameters.length > 0) {
            return "-- " + parameters[0]; //$NON-NLS-1$
        }
        return new String();
    }

    @Override
    public MIDataDisassembleInfo getResult(MIOutput output) {
        return new MIDataDisassembleInfo(output);
    }
}
