/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.filetype;

/**
 * Corresponds to an org.eclipse.cdt.core.CLanguage entry.
 */
public interface ICLanguage {

	/**
     * @return Id associated with this language.
     */
    public String getId();
	
	/**
     * @return Name of this language.
     */
    public String getName();
}
