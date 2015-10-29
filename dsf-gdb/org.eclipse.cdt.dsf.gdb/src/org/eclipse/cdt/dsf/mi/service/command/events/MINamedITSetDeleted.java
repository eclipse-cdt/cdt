package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * @since 5.0
 */
public class MINamedITSetDeleted extends MIEvent<ICommandControlDMContext> {
	
	final private String fName;
	
	public MINamedITSetDeleted(ICommandControlDMContext dmc, String name) {
		this(dmc, 0, name);
	}
	
	public MINamedITSetDeleted(ICommandControlDMContext dmc, int token, String name) {
		super(dmc, token, null);
		fName = name;
	}
	
	public String getName() {
		return fName;
	}
}
