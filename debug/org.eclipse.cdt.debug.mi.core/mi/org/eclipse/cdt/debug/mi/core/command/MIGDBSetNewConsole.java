package org.eclipse.cdt.debug.mi.core.command;

public class MIGDBSetNewConsole extends MIGDBSet {

	public MIGDBSetNewConsole(String miVersion) {
		super(miVersion, new String[] {"new-console"}); //$NON-NLS-1$
	}

}
