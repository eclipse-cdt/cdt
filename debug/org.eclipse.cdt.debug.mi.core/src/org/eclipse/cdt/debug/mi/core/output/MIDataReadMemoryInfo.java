package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;



/**
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
	}

	public long getAddress() {
		if  (memories == null) {
			parse();
		}
		return addr;
	}

	public long getNumberBytes() {
		if  (memories == null) {
			parse();
		}
		return numBytes;
	}

	public long getTotalBytes() {
		if  (memories == null) {
			parse();
		}
		return totalBytes;
	}

	public long getNextRow() {
		if  (memories == null) {
			parse();
		}
		return nextRow;
	}

	public long getPreviousRow() {
		if  (memories == null) {
			parse();
		}
		return prevRow;
	}

	public long getNextPage() {
		if  (memories == null) {
			parse();
		}
		return nextPage;
	}

	public long getPreviousPage() {
		if  (memories == null) {
			parse();
		}
		return prevPage;
	}

	public MIMemory[] getMemories() {
		if (memories == null) {
			parse();
		}
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
					String str = "";
					if (value != null && value instanceof MIConst) {
						str = ((MIConst)value).getString();
					}

					if (var.equals("addr")) {
						try {
							addr = Long.decode(str).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("nr-bytes")) {
						try {
							numBytes = Long.decode(str).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("total-bytes")) {
						try {
							totalBytes = Long.decode(str).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("next-row")) {
						try {
							nextRow = Long.decode(str).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("prev-row")) {
						try {
							prevRow = Long.decode(str).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("next-page")) {
						try {
							nextPage = Long.decode(str).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("prev-page")) {
						try {
							prevPage = Long.decode(str).longValue();
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("memory")) {
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
