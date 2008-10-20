/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.ui.viewmodel.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.actions.VMHandlerUtils;
import org.eclipse.dd.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.update.ICachingVMProvider;

public class RefreshHandler extends AbstractHandler  {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IVMProvider vmProvider = VMHandlerUtils.getActiveVMProvider(event);
        
        if (vmProvider instanceof ICachingVMProvider) {
            ((ICachingVMProvider)vmProvider).refresh();
        }
        
        return null;
    }
}
