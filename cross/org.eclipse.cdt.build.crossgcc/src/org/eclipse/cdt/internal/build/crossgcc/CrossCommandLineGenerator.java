package org.eclipse.cdt.internal.build.crossgcc;

import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator;

public class CrossCommandLineGenerator extends ManagedCommandLineGenerator {

	@Override
	public IManagedCommandLineInfo generateCommandLineInfo(ITool tool,
			String commandName, String[] flags, String outputFlag,
			String outputPrefix, String outputName, String[] inputResources,
			String commandLinePattern) {
		IToolChain toolchain = (IToolChain)tool.getParent();
		IOption option = toolchain.getOptionBySuperClassId("cdt.managedbuild.option.gnu.cross.prefix");
		String prefix = (String)option.getValue();
		String newCommandName = prefix + commandName;
		return super.generateCommandLineInfo(tool, newCommandName, flags, outputFlag,
				outputPrefix, outputName, inputResources, commandLinePattern);
	}
	
}
