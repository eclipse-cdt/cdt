/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.HashMap;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IWorkingSet;

public class WorkingSetFilter {

    private static final Object ACCEPT = new Object();
    private static final Object REJECT = new Object();

    private HashMap fResourceFilter= null;

    public synchronized boolean isPartOfWorkingSet(ICElement elem) {
        if (fResourceFilter == null) {
            return true;
        }
        IPath path= elem.getPath();
        if (path == null) {
            return false;
        }
        Object check= fResourceFilter.get(path);
        if (check == null) {
            check= checkWorkingSet(path);
            fResourceFilter.put(path, check);
        }
        return check == ACCEPT;
    }

    public synchronized boolean isPartOfWorkingSet(IPath resourceOrExternalPath) {
        if (fResourceFilter == null) {
            return true;
        }
        if (resourceOrExternalPath == null) {
            return false;
        }
        Object check= fResourceFilter.get(resourceOrExternalPath);
        if (check == null) {
            check= checkWorkingSet(resourceOrExternalPath);
            fResourceFilter.put(resourceOrExternalPath, check);
        }
        return check == ACCEPT;
    }

    private synchronized Object checkWorkingSet(IPath path) {
        if (path.segmentCount() == 0) {
            return REJECT;
        }

        Object result= fResourceFilter.get(path);
        if (result == null) {
            result= checkWorkingSet(path.removeLastSegments(1));
            fResourceFilter.put(path, result);
        }
        return result;
    }

    public synchronized void setWorkingSet(IWorkingSet workingSetFilter) {
        if (workingSetFilter == null) {
            fResourceFilter= null;
        }
        else {
            IAdaptable[] input = workingSetFilter.getElements();
            fResourceFilter = new HashMap();
            for (int i = 0; i < input.length; i++) {
                IAdaptable adaptable = input[i];
                IResource res = (IResource) adaptable.getAdapter(IResource.class);
                if (res != null) {
                    fResourceFilter.put(res.getFullPath(), ACCEPT);
                }
            }
        }
    }
}
