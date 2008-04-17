/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [180519][api] declaratively register adapter factories
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Yu-Fen Kuo       (MontaVista) - Adapted from  ShellServiceSubSystemConfigurationAdapterFactory
 ********************************************************************************/

package org.eclipse.rse.internal.terminals.ui.configuration.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.rse.subsystems.terminals.core.TerminalServiceSubSystemConfiguration;
import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;

public class TerminalServiceSubSystemConfigurationAdapterFactory implements
        IAdapterFactory {

    private ISubSystemConfigurationAdapter factoryAdapter;

    public TerminalServiceSubSystemConfigurationAdapterFactory() {
        super();
        factoryAdapter = new TerminalServiceSubSystemConfigurationAdapter();
    }

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        Object adapter = null;
        if (adaptableObject instanceof TerminalServiceSubSystemConfiguration)
            adapter = factoryAdapter;

        return adapter;
    }

    public Class[] getAdapterList() {
        return new Class[] { ISubSystemConfigurationAdapter.class };
    }

}
