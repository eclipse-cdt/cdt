/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.rse.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.tcf.te.core.terminals.interfaces.ITerminalContextPropertiesProvider;
import org.eclipse.tcf.te.core.terminals.interfaces.constants.IContextPropertiesConstants;

/**
 * Terminal context properties provider implementation.
 */
public class TerminalContextPropertiesProvider implements ITerminalContextPropertiesProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.core.terminals.interfaces.ITerminalContextPropertiesProvider#getTargetAddress(java.lang.Object)
	 */
	@Override
	public Map<String, String> getTargetAddress(Object context) {
		if (context instanceof IHost) {
			IHost host = (IHost) context;

			Map<String, String> props = new HashMap<String, String>();
			props.put(IContextPropertiesConstants.PROP_ADDRESS, host.getHostName());
			props.put(IContextPropertiesConstants.PROP_NAME, host.getName());

			return props;
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.core.terminals.interfaces.ITerminalContextPropertiesProvider#getProperty(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object getProperty(Object context, String key) {
		if (context instanceof IHost) {
			IHost host = (IHost) context;

			if (IContextPropertiesConstants.PROP_DEFAULT_USER.equals(key)) {
				String user = host.getDefaultUserId();
				if (user != null && !"".equals(user.trim())) { //$NON-NLS-1$
					return user;
				}
			}

			if (IContextPropertiesConstants.PROP_DEFAULT_ENCODING.equals(key)) {
				String encoding = host.getDefaultEncoding(true);
				if (encoding != null && !"".equals(encoding)) { //$NON-NLS-1$
					return encoding;
				}
			}
		}
		return null;
	}

}
