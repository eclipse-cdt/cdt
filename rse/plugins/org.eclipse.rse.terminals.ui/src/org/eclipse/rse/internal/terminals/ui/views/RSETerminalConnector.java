/********************************************************************************
 * Copyright (c) 2008, 2009 MontaVista Software, Inc. and others
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova (MontaVista) - initial API and implementation
 * Uwe Stieber (Wind River) - [282996] [terminal][api] Add "hidden" attribute to terminal connector extension point
 ********************************************************************************/
package org.eclipse.rse.internal.terminals.ui.views;

import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;

public class RSETerminalConnector extends RSETerminalConnectorImpl implements ITerminalConnector {

    public RSETerminalConnector(IHost host) {
		super(host);
    }

    public String getId() {
        return "rse_internal_connector"; //$NON-NLS-1$
    }

    public String getInitializationErrorMessage() {
        return null;
    }

    public String getName() {
        return "rse_internal_connector";
    }

    public boolean isHidden() {
    	return true;
    }

    public boolean isInitialized() {
        return true;
    }

	public Object getAdapter(Class adapterType) {
		if (adapterType.isInstance(this))
			return this;
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

}
