package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.core.resources.IResourceDelta;


/**
 * @see ICElementDelta
 */
public class CElementDelta implements ICElementDelta {
	/**
	 * The element that this delta describes the change to.
	 * @see #getElement()
	 */
	protected ICElement fChangedElement;

	/**
	 * @see #getKind()
	 */
	private int fKind = 0;

	/**
	 * @see #getFlags()
	 */
	private int fChangeFlags = 0;

	/**
	 * @see #getMovedFromHandle()
	 */
	protected ICElement fMovedFromHandle = null;

	/**
	 * @see #getMovedToHandle()
	 */
	protected ICElement fMovedToHandle = null;

	/**
	 * Collection of resource deltas that correspond to non c resources deltas.
	 */
	protected IResourceDelta[] resourceDeltas = null;

	/**
	 * Counter of resource deltas
	 */
	protected int resourceDeltasCounter;

	/**
	 * Empty array of ICElementDelta
	 */
	protected static  ICElementDelta[] fgEmptyDelta= new ICElementDelta[] {};

	/**
	 * @see #getAffectedChildren()
	 */
	protected ICElementDelta[] fAffectedChildren = fgEmptyDelta;

	/**
	 * Creates the root delta. To create the nested delta
	 * hierarchies use the following convenience methods. The root
	 * delta can be created at any level (i.e. project, folder).
	 * <ul>
	 * <li><code>added(ICElement)</code>
	 * <li><code>changed(ICElement)</code>
	 * <li><code>moved(ICElement, ICElement)</code>
	 * <li><code>removed(ICElement)</code>
	 * <li><code>renamed(ICElement, ICElement)</code>
	 * </ul>
	 */
	public CElementDelta(ICElement element) {
		super();
		fChangedElement = element;
	}

	/**
	 * Adds the child delta to the collection of affected children.  If the
	 * child is already in the collection, walk down the hierarchy.
	 */
	protected void addAffectedChild(CElementDelta child) {
		switch (fKind) {
			case ADDED:
			case REMOVED:
				// no need to add a child if this parent is added or removed
				return;
			case CHANGED:
				fChangeFlags |= F_CHILDREN;
				break;
			default:
				fKind = CHANGED;
				fChangeFlags |= F_CHILDREN;
		}

		// if a child delta is added to a translation unit delta or below, 
		// it's a fine grained delta
		if (!(fChangedElement.getElementType() >= ICElement.C_UNIT)) {
			fineGrained();
		}
	
		if (fAffectedChildren.length == 0) {
			fAffectedChildren = new ICElementDelta[] {child};
			return;
		}

		// Check if we already have the delta.
		ICElementDelta existingChild = null;
		int existingChildIndex = -1;
		for (int i = 0; i < fAffectedChildren.length; i++) {
			// handle case of two jars that can be equals but not in the same project
			if (equalsAndSameParent(fAffectedChildren[i].getElement(), child.getElement())) {
				existingChild = fAffectedChildren[i];
				existingChildIndex = i;
				break;
			}
		}

		if (existingChild == null) { //new affected child
			fAffectedChildren= growAndAddToArray(fAffectedChildren, child);
		} else {
			switch (existingChild.getKind()) {
				case ADDED:
					switch (child.getKind()) {
						// child was added then added -> it is added
						case ADDED:
						// child was added then changed -> it is added
						case CHANGED:
							return;

						// child was added then removed -> noop
						case REMOVED:
							fAffectedChildren = removeAndShrinkArray(fAffectedChildren, existingChildIndex);
							return;
					}
					break;
				case REMOVED:
					switch (child.getKind()) {
						// child was removed then added -> it is changed
						case ADDED:
							child.fKind = CHANGED;
							fAffectedChildren[existingChildIndex] = child;
							return;

						// child was removed then changed -> it is removed
						case CHANGED:
						// child was removed then removed -> it is removed
						case REMOVED:
							return;
					}
					break;
				case CHANGED:
					switch (child.getKind()) {
						// child was changed then added -> it is added
						case ADDED:
						// child was changed then removed -> it is removed
						case REMOVED:
							fAffectedChildren[existingChildIndex] = child;
							return;

						// child was changed then changed -> it is changed
						case CHANGED:
							ICElementDelta[] children = child.getAffectedChildren();
							for (int i = 0; i < children.length; i++) {
								CElementDelta childsChild = (CElementDelta) children[i];
								((CElementDelta) existingChild).addAffectedChild(childsChild);
							}
							// add the non-c resource deltas if needed
							// note that the child delta always takes
							// precedence over this existing child delta
							// as non-c resource deltas are always
							// created last (by the DeltaProcessor)
							IResourceDelta[] resDeltas = child.getResourceDeltas();
							if (resDeltas != null) {
								((CElementDelta)existingChild).resourceDeltas = resDeltas;
								((CElementDelta)existingChild).resourceDeltasCounter = child.resourceDeltasCounter;
							}
							return;
					}
					break;
				default: 
					// unknown -> existing child becomes the child with the existing child's flags
					int flags = existingChild.getFlags();
					fAffectedChildren[existingChildIndex] = child;
					child.fChangeFlags |= flags;
			}
		}
	}

