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



/**
 * GDB/MI data read memor info extraction.
 */
public class MIDataReadMemoryInfo extends MIInfo {

	long addr;
	long nextRow;
	long prevRow;
	long nextPage;
	long prevPage;
	long numBytes;
	long totalBytes;
	MIMemory[] memories;
	
	
	public MIDataReadMemoryInfo(MIOutput rr) {
		super(rr);
		parse();
	}

	public long getAddress() {
		return addr;
	}

	public long getNumberBytes() {
		return numBytes;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public long getNextRow() {
		return nextRow;
	}

	public long getPreviousRow() {
		return prevRow;
	}

	public long getNextPage() {
		return nextPage;
	}

	public long getPreviousPage() {
		return prevPage;
	}

	public MIMemory[] getMemories() {
		return memories;
	}
/*
	public String toString() {
		MIMemory[] mem = getMemories();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < mem.length; i++) {
			buffer.append(Long.toHexString(mem[i].getAddress()));
			buffer.append(":");
			long[] data = mem[i].getData();
			for (int j = 0; j < data.length; j++) {
				buffer.append(" ").append(Long.toHexString(data[j]));
			}
			buffer.append("\t").append(mem[i].getAscii());
		}
		return buffer.toString();
	}
*/
	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					MIValue value = results[i].getMIValue();
					String str = ""; //$NON-NLS-1$
					if (value != null && value instanceof MIConst) {
						str = ((MIConst)value).getCString();
					}

					if (var.equals("addr")) { //$NON-NLS-1$
						try {
							addr = Long.decode(str.trim()).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("nr-bytes")) { //$NON-NLS-1$
						try {
							numBytes = Long.decode(str.trim()).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("total-bytes")) { //$NON-NLS-1$
						try {
							totalBytes = Long.decode(str.trim()).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("next-row")) { //$NON-NLS-1$
						try {
							nextRow = Long.decode(str.trim()).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("prev-row")) { //$NON-NLS-1$
						try {
							prevRow = Long.decode(str.trim()).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("next-page")) { //$NON-NLS-1$
						try {
							nextPage = Long.decode(str.trim()).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("prev-page")) { //$NON-NLS-1$
						try {
							prevPage = Long.decode(str.trim()).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("memory")) { //$NON-NLS-1$
						if (value instanceof MIList) { 
							parseMemory((MIList)value);
						}
					}
				}
			}
		}
		if (memories == null) {
			memories = new MIMemory[0];
		}
	}

	void parseMemory(MIList list) {
		MIValue[] values = list.getMIValues();
		memories = new MIMemory[values.length];
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MITuple) {
				memories[i] = new MIMemory((MITuple)values[i]);
			}
		}
	}
}
