package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIInfo {

	MIOutput miOutput;

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
}
