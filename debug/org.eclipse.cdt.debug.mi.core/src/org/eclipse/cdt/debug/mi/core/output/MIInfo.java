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

	public String toString() {
		if (miOutput != null) {
			return miOutput.toString();
		}
		return "";
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
					if (var.equals("msg")) {
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							String s = ((MIConst)value).getString();
							return s;
						}
					}
				}
			}
		}
		return "";
	}
}
