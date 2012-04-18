/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov.
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
 * Launch delegate implementation that redirects its queries to DSF.
 */
public class DsfGdbRunTestsLaunchDelegate extends BaseTestsLaunchDelegate {
	
    @Override
    public String getPreferredDelegateId() {
        return "org.eclipse.cdt.dsf.gdb.launch.localCLaunch"; //$NON-NLS-1$
    }
	
}
