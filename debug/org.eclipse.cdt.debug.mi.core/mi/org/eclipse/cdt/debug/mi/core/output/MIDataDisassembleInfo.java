/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

/**
 * GDB/MI disassemble parsing response.
 */
public class MIDataDisassembleInfo extends MIInfo {

	MISrcAsm[] src_asm;
	MIAsm[] asm;
	boolean mixed;

	public MIDataDisassembleInfo(MIOutput rr) {
		super(rr);
		mixed = false;
		parse();
	}

	public MIAsm[] getMIAsms() {
		return asm;
	}

	public boolean isMixed() {
		return mixed;
	}

	public MISrcAsm[] getMISrcAsms() {
		return src_asm;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("asm_insns=["); //$NON-NLS-1$
		
		if (isMixed()) {
			MISrcAsm[] array = getMISrcAsms();
			for (int i = 0; i < array.length; i++) {
				if (i != 0) {
					buffer.append(',');
				}
				buffer.append(array[i].toString());
			}
		} else {
			MIAsm[] array = getMIAsms();
			for (int i = 0; i < array.length; i++) {
				if (i != 0) {
					buffer.append(',');
				}
				buffer.append(array[i].toString());
			}
		}
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}

	void parse() {
		List asmList = new ArrayList();
		List srcList = new ArrayList();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("asm_insns")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIList) {
							parse((MIList)value, srcList, asmList);
						}
					}
				}
			}
		}
		src_asm = (MISrcAsm[])srcList.toArray(new MISrcAsm[srcList.size()]);
		asm = (MIAsm[])asmList.toArray(new MIAsm[asmList.size()]);
	}

	void parse(MIList list, List srcList, List asmList) {
		// src and assenbly is different
		
		// Mixed mode.
		MIResult[] results = list.getMIResults();
		if (results != null && results.length > 0) {
			for (int i = 0; i < results.length; i++) {
				String var = results[i].getVariable();
				if (var.equals("src_and_asm_line")) { //$NON-NLS-1$
					MIValue value = results[i].getMIValue();
					if (value instanceof MITuple) {
						srcList.add(new MISrcAsm((MITuple)value));
					}
				}
			}
			mixed = true;
		}

		// Non Mixed with source
		MIValue[] values = list.getMIValues();
		if (values != null && values.length > 0) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] instanceof MITuple) {
					asmList.add(new MIAsm((MITuple)values[i]));
				}
			}
			mixed = false;
		}
	}
}
