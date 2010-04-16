/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.p2.touchpoint.natives.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.internal.p2.Activator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.touchpoint.natives.actions.ActionConstants;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;

/**
 * Action for post processing files to replace strings based on the install, such
 * as install location.
 * 
 * @author Doug Schaefer
 */
public class TranslateAction extends ProvisioningAction {

	private static final String PARM_MAP = "map";
	
	static int n;
	
	@Override
	public IStatus execute(Map<String, Object> parameters) {
		// The file to process
		String targetFileName = (String)parameters.get(ActionConstants.PARM_TARGET_FILE);
		File targetFile = new File(targetFileName);
		if (!targetFile.exists())
			return new Status(IStatus.WARNING, Activator.PLUGIN_ID, targetFileName + " not found");
		
		// The replacement map
		String mapString = (String)parameters.get(PARM_MAP);
		String[] mapStrings = mapString.split("!");
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < mapStrings.length; i += 2) {
			if (i == mapStrings.length - 1)
				// Odd number of strings
				break;
			map.put(mapStrings[i], mapStrings[i + 1]);
		}
		
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(targetFile));
			File tmpFile = new File(targetFile.getParentFile(), "translate" + (n++));
			FileWriter writer = new FileWriter(tmpFile);
			
			Pattern pattern = Pattern.compile("!(.*)!");
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					String value = map.get(matcher.group(1));
					if (value != null) {
						line = line.replace(matcher.group(), value);
						matcher.reset(line);
					}
				}
				writer.write(line);
				writer.write('\n');
			}
			
			reader.close();
			writer.close();
			
			targetFile.delete();
			tmpFile.renameTo(targetFile);
		} catch (IOException e) {
			return new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(Map<String, Object> parameters) {
		// No real undo since the file will likely be deleted.
		return Status.OK_STATUS;
	}

}
