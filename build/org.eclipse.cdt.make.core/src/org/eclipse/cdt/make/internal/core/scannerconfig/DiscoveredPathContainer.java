/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredInfoListener;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerProjectDiscoveredPathInfo;
import org.eclipse.core.resources.IProject;

public class DiscoveredPathContainer extends AbstractDiscoveredPathContainer {
	static Map fgPathEntries;

	public DiscoveredPathContainer(IProject project) {
		super(project);
        initialize();
    }

    private static void initialize() {
        if (fgPathEntries == null) {
            fgPathEntries = new HashMap(10);

            IDiscoveredInfoListener listener = new IDiscoveredInfoListener() {

                public void infoRemoved(IDiscoveredPathInfo info) {
                    if (info != null && 
                            info instanceof IPerProjectDiscoveredPathInfo) {
                        fgPathEntries.remove(info.getProject());
                    }
                }

                public void infoChanged(IDiscoveredPathInfo info) {
                    if (info != null && 
                            info instanceof IPerProjectDiscoveredPathInfo) {
                        fgPathEntries.remove(info.getProject());
                    }
                }

            };
            MakeCorePlugin.getDefault().getDiscoveryManager().addDiscoveredInfoListener(listener);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig.AbstractDiscoveredPathContainer#getPathEntryMap()
     */
    protected Map getPathEntryMap() {
        return fgPathEntries;
    }

}
