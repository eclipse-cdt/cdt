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

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

/**
 * The parsed output of the data-disassemble command. The output format is
 * determined by the mode field on the request.
 * 
 * -data-disassemble -s $pc -e "$pc + 20" -- 0
 * ^done,asm_insns=[
 * {address="0x000107c0",func-name="main",offset="4",inst="mov 2, %o0"},
 * {address="0x000107c4",func-name="main",offset="8",inst="sethi %hi(0x11800), %o2"},
 * {address="0x000107c8",func-name="main",offset="12",inst="or %o2, 0x140, %o1\t! 0x11940 <_lib_version+8>"},
 * {address="0x000107cc",func-name="main",offset="16",inst="sethi %hi(0x11800), %o2"},
 * {address="0x000107d0",func-name="main",offset="20",inst="or %o2, 0x168, %o4\t! 0x11968 <_lib_version+48>"}]
 * 
 * -data-disassemble -f basics.c -l 32 -- 0
 * ^done,asm_insns=[
 * {address="0x000107bc",func-name="main",offset="0",inst="save %sp, -112, %sp"},
 * {address="0x000107c0",func-name="main",offset="4",inst="mov   2, %o0"},
 * {address="0x000107c4",func-name="main",offset="8",inst="sethi %hi(0x11800), %o2"},
 * [...]
 * {address="0x0001081c",func-name="main",offset="96",inst="ret "},
 * {address="0x00010820",func-name="main",offset="100",inst="restore "}]
 * 
 * -data-disassemble -f basics.c -l 32 -n 3 -- 1
 * ^done,asm_insns=[
 * src_and_asm_line={line="31",file="/dir1/dir2/basics.c",line_asm_insn=[
 * {address="0x000107bc",func-name="main",offset="0",inst="save %sp, -112, %sp"}]},
 * src_and_asm_line={line="32",file="/dir1/dir2/basics.c",line_asm_insn=[
 * {address="0x000107c0",func-name="main",offset="4",inst="mov 2, %o0"},
 * {address="0x000107c4",func-name="main",offset="8",inst="sethi %hi(0x11800), %o2"}]}]
 * 
 */

public class MIDataDisassembleInfo extends MIInfo {

    // The parsed information
    private boolean mixed;
    private MIMixedInstruction[] mixedCode;
    private MIInstruction[] assemblyCode;

    public MIDataDisassembleInfo(MIOutput record) {
        super(record);
        mixed = false;
        parse();
    }

    public boolean isMixed() {
        return mixed;
    }

    public MIInstruction[] getMIAssemblyCode() {
        return assemblyCode;
    }

    public MIMixedInstruction[] getMIMixedCode() {
        return mixedCode;
    }

    /**
     *  Find the relevant tag in the output record ("asm_insns") and then
     *  parse its value.
     */
    private void parse() {
        List<MIInstruction> asmList = new ArrayList<MIInstruction>();
        List<MIMixedInstruction> srcList = new ArrayList<MIMixedInstruction>();

        if (isDone()) {
            MIOutput out = getMIOutput();
            MIResultRecord rr = out.getMIResultRecord();
            if (rr != null) {
                MIResult[] results =  rr.getMIResults();
                // Technically, there should be only one field (asm_insns), but just in case...
                for (int i = 0; i < results.length; i++) {
                    String var = results[i].getVariable();
                    if (var.equals("asm_insns")) { //$NON-NLS-1$
                        MIValue value = results[i].getMIValue();
                        if (value instanceof MIList) {
                            parseResult((MIList) value, srcList, asmList);
                        }
                    }
                }
            }
        }

        assemblyCode = asmList.toArray(new MIInstruction[asmList.size()]);
        mixedCode = srcList.toArray(new MIMixedInstruction[srcList.size()]);
    }

    /**
     *  Parse the back-end result. Depending on the requested mode
     *  ("-- 0" or "-- 1" on the request), the result has one of the
     *  following forms:
     *  
     *  [1] Mode == 0 (assembly instructions only)
     *      {address="0x000107c0",func-name="main",offset="4",inst="mov 2, %o0"},
     *      {address="0x000107c4",func-name="main",offset="8",inst="sethi %hi(0x11800), %o2"},
     *      ...,
     *      {address="0x00010820",func-name="main",offset="100",inst="restore "}
     * 
     *  [2] Mode == 1 (Mixed source and assembly code)
     *      src_and_asm_line={
     *          line="31",file="/dir1/dir2/basics.c",
     *          line_asm_insn=[
     *          {address="0x000107c0",func-name="main",offset="4",inst="mov 2, %o0"},
     *          {address="0x000107c4",func-name="main",offset="8",inst="sethi %hi(0x11800), %o2"},
     *          ...,
     *          {address="0x00010820",func-name="main",offset="100",inst="restore "}
     *          ]
     *      },
     *      ...,
     *      src_and_asm_line={
     *          line="31",file="/dir1/dir2/basics.c",
     *          line_asm_insn=[
     *          ...,
     *          ]
     *      }
     */
    private void parseResult(MIList list,
            List<MIMixedInstruction> srcList, List<MIInstruction> asmList) {

        // Mixed mode (with source)
        MIResult[] results = list.getMIResults();
        if (results != null && results.length > 0) {
            for (int i = 0; i < results.length; i++) {
                String var = results[i].getVariable();
                if (var.equals("src_and_asm_line")) { //$NON-NLS-1$
                    MIValue value = results[i].getMIValue();
                    if (value instanceof MITuple) {
                        srcList.add(new MIMixedInstruction((MITuple) value));
                    }
                }
            }
            mixed = true;
        }

        // Non-mixed mode
        MIValue[] values = list.getMIValues();
        if (values != null && values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof MITuple) {
                    asmList.add(new MIInstruction((MITuple) values[i]));
                }
            }
            mixed = false;
        }

    }

}
