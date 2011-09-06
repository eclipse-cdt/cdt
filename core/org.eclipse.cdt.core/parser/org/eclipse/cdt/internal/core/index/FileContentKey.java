/*******************************************************************************
 * Copyright (c) 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.ISignificantMacros;

/**
 * A key that uniquely determines the preprocessed contents of a file. 
 */
public class FileContentKey {
	private final int linkageID;
	private final IIndexFileLocation location;
	private final ISignificantMacros significantMacros;

	/**
	 * Creates a file content key.
	 * @param location the file location.
	 */
	public FileContentKey(int linkageID, IIndexFileLocation location, ISignificantMacros sigMacros) {
		this.linkageID= linkageID;
		this.location = location;
		this.significantMacros = sigMacros;
	}

	public int getLinkageID() {
		return linkageID;
	}

	public IIndexFileLocation getLocation() {
		return location;
	}

	public ISignificantMacros getSignificantMacros() {
		return significantMacros;
	}

	@Override
	public int hashCode() {
		return (linkageID + location.hashCode() * 31) * 31 + significantMacros.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileContentKey other = (FileContentKey) obj;
		if (linkageID != other.linkageID)
			return false;
		
		if (!location.equals(other.location)) 
			return false;
		
		if (!significantMacros.equals(other.significantMacros)) 
			return false;
	
        return true;
	}
	
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return linkageID + ": " + location.getURI().toString() + "[" + significantMacros.hashCode() + "]";
	}
}
