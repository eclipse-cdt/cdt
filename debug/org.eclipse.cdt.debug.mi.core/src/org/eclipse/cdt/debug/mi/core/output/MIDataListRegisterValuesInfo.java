package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIDataListRegisterValuesInfo extends MIInfo {

	MIRegisterValue[] registers;

	public MIDataListRegisterValuesInfo(MIOutput rr) {
		super(rr);
	}

	MIRegisterValue[] getRegistersValues () {
		if (registers == null) {
			parse();
		}
		return registers;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("register-values")) {
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
}
