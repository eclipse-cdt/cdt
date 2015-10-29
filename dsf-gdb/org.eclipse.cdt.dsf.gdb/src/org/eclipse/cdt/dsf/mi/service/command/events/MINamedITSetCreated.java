package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;


/**
 * @since 5.0
 */
public class MINamedITSetCreated extends MIEvent<ICommandControlDMContext> {

	final private String fName;
	final private String fSpec;
	
	public MINamedITSetCreated(ICommandControlDMContext dmc, String name, String spec) {
		this(dmc, 0, name, spec);
	}
	
	public MINamedITSetCreated(ICommandControlDMContext dmc, int token, String name, String spec) {
		super(dmc, token, null);
		fName = name;
		fSpec = spec;
	}
	
	public String getName() {
		return fName;
	}

	public String getSpec() {
		return fSpec;
	}
}
