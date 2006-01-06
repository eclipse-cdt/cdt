/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.core.resources.IResourceDelta;

/**
 * In this case, no delta for specific element passed
 * Instead we'll notify Outline about offsets change.
 * 
 * @author Oleg Krasilnikov
 */
public class CShiftData implements ICElementDelta {

	private final ICElement element;
	private final int offset;
	private final int size;
	private final int lines;
	
	public CShiftData(ICElement element, int offset, int size, int lines) {
		this.element = element;
		this.offset = offset;
		this.size  = size;
		this.lines = lines;
	}

	public int getOffset() {
		return offset;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getLines() {
		return lines;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElementDelta#getAddedChildren()
	 */
	public ICElementDelta[] getAddedChildren() {
		return new ICElementDelta[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElementDelta#getAffectedChildren()
	 */
	public ICElementDelta[] getAffectedChildren() {
		return new ICElementDelta[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElementDelta#getChangedChildren()
	 */
	public ICElementDelta[] getChangedChildren() {
		return new ICElementDelta[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElementDelta#getElement()
	 */
	public ICElement getElement() {
		return element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElementDelta#getFlags()
	 */
	public int getFlags() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElementDelta#getKind()
	 */
	public int getKind() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElementDelta#getMovedFromElement()
	 */
	public ICElement getMovedFromElement() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElementDelta#getMovedToElement()
	 */
	public ICElement getMovedToElement() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElementDelta#getRemovedChildren()
	 */
	public ICElementDelta[] getRemovedChildren() {
		return new ICElementDelta[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElementDelta#getResourceDeltas()
	 */
	public IResourceDelta[] getResourceDeltas() {
		return null;
	}

	public String toString() {
		return ("CShiftData: offset=" + offset + ", size=" + size + ", lines=" + lines);
	}
}
