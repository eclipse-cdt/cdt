/*******************************************************************************
 * Copyright (c) 2011, 2012 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service.command;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.Messages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 4.1
 */
public class CustomTimeoutsMap extends HashMap<String, Integer> {

	public CustomTimeoutsMap() {
		super();
	}

	public CustomTimeoutsMap(CustomTimeoutsMap map) {
		super(map);
	}

	private static final long serialVersionUID = -8281280275781904870L;

	public String getMemento() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Integer> entry : entrySet()) {
			sb.append(entry.getKey());
			sb.append(',');
			sb.append(entry.getValue().intValue());
			sb.append(';');
		}
		return sb.toString();
	}

	public void initializeFromMemento(String memento) {
		clear();
		StringTokenizer st = new StringTokenizer(memento, ";"); //$NON-NLS-1$
		MultiStatus ms = new MultiStatus(GdbPlugin.PLUGIN_ID, 0,
				Messages.CustomTimeoutsMap_Error_initializing_custom_timeouts, null);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			String[] tokenParts = token.split(","); //$NON-NLS-1$
			if (tokenParts.length == 2 && tokenParts[0].length() > 0 && tokenParts[1].length() > 0) {
				try {
					put(tokenParts[0], Integer.valueOf(tokenParts[1]));
				} catch (NumberFormatException e) {
					ms.add(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
							String.format(Messages.CustomTimeoutsMap_Invalid_custom_timeout_value, tokenParts[0])));
				}
			} else {
				ms.add(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
						Messages.CustomTimeoutsMap_Invalid_custom_timeout_data));
			}
		}
		if (!ms.isOK()) {
			GdbPlugin.getDefault().getLog().log(ms);
		}
	}
}
