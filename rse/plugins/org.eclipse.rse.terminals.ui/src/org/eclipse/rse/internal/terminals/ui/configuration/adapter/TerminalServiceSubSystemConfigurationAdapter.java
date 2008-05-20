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
 * Yu-Fen Kuo (MontaVista) - Adapted from ShellServiceSubSystemConfigurationAdapter
 * Anna Dushistova  (MontaVista) - [227535] [rseterminal][api] terminals.ui should not depend on files.core
 ********************************************************************************/

package org.eclipse.rse.internal.terminals.ui.configuration.adapter;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.terminals.ui.Activator;
import org.eclipse.rse.ui.view.SubSystemConfigurationAdapter;

public class TerminalServiceSubSystemConfigurationAdapter extends
        SubSystemConfigurationAdapter {
    protected ImageDescriptor activeImageDescriptor;
    protected ImageDescriptor inactiveImageDescriptor;

    public ImageDescriptor getImage(ISubSystemConfiguration config) {
        if (inactiveImageDescriptor == null) {
            inactiveImageDescriptor = Activator.getDefault()
                    .getImageDescriptor(Activator.ICON_ID_TERMINAL_SUBSYSTEM);
        }
        return inactiveImageDescriptor;
    }

    public ImageDescriptor getLiveImage(ISubSystemConfiguration config) {
        if (activeImageDescriptor == null) {
            activeImageDescriptor = Activator.getDefault().getImageDescriptor(
                    Activator.ICON_ID_TERMINAL_SUBSYSTEM_LIVE);
        }
        return activeImageDescriptor;
    }

}