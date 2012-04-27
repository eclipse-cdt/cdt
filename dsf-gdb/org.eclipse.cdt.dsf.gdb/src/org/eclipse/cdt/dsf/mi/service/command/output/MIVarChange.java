/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Anton Gorenkov - DSF-GDB should properly handle variable type change (based on RTTI) (Bug 376901)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;


/**
 * GDB/MI var-update.
 */

public class MIVarChange {
	String name;
	String value;
	boolean inScope;
	boolean changed;
	private String newType;
	private boolean isDynamic = false;
	private int newNumChildren = -1;
	private boolean hasMore = false;
	private MIVar[] newChildren;
	private MIDisplayHint displayHint = MIDisplayHint.NONE;
	
	public MIVarChange(String n) {
		name = n;
	}

	public String getVarName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public boolean isInScope() {
		return inScope;
	}

	public boolean isChanged() {
		return changed;
	}

	/**
	 * @since 4.1
	 */
	public String getNewType() {
		return newType;
	}

	/**
	 * @return Whether the associated variable's value and children are provided
	 *         by a pretty printer.
	 *         
	 * @since 4.0
	 */
	public boolean isDynamic() {
		return isDynamic;
	}
	
	/**
	 * @return Whether the number of children changed since the last update.
	 * 
	 * @since 4.0
	 */
	public boolean numChildrenChanged() {
		return (newNumChildren != -1);
	}
	
	/**
	 * Only call if {@link #numChildrenChanged()} returns true.
	 * 
	 * @return The new number of children the associated varobj now has already fetched.
	 * 
	 * @since 4.0
	 */
	public int getNewNumChildren() {
		assert(newNumChildren != -1);
		return newNumChildren;
	}
	
	/**
	 * @return Whether there more children available than {@link #getNewNumChildren()}.
	 * 
	 * @since 4.0
	 */
	public boolean hasMore() {
		return hasMore;
	}

	/**
	 * @return The children added within the current update range.
	 * 
	 * @since 4.0
	 */
	public MIVar[] getNewChildren() {
		return newChildren;
	}
	
	/**
	 * @return The new display hint
	 *         
	 * @since 4.0
	 */
	public MIDisplayHint getDisplayHint() {
		return displayHint;
	}
	
	public void setValue(String v) {
		value = v;
	}

	public void setInScope(boolean b) {
		inScope = b;
	}

	public void setChanged(boolean c) {
		changed = c;
	}
	
	/**
	 * @since 4.1
	 */
	public void setNewType(String newType) {
		this.newType = newType;
	}
	
	/**
	 * @since 4.0
	 */
	public void setDynamic(boolean isDynamic) {
		this.isDynamic = isDynamic;
	}
	
	/**
	 * @since 4.0
	 */
	public void setNewNumChildren(int newNumChildren) {
		this.newNumChildren = newNumChildren;
	}
	
	/**
	 * @since 4.0
	 */
	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}
	
	/**
	 * @since 4.0
	 */
	public void setNewChildren(MIVar[] newChildren) {
		this.newChildren = newChildren;
	}

	/**
	 * @param hint
	 * 
	 * @since 4.0
	 */
	public void setDisplayHint(MIDisplayHint hint) {
		displayHint = hint;
	}
}