	/**
	 * Creates the nested deltas resulting from an add operation.
	 * Convenience method for creating add deltas.
	 * The constructor should be used to create the root delta 
	 * and then an add operation should call this method.
	 */
	public void added(ICElement element) {
		CElementDelta addedDelta = new CElementDelta(element);
		addedDelta.fKind = ADDED;
		insertDeltaTree(element, addedDelta);
	}

	/**
	 * Adds the child delta to the collection of affected children.  If the
	 * child is already in the collection, walk down the hierarchy.
	 */
	protected void addResourceDelta(IResourceDelta child) {
		switch (fKind) {
			case ADDED:
			case REMOVED:
				// no need to add a child if this parent is added or removed
				return;
			case CHANGED:
				fChangeFlags |= F_CONTENT;
				break;
			default:
				fKind = CHANGED;
				fChangeFlags |= F_CONTENT;
		}
		if (resourceDeltas == null) {
			resourceDeltas = new IResourceDelta[5];
			resourceDeltas[resourceDeltasCounter++] = child;
			return;
		}
		if (resourceDeltas.length == resourceDeltasCounter) {
			// need a resize
			System.arraycopy(resourceDeltas, 0, (resourceDeltas = new IResourceDelta[resourceDeltasCounter * 2]), 0, resourceDeltasCounter);
		}
		resourceDeltas[resourceDeltasCounter++] = child;
	}

	/**
	 * Creates the nested deltas resulting from a change operation.
	 * Convenience method for creating change deltas.
	 * The constructor should be used to create the root delta 
	 * and then a change operation should call this method.
	 */
	public void changed(ICElement element, int changeFlag) {
		CElementDelta changedDelta = new CElementDelta(element);
		changedDelta.fKind = CHANGED;
		changedDelta.fChangeFlags |= changeFlag;
		insertDeltaTree(element, changedDelta);
	}

	/**
	 * Creates the nested deltas for a closed element.
	 */
	public void closed(ICElement element) {
		CElementDelta delta = new CElementDelta(element);
		delta.fKind = CHANGED;
		delta.fChangeFlags |= F_CLOSED;
		insertDeltaTree(element, delta);
	}

	/**
	 * Returns whether the two c elements are equals and have the same parent.
	 */
	protected boolean equalsAndSameParent(ICElement e1, ICElement e2) {
		ICElement parent1;
		return e1.equals(e2) && ((parent1 = e1.getParent()) != null) && parent1.equals(e2.getParent());
	}

