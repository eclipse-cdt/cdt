/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.dsf.concurrent;

import org.eclipse.dd.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

public class InputCoalescingSlowDataProviderAction extends ActionDelegate 
    implements IWorkbenchWindowActionDelegate 
{
    private IWorkbenchWindow fWindow;
    
    @Override
    public void run(IAction action) {
        if (fWindow != null) {
            // Create the standard data provider.
            final InputCoalescingSlowDataProvider dataProvider = 
                new InputCoalescingSlowDataProvider(new DefaultDsfExecutor());
            
            // Create the dialog and open it.
            Dialog dialog = new SlowDataProviderDialog(
                fWindow.getShell(), new SlowDataProviderContentProvider(), dataProvider);
            dialog.open();
            
            // Shut down the data provider thread and the DSF executor thread.
            // Note, since data provider is running in background thread, we have to
            // wait until this background thread has completed shutdown before
            // killing the executor thread itself.
            dataProvider.shutdown(new RequestMonitor(dataProvider.getDsfExecutor(), null) { 
                @Override
                public void handleCompleted() {
                    dataProvider.getDsfExecutor().shutdown();
                }
            });
        }
    }

    public void init(IWorkbenchWindow window) {
        fWindow = window;
    }
}
