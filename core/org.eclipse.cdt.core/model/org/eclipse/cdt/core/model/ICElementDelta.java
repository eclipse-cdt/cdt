package org.eclipse.cdt.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IResourceDelta;

/**
 * A C element delta describes changes in C element between two discrete
 * points in time.  Given a delta, clients can access the element that has 
 * changed, and any children that have changed.
 * <p>
 * Deltas have a different status depending on the kind of change they represent.  
 * The list below summarizes each status (as returned by <code>getKind</code>)
 * and its meaning:
 * <ul>
 *  <li><code>ADDED</code> - The element described by the delta 
 *  has been added.
 *  <li><code>REMOVED</code> - The element described by the delta 
 *  has been removed.
 *  <li><code>CHANGED</code> - The element described by the delta 
 *  has been changed in some way.
 *  </li>
 * </ul>
 * <p>
 * Move operations are indicated by other change flags, layered on top
 * of the change flags described above. If element A is moved to become B,
 * the delta for the  change in A will have status <code>REMOVED</code>,
 * with change flag <code>F_MOVED_TO</code>. In this case,
 * <code>getMovedToElement</code> on delta A will return the handle for B.
 * The  delta for B will have status <code>ADDED</code>, with change flag
 * <code>F_MOVED_FROM</code>, and <code>getMovedFromElement</code> on delta
 * B will return the handle for A. (Note, the handle to A in this case represents
 * an element that no longer exists).
 * </p>
 * <p>
 * Note that the move change flags only describe the changes to a single element, they
 * do not imply anything about the parent or children of the element.
 */
public interface ICElementDelta {

	/**
	 * Status constant indicating that the element has been added.
	 */
	public int ADDED = 1;

	/**
	 * Status constant indicating that the element has been removed.
	 */
	public int REMOVED = 2;

	/**
	 * Status constant indicating that the element has been changed,
	 * as described by the change flags.
	 */
	public int CHANGED = 4;

	/**
	 * Change flag indicating that the content of the element has changed.
	 */
	public int F_CONTENT = 0x0001;

	/**
	 * Change flag indicating that the modifiers of the element have changed.
	 */
	public int F_MODIFIERS = 0x0002;

	/**
	 * Change flag indicating that there are changes to the children of the element.
	 */
	public int F_CHILDREN = 0x0008;

	/**
	 * Change flag indicating that the element was moved from another location.
	 * The location of the old element can be retrieved using <code>getMovedFromElement</code>.
	 */
	public int F_MOVED_FROM = 0x0010;

	/**
	 * Change flag indicating that the element was moved to another location.
	 * The location of the new element can be retrieved using <code>getMovedToElement</code>.
	 */
	public int F_MOVED_TO = 0x0020;

	/**
	 * Change flag indicating that the element has changed position relatively to its siblings. 
	 * If the element is an <code>IPackageFragmentRoot</code>,  a classpath entry corresponding 
	 * to the element has changed position in the project's classpath.
	 * 
	 * @since 2.1
	 */
	public int F_REORDER = 0x00100;

	/**
	 * Change flag indicating that the underlying <code>IProject</code> has been
	 * opened.
	 */
	public int F_OPENED = 0x0040;

	/**
	 * Change flag indicating that the underlying <code>IProject</code> has been
	 * closed.
	 */
	public int F_CLOSED = 0x0080;

	/**
	 * A source entry added for this resource.
	 */
	public int F_ADDED_PATHENTRY_SOURCE =   0x0100;

	/**
	 * A source entry was remove for this resource.
	 */
	public int F_REMOVED_PATHENTRY_SOURCE =   0x0200;

	/**
	 * A pathEntry Macro was added for this resource
	 */
	public int F_CHANGED_PATHENTRY_MACRO =    0x0400;

	/**
	 * A pathEntry Include was added for this resource
	 */
	public int F_CHANGED_PATHENTRY_INCLUDE =  0x0800;

	/**
	 * A pathEntry Library was added for this resource
	 */
	public int F_ADDED_PATHENTRY_LIBRARY =  0x01000;

	/**
	 * A pathEntry Library was added for this resource
	 */
	public int F_REMOVED_PATHENTRY_LIBRARY =  0x02000;
	/**
	 * A pathEntry Project was added to the project.
	 */
	public int F_CHANGED_PATHENTRY_PROJECT =  0x04000;

	/**
	 * Reordering of the path entries.
	 */
	public int F_PATHENTRY_REORDER = 0x040000;

	//public int F_SUPER_TYPES = 0x080000;

	/**
	 * Change flag indicating that a source jar has been attached to a binary jar.
	 */
	public int F_SOURCEATTACHED = 0x100000;   

	/**
	 * Change flag indicating that a source jar has been detached to a binary jar.
	 */
	public int F_SOURCEDETACHED = 0x200000;

	/**
	 * Change flag indicating that this is a fine-grained delta, i.e. an analysis down
	 * to the members level was done to determine if there were structural changes to
	 * members.
	 */
	public int F_FINE_GRAINED = 0x400000;

	/**
	 * Change in the binary Parser.
	 */
	public int F_BINARY_PARSER_CHANGED = 0x800000;

	/**
	 * Change in the binary Parser.
	 */
	public int F_CONTENT_TYPE = 0x1000000;

	/**
	 * Returns deltas for the children that have been added.
	 */
	public ICElementDelta[] getAddedChildren();

	/**
	 * Returns deltas for the affected (added, removed, or changed) children.
	 */
	public ICElementDelta[] getAffectedChildren();

	/**
	 * Returns deltas for the children which have changed.
	 */
	public ICElementDelta[] getChangedChildren();

	/**
	 * Returns the element that this delta describes a change to.
	 */
	public ICElement getElement();

	/**
	 * Returns flags that describe how an element has changed.
	 *
	 * @see ICElementDelta#F_CHILDREN
	 * @see ICElementDelta#F_CONTENT
	 * @see ICElementDelta#F_MODIFIERS
	 * @see ICElementDelta#F_MOVED_FROM
	 * @see ICElementDelta#F_MOVED_TO
	 */
	public int getFlags();

	/**
	 * Returns the kind of this delta - one of <code>ADDED</code>, <code>REMOVED</code>,
	 * or <code>CHANGED</code>.
	 */
	public int getKind();

	/**
	 * Returns an element describing this element before it was moved
	 * to its current location, or <code>null</code> if the
	 * <code>F_MOVED_FROM</code> change flag is not set. 
	 */
	public ICElement getMovedFromElement();

	/**
	 * Returns an element describing this element in its new location,
	 * or <code>null</code> if the <code>F_MOVED_TO</code> change
	 * flag is not set.
	 */
	public ICElement getMovedToElement();

	/**
	 * Returns deltas for the children which have been removed.
	 */
	public ICElementDelta[] getRemovedChildren();

	/**
	 * Returns the collection of resource deltas.
	 * <p>
	 * Note that resource deltas, like C element deltas, are generally only valid
	 * for the dynamic scope of an event notification. Clients must not hang on to
	 * these objects.
	 * </p>
	 *
	 * @return the underlying resource deltas, or <code>null</code> if none
	 */
	public IResourceDelta[] getResourceDeltas();
}