	/**
	 * Creates the nested delta deltas based on the affected element
	 * its delta, and the root of this delta tree. Returns the root
	 * of the created delta tree.
	 */
	protected CElementDelta createDeltaTree(ICElement element, CElementDelta delta) {
		CElementDelta childDelta = delta;
		ArrayList ancestors= getAncestors(element);
		if (ancestors == null) {
			if (equalsAndSameParent(delta.getElement(), getElement())) {
				// handle case of two jars that can be equals but not in the
				// same project
				// the element being changed is the root element
				fKind= delta.fKind;
				fChangeFlags = delta.fChangeFlags;
				fMovedToHandle = delta.fMovedToHandle;
				fMovedFromHandle = delta.fMovedFromHandle;
			} else {
				// the given delta is not the root or a child - illegal
				//Assert.isTrue(false);
			}
		} else {
			for (int i = 0, size = ancestors.size(); i < size; i++) {
				ICElement ancestor = (ICElement) ancestors.get(i);
				CElementDelta ancestorDelta = new CElementDelta(ancestor);
				ancestorDelta.addAffectedChild(childDelta);
				childDelta = ancestorDelta;
			}
		}
		return childDelta;
	}

	/**
	 * Returns the <code>CElementDelta</code> for the given element
	 * in the delta tree, or null, if no delta for the given element is found.
	 */
	protected CElementDelta find(ICElement e) {
		if (equalsAndSameParent(fChangedElement, e)) { // handle case of two jars that can be equals but not in the same project
			return this;
		}
		for (int i = 0; i < fAffectedChildren.length; i++) {
			CElementDelta delta = ((CElementDelta)fAffectedChildren[i]).find(e);
			if (delta != null) {
				return delta;
			}
		}
		return null;
	}

	/**
	 * Mark this delta as a fine-grained delta.
	 */
	public void fineGrained() {
		fChangeFlags |= F_FINE_GRAINED;
	}

	/**
	 * @see ICElementDelta
	 */
	public ICElementDelta[] getAddedChildren() {
		return getChildrenOfType(ADDED);
	}

	/**
	 * @see ICElementDelta
	 */
	public ICElementDelta[] getAffectedChildren() {
		return fAffectedChildren;
	}

	/**
	 * Returns a collection of all the parents of this element up to (but
	 * not including) the root of this tree in bottom-up order. If the given
	 * element is not a descendant of the root of this tree, <code>null</code>
	 * is returned.
	 */
	private ArrayList getAncestors(ICElement element) {
		ICElement parent = element.getParent();
		if (parent == null) {
			return null;
		}
		ArrayList parents = new ArrayList();
		while (!parent.equals(fChangedElement)) {
			parents.add(parent);
			parent = parent.getParent();
			if (parent == null) {
				return null;
			}
		}
		parents.trimToSize();
		return parents;
	}

	/**
	 * @see ICElementDelta
	 */
	public ICElementDelta[] getChangedChildren() {
		return getChildrenOfType(CHANGED);
	}

	/**
	 * @see ICElementDelta
	 */
	protected ICElementDelta[] getChildrenOfType(int type) {
		int length = fAffectedChildren.length;
		if (length == 0) {
			return new ICElementDelta[] {};
		}
		ArrayList children= new ArrayList(length);
		for (int i = 0; i < length; i++) {
			if (fAffectedChildren[i].getKind() == type) {
				children.add(fAffectedChildren[i]);
			}
		}

		ICElementDelta[] childrenOfType = new ICElementDelta[children.size()];
		children.toArray(childrenOfType);
		return childrenOfType;
	}

	/**
	 * Returns the delta for a given element.  Only looks below this
	 * delta.
	 */
	protected CElementDelta getDeltaFor(ICElement element) {
		if (equalsAndSameParent(getElement(), element)) // handle case of two jars that can be equals but not in the same project
			return this;
		if (fAffectedChildren.length == 0)
			return null;
		int childrenCount = fAffectedChildren.length;
		for (int i = 0; i < childrenCount; i++) {
			CElementDelta delta = (CElementDelta)fAffectedChildren[i];
			if (equalsAndSameParent(delta.getElement(), element)) { // handle case of two jars that can be equals but not in the same project
				return delta;
			}
			delta = delta.getDeltaFor(element);
			if (delta != null)
				return delta;
		}
		return null;
	}

	/**
	 * @see ICElementDelta
	 */
	public ICElement getElement() {
		return fChangedElement;
	}

	/**
	 * @see ICElementDelta
	 */
	public int getFlags() {
		return fChangeFlags;
	}

