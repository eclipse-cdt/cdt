package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;

public class CustomOptionCommandGenerator2 extends CustomOptionCommandGenerator {

	public CustomOptionCommandGenerator2() {
	}

	@Override
	public String generateCommand(IOption option, IVariableSubstitutor macroSubstitutor) {
		String command = super.generateCommand(option, macroSubstitutor);
		if (command == null) {
			try {
				switch (option.getValueType()) {
				case IOption.BOOLEAN:
					return option.getBooleanValue() ? option.getCommand() : option.getCommandFalse();
				case IOption.ENUMERATED:
					return option.getEnumCommand(option.getSelectedEnum());
				case IOption.TREE:
					return option.getCommand(option.getStringValue());
				default:
					return option.getCommand() + option.getValue();
				}
			} catch (BuildException e) {
				return "CustomOptionCommandGenerator2-error";
			}
		}
		return command;
	}

}
