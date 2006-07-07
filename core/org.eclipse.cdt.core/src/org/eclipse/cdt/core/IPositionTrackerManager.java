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

package org.eclipse.cdt.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

/**
 * An interface to manage the position tracking. It allows for mapping character 
 * offsets from a file previously stored on disk to the offset in the current document
 * for the file.
 * 
 * <p> This interface is not intended to be implemented by clients. </p>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * </p>
 */
public interface IPositionTrackerManager {
    /**
     * Returns the position converter suitable for mapping character offsets of the
     * given file/timestamp to the current version of it.
     * 
     * @param file a file for which the position adapter is requested.
     * @param timestamp identifies the version of the file stored on disk.
     * @return the requested position converter or <code>null</code>.
     */
    public IPositionConverter findPositionConverter(IFile file, long timestamp);
    
    /**
     * Returns the position tracker suitable for mapping character offsets of the
     * given external file/timestamp to the current version of it. <p> 
     * The method can be used for resources by supplying the <b>full path</b>. However,
     * it does not work if you supply the location of a resource.
     * 
     * @param externalLocationOrFullPath an external location for which the position adapter is requested.
     * @param timestamp identifies the version of the file stored on disk.
     * @return the requested position converter or <code>null</code>.
     */
    public IPositionConverter findPositionConverter(IPath fullPathOrExternalLocation, long timestamp);
}
