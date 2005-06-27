/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IParent;

/**
 * A C element delta biulder creates a C element delta on a C element between
 * the version of the C element at the time the comparator was created and the
 * current version of the C element.
 *
 * It performs this operation by locally caching the contents of 
 * the C element when it is created. When the method buildDeltas() is called, it
 * creates a delta over the cached contents and the new contents.
 * 
 * This class is similar to the JDT CElementDeltaBuilder class.
 */

public class CElementDeltaBuilder {
	
	/**
	 * The c element handle
	 */
	ICElement cElement;

	/**
	 * The maximum depth in the C element children we should look into
	 */
	int maxDepth = Integer.MAX_VALUE;

	/**
	 * The old handle to info relationships
	 */
	Map infos;

	/**
	 * The old position info
	 */
	Map oldPositions;

	/**
	 * The new position info
	 */
	Map newPositions;

	/**
	 * Change delta
	 */
	CElementDelta delta;

	/**
	 * List of added elements
	 */
	ArrayList added;

	/**
	 * List of removed elements
	 */
	ArrayList removed;
	
	/**
	 * Doubly linked list item
	 */
	class ListItem {
		public ICElement previous;
		public ICElement next;

		public ListItem(ICElement previous, ICElement next) {
			this.previous = previous;
			this.next = next;
		}
	}
/**
 * Creates a C element comparator on a C element
 * looking as deep as necessary.
 */
public CElementDeltaBuilder(ICElement CElement) {
	this.cElement = CElement;
	this.initialize();
	this.recordElementInfo(CElement,0);
}
/**
 * Creates a C element comparator on a C element
 * looking only 'maxDepth' levels deep.
 */
public CElementDeltaBuilder(ICElement cElement, int maxDepth) {
	this.cElement = cElement;
	this.maxDepth = maxDepth;
	this.initialize();
	this.recordElementInfo(cElement,0);
}
/**
 * Repairs the positioning information
 * after an element has been added
 */
private void added(ICElement element) {
	this.added.add(element);
	ListItem current = this.getNewPosition(element);
	ListItem previous = null, next = null;
	if (current.previous != null)
		previous = this.getNewPosition(current.previous);
	if (current.next != null)
		next = this.getNewPosition(current.next);
	if (previous != null)
		previous.next = current.next;
	if (next != null)
		next.previous = current.previous;
}
/**
 * Builds the C element deltas between the old content of the translation unit
 * and its new content.
 */
public void buildDeltas() throws CModelException {
	this.recordNewPositions(this.cElement, 0);
	this.findAdditions(this.cElement, 0);
	this.findDeletions();
	this.findChangesInPositioning(this.cElement, 0);
	this.trimDelta(this.delta);
}
/**
 * Finds elements which have been added or changed.
 */
private void findAdditions(ICElement newElement, int depth) throws CModelException {
	CElementInfo oldInfo = this.getElementInfo(newElement);
	if (oldInfo == null && depth < this.maxDepth) {
		this.delta.added(newElement);
		added(newElement);
	} else {
		this.removeElementInfo(newElement);
	}
	
	if (depth >= this.maxDepth) {
		// mark element as changed
		this.delta.changed(newElement, ICElementDelta.F_CONTENT);
		return;
	}

	CElementInfo newInfo = null;
	newInfo = ((CElement)newElement).getElementInfo();
	
	this.findContentChange(oldInfo, newInfo, newElement);
		
	if (oldInfo != null && newElement instanceof IParent) {

		ICElement[] children = newInfo.getChildren();
		if (children != null) {
			int length = children.length;
			for(int i = 0; i < length; i++) {
				this.findAdditions(children[i], depth + 1);
			}
		}		
	}
}
/**
 * Looks for changed positioning of elements.
 */
private void findChangesInPositioning(ICElement element, int depth) throws CModelException {
	if (depth >= this.maxDepth || this.added.contains(element) || this.removed.contains(element))
		return;
		
	if (!isPositionedCorrectly(element)) {
		this.delta.changed(element, ICElementDelta.F_REORDER);
	} 
	
	if (element instanceof IParent) {
		CElementInfo info = null;
		info = ((CElement)element).getElementInfo();

		ICElement[] children = info.getChildren();
		if (children != null) {
			int length = children.length;
			for(int i = 0; i < length; i++) {
				this.findChangesInPositioning(children[i], depth + 1);
			}
		}		
	}
}
/**
 * The elements are equivalent, but might have content changes.
 */
private void findContentChange(CElementInfo oldInfo, CElementInfo newInfo, ICElement newElement) {
	if (oldInfo instanceof SourceManipulationInfo && newInfo instanceof SourceManipulationInfo) {
		SourceManipulationInfo oldSourceInfo = (SourceManipulationInfo) oldInfo;
		SourceManipulationInfo newSourceInfo = (SourceManipulationInfo) newInfo;
		
		if ((oldSourceInfo).getModifiers() != (newSourceInfo).getModifiers()) {
			this.delta.changed(newElement, ICElementDelta.F_MODIFIERS);
		}
		
		// The element info should be able to tell if the contents are the same. 
		if(!oldSourceInfo.hasSameContentsAs(newSourceInfo)){
			this.delta.changed(newElement, ICElementDelta.F_CONTENT);
		}
	}
}
/**
 * Adds removed deltas for any handles left in the table
 */
private void findDeletions() {
	Iterator iter = this.infos.keySet().iterator();
	while(iter.hasNext()) {
		ICElement element = (ICElement)iter.next();
		this.delta.removed(element);
		this.removed(element);
	}
}
private CElementInfo getElementInfo(ICElement element) {
	return (CElementInfo)this.infos.get(element);
}
private ListItem getNewPosition(ICElement element) {
	return (ListItem)this.newPositions.get(element);
}
private ListItem getOldPosition(ICElement element) {
	return (ListItem)this.oldPositions.get(element);
}
private void initialize() {
	this.infos = new HashMap(20);
	this.oldPositions = new HashMap(20);
	this.newPositions = new HashMap(20);
	this.putOldPosition(this.cElement, new ListItem(null, null));
	this.putNewPosition(this.cElement, new ListItem(null, null));
	this.delta = new CElementDelta(cElement);
	
	// if building a delta on a translation unit or below, 
	// it's a fine grained delta
	if (cElement.getElementType() >= ICElement.C_UNIT) {
		this.delta.fineGrained();
	}
	
	this.added = new ArrayList(5);
	this.removed = new ArrayList(5);
}
/**
 * Inserts position information for the elements into the new or old positions table
 */
private void insertPositions(ICElement[] elements, boolean isNew) {
	int length = elements.length;
	ICElement previous = null, current = null, next = (length > 0) ? elements[0] : null;
	for(int i = 0; i < length; i++) {
		previous = current;
		current = next;
		next = (i + 1 < length) ? elements[i + 1] : null;
		if (isNew) {
			this.putNewPosition(current, new ListItem(previous, next));
		} else {
			this.putOldPosition(current, new ListItem(previous, next));
		}
	}
}
/**
 * Returns true if the given elements represent the an equivalent declaration.
 *
 * <p>NOTE: Since this comparison can be done with handle info only,
 * none of the internal calls need to use the locally cached contents
 * of the old translation unit.
 */
//private boolean isIdentical(CElement e1, CElement e2) {
//	if (e1 == null ^ e2 == null)
//		return false;
//	
//	if (e1 == null)
//		return true;
//
//	return e1.isIdentical(e2);					
//}
/**
 * Answers true if the elements position has not changed.
 * Takes into account additions so that elements following
 * new elements will not appear out of place.
 */
private boolean isPositionedCorrectly(ICElement element) {
	ListItem oldListItem = this.getOldPosition(element);
	if (oldListItem == null) return false;
	
	ListItem newListItem = this.getNewPosition(element);
	if (newListItem == null) return false;
	
	ICElement oldPrevious = oldListItem.previous;
	ICElement newPrevious = newListItem.previous;
	
	if (oldPrevious == null) {
		return newPrevious == null;
	}
	return oldPrevious.equals(newPrevious);
}
private void putElementInfo(ICElement element, CElementInfo info) {
	this.infos.put(element, info);
}
private void putNewPosition(ICElement element, ListItem position) {
	this.newPositions.put(element, position);
}
private void putOldPosition(ICElement element, ListItem position) {
	this.oldPositions.put(element, position);
}
/**
 * Records this elements info, and attempts
 * to record the info for the children.
 */
private void recordElementInfo(ICElement element, int depth) {
	if (depth >= this.maxDepth) {
		return;
	}
	CElementInfo info = (CElementInfo)CModelManager.getDefault().getInfo(element);
	if (info == null) // no longer in the C model.
		return;
	this.putElementInfo(element, info);
		
	if (element instanceof IParent) {
		ICElement[] children = info.getChildren();
		if (children != null) {
			insertPositions(children, false);
			for(int i = 0, length = children.length; i < length; i++)
				recordElementInfo(children[i], depth + 1);
		}
	}
}
/**
 * Fills the newPositions hashtable with the new position information
 */
private void recordNewPositions(ICElement newElement, int depth) throws CModelException {
	if (depth < this.maxDepth && newElement instanceof IParent) {
		CElementInfo info = null;
		info = ((CElement)newElement).getElementInfo();

		ICElement[] children = info.getChildren();
		if (children != null) {
			insertPositions(children, true);
			for(int i = 0, length = children.length; i < length; i++) {
				recordNewPositions(children[i], depth + 1);
			}
		}
	}
}
/**
 * Repairs the positioning information
 * after an element has been removed
 */
private void removed(ICElement element) {
	this.removed.add(element);
	ListItem current = this.getOldPosition(element);
	ListItem previous = null, next = null;
	if (current.previous != null)
		previous = this.getOldPosition(current.previous);
	if (current.next != null)
		next = this.getOldPosition(current.next);
	if (previous != null)
		previous.next = current.next;
	if (next != null)
		next.previous = current.previous;
	
}
private void removeElementInfo(ICElement element) {
	this.infos.remove(element);
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("Built delta:\n"); //$NON-NLS-1$
	buffer.append(this.delta.toString());
	return buffer.toString();
}
/**
 * Trims deletion deltas to only report the highest level of deletion
 */
private void trimDelta(CElementDelta delta) {
	if (delta.getKind() == ICElementDelta.REMOVED) {
		ICElementDelta[] children = delta.getAffectedChildren();
		for(int i = 0, length = children.length; i < length; i++) {
			delta.removeAffectedChild((CElementDelta)children[i]);
		}
	} else {
		ICElementDelta[] children = delta.getAffectedChildren();
		for(int i = 0, length = children.length; i < length; i++) {
			trimDelta((CElementDelta)children[i]);
		}
	}
}
	
}
