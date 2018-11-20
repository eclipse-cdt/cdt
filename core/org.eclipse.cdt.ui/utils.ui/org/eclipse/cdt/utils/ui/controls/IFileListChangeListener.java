/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.ui.controls;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public interface IFileListChangeListener {
	void fileListChanged(FileListControl fileList, String oldValue[], String newValue[]);
}
