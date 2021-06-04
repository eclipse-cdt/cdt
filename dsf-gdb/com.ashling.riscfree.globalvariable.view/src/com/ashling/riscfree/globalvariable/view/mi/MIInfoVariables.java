/**
 * 
 */
package com.ashling.riscfree.globalvariable.view.mi;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MICommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * @author vinod.appu
 *
 */
/**
 * -symbol-info-variables 
 * Returns the list of global variables for the current execution context.
 *
 */
public class MIInfoVariables extends MICommand<MIGlobalVariableInfo> {

	public MIInfoVariables(IDMContext ctx) {
		super(ctx, "-symbol-info-variables");
	}

	@Override
	public MIInfo getResult(MIOutput MIresult) {
		return new MIGlobalVariableInfo(MIresult);
	}

}
