/**********************************************************************
 * Created on Mar 25, 2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.IContainerEntry;

public class ContainerEntry extends CPathEntry implements IContainerEntry {

	String id;

	public ContainerEntry(String id, boolean isExported) {
		super(IContainerEntry.CDT_CONTAINER, isExported);
		this.id = id;
	}

	/**
	 * Returns the id identifying this container.
	 * @return String
	 */
	public String getId() {
		return id;
	}

	public boolean equals(Object obj) {
		if (obj instanceof IContainerEntry) {
			IContainerEntry container = (IContainerEntry)obj;
			if (!super.equals(container)) {
				return false;
			}
			if (id == null) {
				if (container.getId() != null) {
					return false;
				}
			} else {
				if (!id.equals(container.getId())) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}

}
