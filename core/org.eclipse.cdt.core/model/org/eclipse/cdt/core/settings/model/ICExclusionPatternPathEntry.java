/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.runtime.IPath;

public interface ICExclusionPatternPathEntry extends ICPathEntry {
	/**
	 * Returns an array of inclusion paths affecting the
	 * source folder when looking for files recursively.
	 * @return IPath
	 */
	IPath[] getExclusionPatterns();
	
	/**
	 * Returns a char based representation of the exclusions patterns full path.
	 */
	public char[][] fullExclusionPatternChars();
}
