/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.launcher;

import org.eclipse.cdt.testsrunner.launcher.BaseTestsLaunchDelegate;


/**
 * Launch delegate implementation that is used for Run mode.
 * @since 8.0
 */
public class RunTestsLaunchDelegate extends BaseTestsLaunchDelegate {
	
    @Override
    public String getPreferredDelegateId() {
        return "org.eclipse.cdt.cdi.launch.localCLaunch"; //$NON-NLS-1$
    }
	
}
