package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIInfo {

	MIOutput miOutput;

	public MIInfo(MIOutput record) {
		miOutput = record;
	}

	MIOutput getMIOutput () {
		return miOutput;
	}
}
