package org.eclipse.cdt.debug.mi.core.output;

/**
 */
public class MIOutput {

	public static final String terminator = "(gdb)";
	public static final MIOOBRecord[] nullOOBRecord = new MIOOBRecord[0];
	MIResultRecord rr = null;
	MIOOBRecord[] oobs = nullOOBRecord;
 

	public MIResultRecord getMIResultRecord() {
		return rr;
	}

	public void setMIResultRecord(MIResultRecord res) {
		rr = res ;
	}

	public MIOOBRecord[] getMIOOBRecords() {
		return oobs;
	}

	public void setMIOOBRecords(MIOOBRecord [] bands) {
		oobs = bands;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < oobs.length; i++) {
			buffer.append(oobs[i].toString());
		}
		if (rr != null) {
			buffer.append(rr.toString());
		}
		return buffer.toString();
	}
}
