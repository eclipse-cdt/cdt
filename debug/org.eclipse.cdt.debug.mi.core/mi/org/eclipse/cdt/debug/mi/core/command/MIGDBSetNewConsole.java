package org.eclipse.cdt.debug.mi.core.command;

public class MIGDBSetNewConsole extends MIGDBSet {

	public MIGDBSetNewConsole(String miVersion) {
		this(miVersion, "on");
	}
	
	public MIGDBSetNewConsole(String miVersion, String param) {
		super(miVersion, new String[] {"new-console", param}); //$NON-NLS-1$
	}

}