	/**
	 * @see ICElementDelta
	*/
	public int getKind() {
		return fKind;
	}

	/**
	 * @see ICElementDelta
	 */
	public ICElement getMovedFromElement() {
		return fMovedFromHandle;
	}

	/**
	 * @see ICElementDelta
	 */
	public ICElement getMovedToElement() {
		return fMovedToHandle;
	}

	/**
	 * @see ICElementDelta
	 */
	public ICElementDelta[] getRemovedChildren() {
		return getChildrenOfType(REMOVED);
	}

	/**
	 * Return the collection of resource deltas. Return null if none.
	 */
	public IResourceDelta[] getResourceDeltas() {
		if (resourceDeltas == null)
			return null;
		if (resourceDeltas.length != resourceDeltasCounter) {
			System.arraycopy(resourceDeltas, 0, resourceDeltas = new IResourceDelta[resourceDeltasCounter], 0, resourceDeltasCounter);
		}
		return resourceDeltas;
	}

	/**
	 * Adds the new element to a new array that contains all of the elements of the old array.
	 * Returns the new array.
	 */
	protected ICElementDelta[] growAndAddToArray(ICElementDelta[] array, ICElementDelta addition) {
		ICElementDelta[] old = array;
		array = new ICElementDelta[old.length + 1];
		System.arraycopy(old, 0, array, 0, old.length);
		array[old.length] = addition;
		return array;
	}

	/**
	 * Creates the delta tree for the given element and delta, and then
	 * inserts the tree as an affected child of this node.
	 */
	protected void insertDeltaTree(ICElement element, CElementDelta delta) {
		CElementDelta childDelta= createDeltaTree(element, delta);
		if (!equalsAndSameParent(element, getElement())) {
			addAffectedChild(childDelta);
		}
	}


	/**
	 * Creates the nested deltas resulting from an move operation.
	 * Convenience method for creating the "move from" delta.
	 * The constructor should be used to create the root delta 
	 * and then the move operation should call this method.
	 */
	public void movedFrom(ICElement movedFromElement, ICElement movedToElement) {
		CElementDelta removedDelta = new CElementDelta(movedFromElement);
		removedDelta.fKind = REMOVED;
		removedDelta.fChangeFlags |= F_MOVED_TO;
		removedDelta.fMovedToHandle = movedToElement;
		insertDeltaTree(movedFromElement, removedDelta);
	}

	/**
	 * Creates the nested deltas resulting from an move operation.
	 * Convenience method for creating the "move to" delta.
	 * The constructor should be used to create the root delta 
	 * and then the move operation should call this method.
	 */
	public void movedTo(ICElement movedToElement, ICElement movedFromElement) {
		CElementDelta addedDelta = new CElementDelta(movedToElement);
		addedDelta.fKind = ADDED;
		addedDelta.fChangeFlags |= F_MOVED_FROM;
		addedDelta.fMovedFromHandle = movedFromElement;
		insertDeltaTree(movedToElement, addedDelta);
	}

	/**
	 * Creates the nested deltas for an opened element.
	 */
	public void opened(ICElement element) {
		CElementDelta delta = new CElementDelta(element);
		delta.fKind = CHANGED;
		delta.fChangeFlags |= F_OPENED;
		insertDeltaTree(element, delta);
	}

	/**
	 * Removes the child delta from the collection of affected children.
	 */
	protected void removeAffectedChild(CElementDelta child) {
		int index = -1;
		if (fAffectedChildren != null) {
			for (int i = 0; i < fAffectedChildren.length; i++) {
				if (equalsAndSameParent(fAffectedChildren[i].getElement(), child.getElement())) { // handle case of two jars that can be equals but not in the same project
					index = i;
					break;
				}
			}
		}
		if (index >= 0) {
			fAffectedChildren= removeAndShrinkArray(fAffectedChildren, index);
		}
	}

	/**
	 * Removes the element from the array.
	 * Returns the a new array which has shrunk.
	 */
	protected ICElementDelta[] removeAndShrinkArray(ICElementDelta[] old, int index) {
		ICElementDelta[] array = new ICElementDelta[old.length - 1];
		if (index > 0)
			System.arraycopy(old, 0, array, 0, index);
		int rest = old.length - index - 1;
		if (rest > 0)
			System.arraycopy(old, index + 1, array, index, rest);
		return array;
	}

