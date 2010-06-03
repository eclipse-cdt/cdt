/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.update.provisional;

import org.eclipse.cdt.dsf.ui.viewmodel.update.ViewModelUpdateMessages;


/**
 * An "automatic" update policy which causes the view model provider cache to 
 * be flushed whenever an event causes a delta to be generated in the given 
 * model.
 * 
 * @since 1.1
 */
public class AllUpdateScope implements IVMUpdateScope {

    public static String ALL_UPDATE_SCOPE_ID = "org.eclipse.cdt.dsf.ui.viewmodel.update.allUpdateScope";  //$NON-NLS-1$
    
    public String getID() {
        return ALL_UPDATE_SCOPE_ID;
    }

    public String getName() {
        return ViewModelUpdateMessages.AllUpdateScope_name;
    }
}
