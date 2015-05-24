/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal;

import java.io.File;
import java.util.List;

import org.eclipse.cdt.arduino.core.ArduinoHome;
import org.eclipse.cdt.arduino.core.ArduinoProjectGenerator;
import org.eclipse.cdt.arduino.core.Board;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuiltinSpecsDetector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

public class AvrLanguageSettingsProvider extends GCCBuiltinSpecsDetector {

	@Override
	public String getToolchainId() {
		return "org.eclipse.cdt.arduino.toolChain.avr"; //$NON-NLS-1$
	}

	@Override
	protected String getToolOptions(String languageId) {
		String opts = super.getToolOptions(languageId);

		try {
			IConfiguration config = ManagedBuildManager.getConfigurationForDescription(currentCfgDescription);
			Board board = ArduinoProjectGenerator.getBoard(config);
			String mcu = board.getMCU();
			if (mcu != null) {
				opts += " -mmcu=" + mcu; //$NON-NLS-1$
			}
		} catch (CoreException e) {
			Activator.getPlugin().getLog().log(e.getStatus());
		}

		return opts;
	}
	
	@Override
	protected List<String> parseOptions(String line) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			if (line.startsWith(" /arduino/")) { //$NON-NLS-1$
				File full = new File(ArduinoHome.getArduinoHome().getParentFile(), line.trim());
				return parseOptions(" " + full.getAbsolutePath()); //$NON-NLS-1$
			}
		}

		return super.parseOptions(line);
	}
	
	@Override
	public AvrLanguageSettingsProvider cloneShallow() throws CloneNotSupportedException {
		return (AvrLanguageSettingsProvider) super.cloneShallow();
	}

	@Override
	public AvrLanguageSettingsProvider clone() throws CloneNotSupportedException {
		return (AvrLanguageSettingsProvider) super.clone();
	}

}
