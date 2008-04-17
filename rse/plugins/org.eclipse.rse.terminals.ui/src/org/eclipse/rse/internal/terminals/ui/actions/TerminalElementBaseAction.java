/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Anna Dushistova (MontaVista) - Adapted from SystemBaseShellAction
 * Yu-Fen Kuo      (MontaVista) - Adapted from SystemBaseShellAction
 *******************************************************************************/

package org.eclipse.rse.internal.terminals.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.subsystems.terminals.core.elements.TerminalElement;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;

public abstract class TerminalElementBaseAction extends SystemBaseAction {
    protected List selected;

    public TerminalElementBaseAction(String name, String tooltip,
            ImageDescriptor image, Shell parent) {
        super(name, tooltip, image, parent);
        setAvailableOffline(true);
        allowOnMultipleSelection(true);
        selected = new ArrayList();
    }

    /**
     * Called when the selection changes. The selection is checked to make sure
     * this action can be performed on the selected object.
     */
    public boolean updateSelection(IStructuredSelection selection) {
        boolean enable = false;
        Iterator e = selection.iterator();
        selected.clear();
        while (e.hasNext()) {
            Object object = e.next();
            if (object instanceof TerminalElement) {
                if (isApplicable((TerminalElement) object)) {
                    selected.add(object);
                    enable = true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        return enable;
    }

    protected boolean isApplicable(TerminalElement element) {
        return true;
    }
}
