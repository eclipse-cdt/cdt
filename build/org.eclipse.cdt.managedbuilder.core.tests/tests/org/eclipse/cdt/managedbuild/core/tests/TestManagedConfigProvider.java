/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuild.core.tests;

import java.util.Iterator;
import java.util.Properties;

import org.eclipse.cdt.managedbuilder.core.IConfigurationV2;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElementProvider;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;

public class TestManagedConfigProvider implements IManagedConfigElementProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigProvider#getConfigElements()
	 */
	public IManagedConfigElement[] getConfigElements() {
		try {
			Properties props = new Properties();
			props.load(getClass().getResourceAsStream("test_commands"));
			IManagedConfigElement[] ret = new IManagedConfigElement[props.size()];
			Iterator it = props.keySet().iterator();
			int i = 0;
			while (it.hasNext()) {
				String targetId = (String)it.next();
				String command = props.getProperty(targetId);
				ret[i++] = createTarget(targetId, command);
			}
			return ret;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new IManagedConfigElement[0];
	}

	private IManagedConfigElement createTarget(String targetId, String command) {
		IManagedConfigElement toolRef = new TestConfigElement(
			IConfigurationV2.TOOLREF_ELEMENT_NAME, 
			new String[][] {
					{ITool.ID, "test.forward.tool"},
					{ITool.COMMAND, command}},
			new IManagedConfigElement[0]);

		IManagedConfigElement config = new TestConfigElement(
				IConfigurationV2.CONFIGURATION_ELEMENT_NAME, 
				new String[][] {
						{IConfigurationV2.ID, targetId + ".config"},
						{IConfigurationV2.NAME, "test.forward.config"}},
				new IManagedConfigElement[] {toolRef});
				
		IManagedConfigElement target = new TestConfigElement(
			ITarget.TARGET_ELEMENT_NAME,
			new String[][] {
					{ITarget.ID, targetId},
					{ITarget.NAME, targetId.substring(targetId.lastIndexOf('.')+1).
						replace('_', ' ')},
					{ITarget.PARENT, "test.forward.parent.target"},
					{ITarget.IS_TEST, "true"},
					{ITarget.OS_LIST, "win32,linux,solaris"}},
			new IManagedConfigElement[] {config});
		
		return target;
	}
}