	/**
	 * Creates the nested deltas resulting from an delete operation.
	 * Convenience method for creating removed deltas.
	 * The constructor should be used to create the root delta 
	 * and then the delete operation should call this method.
	 */
	public void removed(ICElement element) {
		CElementDelta removedDelta= new CElementDelta(element);
		insertDeltaTree(element, removedDelta);
		CElementDelta actualDelta = getDeltaFor(element);
		if (actualDelta != null) {
			actualDelta.fKind = REMOVED;
			actualDelta.fChangeFlags = 0;
			actualDelta.fAffectedChildren = fgEmptyDelta;
		}
	}

	/**
	 * Creates the nested deltas resulting from a change operation.
	 * Convenience method for creating change deltas.
	 * The constructor should be used to create the root delta 
	 * and then a change operation should call this method.
	 */
	public void binaryParserChanged(ICElement element) {
		CElementDelta attachedDelta = new CElementDelta(element);
		attachedDelta.fKind = CHANGED;
		attachedDelta.fChangeFlags |= F_BINARY_PARSER_CHANGED;
		insertDeltaTree(element, attachedDelta);
	}

	/**
	 * Creates the nested deltas resulting from a change operation.
	 * Convenience method for creating change deltas.
	 * The constructor should be used to create the root delta 
	 * and then a change operation should call this method.
	 */
	public void sourceAttached(ICElement element) {
		CElementDelta attachedDelta = new CElementDelta(element);
		attachedDelta.fKind = CHANGED;
		attachedDelta.fChangeFlags |= F_SOURCEATTACHED;
		insertDeltaTree(element, attachedDelta);
	}

	/**
	 * Creates the nested deltas resulting from a change operation.
	 * Convenience method for creating change deltas.
	 * The constructor should be used to create the root delta 
	 * and then a change operation should call this method.
	 */
	public void sourceDetached(ICElement element) {
		CElementDelta detachedDelta = new CElementDelta(element);
		detachedDelta.fKind = CHANGED;
		detachedDelta.fChangeFlags |= F_SOURCEDETACHED;
		insertDeltaTree(element, detachedDelta);
	}

