/**
 * 
 */
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.core.resources.IResourceDelta;

/**
 * In this case, no delta for specific element passed
 * Instead we'll notify Outline about offsets change.
 */
public class CShiftData implements ICElementDelta {

	public int fOffset;
	public int fSize;
	public int fLines;
	
	public CShiftData(int offset, int size, int lines) {
		fOffset = offset;
		fSize  = size;
		fLines = lines;
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
		return null;
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
		return ("CShiftData: offset=" + fOffset + ", size=" + fSize + ", lines=" + fLines);
	}
}
