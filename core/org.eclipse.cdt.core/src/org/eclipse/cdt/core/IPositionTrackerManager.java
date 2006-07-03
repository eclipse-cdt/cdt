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

/**
 * An interface to manage the position tracking that allows for mapping character 
 * offsets from a file previously stored on disk to the current offset.
 */
public interface IPositionTrackerManager {
    /**
     * Returns the position tracker suitable for mapping character offsets of the
     * given file/timestamp to the current version of it.
     * 
     * @param file a file for which the position adapter is requested.
     * @param timestamp identifies the version of the file stored on disk.
     * @return the requested position adapter or <code>null</code>.
     */
    IPositionConverter findPositionAdapter(IFile file, long timestamp);
}