	/** 
	 * Returns a string representation of this delta's
	 * structure suitable for debug purposes.
	 *
	 * @see toString
	 */
	public String toDebugString(int depth) {
		StringBuffer buffer = new StringBuffer();
		for (int i= 0; i < depth; i++) {
			buffer.append('\t');
		}
		buffer.append(((CElement)getElement()).toDebugString());
		buffer.append(" ["); //$NON-NLS-1$
		switch (getKind()) {
			case ICElementDelta.ADDED :
				buffer.append('+');
				break;
			case ICElementDelta.REMOVED :
				buffer.append('-');
				break;
			case ICElementDelta.CHANGED :
				buffer.append('*');
				break;
			default :
				buffer.append('?');
				break;
		}
		buffer.append("]: {"); //$NON-NLS-1$
		int changeFlags = getFlags();
		boolean prev = false;
		if ((changeFlags & ICElementDelta.F_CHILDREN) != 0) {
			if (prev)
				buffer.append(" | "); //$NON-NLS-1$
			buffer.append("CHILDREN"); //$NON-NLS-1$
			prev = true;
		}
		if ((changeFlags & ICElementDelta.F_CONTENT) != 0) {
			if (prev)
				buffer.append(" | "); //$NON-NLS-1$
			buffer.append("CONTENT"); //$NON-NLS-1$
			prev = true;
		}
		if ((changeFlags & ICElementDelta.F_MOVED_FROM) != 0) {
			if (prev)
				buffer.append(" | "); //$NON-NLS-1$
			//buffer.append("MOVED_FROM(" + ((CElement)getMovedFromElement()).toStringWithAncestors() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			prev = true;
		}
		if ((changeFlags & ICElementDelta.F_MOVED_TO) != 0) {
			if (prev)
				buffer.append(" | "); //$NON-NLS-1$
			//buffer.append("MOVED_TO(" + ((CElement)getMovedToElement()).toStringWithAncestors() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			prev = true;
		}
		if ((changeFlags & ICElementDelta.F_MODIFIERS) != 0) {
			if (prev)
				buffer.append(" | "); //$NON-NLS-1$
			buffer.append("MODIFIERS CHANGED"); //$NON-NLS-1$
			prev = true;
		}
		if ((changeFlags & ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE) != 0) {
			if (prev)
				buffer.append(" | "); //$NON-NLS-1$
			buffer.append("CHANGED TO PATHENTRY INCLUDE"); //$NON-NLS-1$
			prev = true;
		}
		if ((changeFlags & ICElementDelta.F_CHANGED_PATHENTRY_MACRO) != 0) {
			if (prev)
				buffer.append(" | "); //$NON-NLS-1$
			buffer.append("CHANGED TO PATHENTRY MACRO"); //$NON-NLS-1$
			prev = true;
		}
		if ((changeFlags & ICElementDelta.F_ADDED_PATHENTRY_LIBRARY) != 0) {
			if (prev)
				buffer.append(" | "); //$NON-NLS-1$
			buffer.append("ADDED TO PATHENTRY LIBRARY"); //$NON-NLS-1$
			prev = true;
		}
		if ((changeFlags & ICElementDelta.F_REMOVED_PATHENTRY_LIBRARY) != 0) {
			if (prev)
				buffer.append(" | "); //$NON-NLS-1$
			buffer.append("ADDED TO PATHENTRY LIBRARY"); //$NON-NLS-1$
			prev = true;
		}
		if ((changeFlags & ICElementDelta.F_PATHENTRY_REORDER) != 0) {
			if (prev)
				buffer.append(" | "); //$NON-NLS-1$
			buffer.append("PATHENTRY REORDER"); //$NON-NLS-1$
			prev = true;
		}
		if ((changeFlags & ICElementDelta.F_CONTENT_TYPE) != 0) {
			if (prev)
				buffer.append(" | "); //$NON-NLS-1$
			buffer.append("CONTENT_TYPE"); //$NON-NLS-1$
			prev = true;
		}

		//if ((changeFlags & ICElementDelta.F_SUPER_TYPES) != 0) {
		//	if (prev)
		//		buffer.append(" | "); //$NON-NLS-1$
		//	buffer.append("SUPER TYPES CHANGED"); //$NON-NLS-1$
		//	prev = true;
		//}
		if ((changeFlags & ICElementDelta.F_FINE_GRAINED) != 0) {
			if (prev)
				buffer.append(" | "); //$NON-NLS-1$
			buffer.append("FINE GRAINED"); //$NON-NLS-1$
			prev = true;
		}
		buffer.append("}"); //$NON-NLS-1$
		ICElementDelta[] children = getAffectedChildren();
		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
				buffer.append("\n"); //$NON-NLS-1$
				buffer.append(((CElementDelta) children[i]).toDebugString(depth + 1));
			}
		}

		for (int i = 0; i < resourceDeltasCounter; i++) {
			buffer.append("\n");//$NON-NLS-1$
			for (int j = 0; j < depth+1; j++) {
				buffer.append('\t');
			}
			IResourceDelta resourceDelta = resourceDeltas[i];
			buffer.append(resourceDelta.toString());
			buffer.append("["); //$NON-NLS-1$
			switch (resourceDelta.getKind()) {
				case IResourceDelta.ADDED :
					buffer.append('+');
					break;
				case IResourceDelta.REMOVED :
					buffer.append('-');
					break;
				case IResourceDelta.CHANGED :
					buffer.append('*');
					break;
				default :
					buffer.append('?');
					break;
			}
			buffer.append("]"); //$NON-NLS-1$
		}
		return buffer.toString();
	}

	/** 
	 * Returns a string representation of this delta's
	 * structure suitable for debug purposes.
	 */
	public String toString() {
		return toDebugString(0);
	}

}
