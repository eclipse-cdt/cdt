/*******************************************************************************
 *  Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - adapted to use in DSF
 *     Patrick Chuong (Texas Instruments)
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;

/**
 * This delta class mostly just duplicates the ModelDelta implementation, but
 * it allows clients to modify the flags after the original object is
 * constructed.
 *
 * @see IModelDelta#getNodes()
 *
 * @since 1.0
 */
public class VMDelta extends ModelDelta {

	private VMDelta fParent;
	private Object fElement;
	private int fFlags;
	private VMDelta[] fNodes = EMPTY_NODES;
	private Object fReplacement;
	private int fIndex;
	private static final VMDelta[] EMPTY_NODES = new VMDelta[0];
	private int fChildCount = -1;

	/**
	 * Constructs a new delta for the given element.
	 *
	 * @param vmcElement model element
	 * @param flags change flags
	 */
	public VMDelta(Object element, int flags) {
		super(element, flags);
		fElement = element;
		fFlags = flags;
	}

	/**
	 * Constructs a new delta for the given element to be replaced
	 * with the specified replacement element.
	 *
	 * @param vmcElement model element
	 * @param replacement replacement element
	 * @param flags change flags
	 */
	public VMDelta(Object element, Object replacement, int flags) {
		super(element, replacement, flags);
		fElement = element;
		fReplacement = replacement;
		fFlags = flags;
	}

	/**
	 * Constructs a new delta for the given element to be inserted at
	 * the specified index.
	 *
	 * @param vmcElement model element
	 * @param index insertion position
	 * @param flags change flags
	 */
	public VMDelta(Object element, int index, int flags) {
		super(element, index, flags);
		fElement = element;
		fIndex = index;
		fFlags = flags;
	}

	/**
	 * Constructs a new delta for the given element at the specified index
	 * relative to its parent with the given number of children.
	 *
	 * @param element model element
	 * @param index insertion position
	 * @param flags change flags
	 * @param childCount number of children this node has
	 */
	public VMDelta(Object element, int index, int flags, int childCount) {
		super(element, index, flags, childCount);
		fElement = element;
		fIndex = index;
		fFlags = flags;
		fChildCount = childCount;
	}

	/**
	 * Returns the non-VMC element if one is set, otherwise returns the VMC
	 * element of this delta node.
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getElement()
	 */
	@Override
	public Object getElement() {
		return fElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getFlags()
	 */
	@Override
	public int getFlags() {
		return fFlags;
	}

	/**
	 * Sets this delta's flags.
	 *
	 * @param flags
	 */
	@Override
	public void setFlags(int flags) {
		fFlags = flags;
	}

	@Override
	public void setChildCount(int count) {
		fChildCount = count;
	}

	/**
	 * Adds a child node to this delta with the given element and change flags,
	 * and returns the child delta.
	 *
	 * @param element child element to add
	 * @param flags change flags for child
	 * @return newly created child delta
	 */
	@Override
	public VMDelta addNode(Object element, int flags) {
		VMDelta node = new VMDelta(element, flags);
		node.setParent(this);
		addDelta(node);
		return node;
	}

	/**
	 * Adds a child node to this delta to replace the given element with the
	 * specified replacement element and change flags, and returns the
	 * newly created child delta.
	 *
	 * @param element child element to add to this delta
	 * @param replacement replacement element for the child element
	 * @param flags change flags
	 * @return newly created child delta
	 */
	@Override
	public VMDelta addNode(Object element, Object replacement, int flags) {
		VMDelta node = new VMDelta(element, replacement, flags);
		node.setParent(this);
		addDelta(node);
		return node;
	}

	/**
	 * Adds a child delta to this delta to insert the specified element at
	 * the given index, and returns the newly created child delta.
	 *
	 * @param element child element in insert
	 * @param index index of insertion
	 * @param flags change flags
	 * @return newly created child delta
	 */
	@Override
	public VMDelta addNode(Object element, int index, int flags) {
		VMDelta node = new VMDelta(element, index, flags);
		node.setParent(this);
		addDelta(node);
		return node;
	}

	/**
	 * Adds a child delta to this delta at the specified index with the
	 * given number of children, and returns the newly created child delta.
	 *
	 * @param element child element in insert
	 * @param index index of the element relative to parent
	 * @param flags change flags
	 * @param numChildren the number of children the element has
	 * @return newly created child delta
	 */
	@Override
	public VMDelta addNode(Object element, int index, int flags, int numChildren) {
		VMDelta node = new VMDelta(element, index, flags, numChildren);
		node.setParent(this);
		addDelta(node);
		return node;
	}

	/**
	 * Returns the child delta for the given element, or <code>null</code> if none.
	 *
	 * @param element child element
	 * @return corresponding delta node, or <code>null</code>
	 *
	 * @since 1.1
	 */
	@Override
	public VMDelta getChildDelta(Object element) {
		if (fNodes != null) {
			for (int i = 0; i < fNodes.length; i++) {
				VMDelta delta = fNodes[i];
				if (element.equals(delta.getElement())) {
					return delta;
				}
			}
		}
		return null;
	}

	/**
	 * Sets the parent delta of this delta
	 *
	 * @param node parent delta
	 */
	void setParent(VMDelta node) {
		fParent = node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getParent()
	 */
	@Override
	public VMDelta getParentDelta() {
		return fParent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getReplacementElement()
	 */
	@Override
	public Object getReplacementElement() {
		return fReplacement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getIndex()
	 */
	@Override
	public int getIndex() {
		return fIndex;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getNodes()
	 */
	@Override
	public VMDelta[] getChildDeltas() {
		return fNodes;
	}

	private void addDelta(VMDelta delta) {
		if (fNodes.length == 0) {
			fNodes = new VMDelta[] { delta };
		} else {
			VMDelta[] nodes = new VMDelta[fNodes.length + 1];
			System.arraycopy(fNodes, 0, nodes, 0, fNodes.length);
			nodes[fNodes.length] = delta;
			fNodes = nodes;
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Model Delta Start\n"); //$NON-NLS-1$
		appendDetail(buf, this, 0);
		buf.append("Model Delta End\n"); //$NON-NLS-1$
		return buf.toString();
	}

	private void appendDetail(StringBuilder buf, VMDelta delta, int depth) {
		String indent = ""; //$NON-NLS-1$
		for (int i = 0; i < depth; i++) {
			indent += '\t';
		}
		buf.append(indent).append("\tElement: "); //$NON-NLS-1$
		buf.append(delta.getElement());
		buf.append('\n');
		buf.append(indent).append("\t\tFlags: "); //$NON-NLS-1$
		int flags = delta.getFlags();
		if (flags == 0) {
			buf.append("NO_CHANGE"); //$NON-NLS-1$
		} else {
			if ((flags & IModelDelta.ADDED) > 0) {
				buf.append("ADDED | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.CONTENT) > 0) {
				buf.append("CONTENT | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.EXPAND) > 0) {
				buf.append("EXPAND | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.COLLAPSE) > 0) {
				buf.append("COLLAPSE | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.INSERTED) > 0) {
				buf.append("INSERTED | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.REMOVED) > 0) {
				buf.append("REMOVED | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.REPLACED) > 0) {
				buf.append("REPLACED | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.SELECT) > 0) {
				buf.append("SELECT | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.STATE) > 0) {
				buf.append("STATE | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.INSTALL) > 0) {
				buf.append("INSTALL | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.UNINSTALL) > 0) {
				buf.append("UNINSTALL | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.REVEAL) > 0) {
				buf.append("REVEAL | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.FORCE) > 0) {
				buf.append("FORCE | "); //$NON-NLS-1$
			}
		}
		buf.append('\n');
		buf.append(indent).append("\t\tIndex: "); //$NON-NLS-1$
		buf.append(delta.fIndex);
		buf.append(" Child Count: "); //$NON-NLS-1$
		buf.append(delta.fChildCount);
		buf.append('\n');
		IModelDelta[] nodes = delta.getChildDeltas();
		for (int i = 0; i < nodes.length; i++) {
			appendDetail(buf, (VMDelta) nodes[i], depth + 1);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta#getChildCount()
	 */
	@Override
	public int getChildCount() {
		return fChildCount;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta#accept(org.eclipse.debug.internal.ui.viewers.provisional.IModelDeltaVisitor)
	 */
	@Override
	public void accept(IModelDeltaVisitor visitor) {
		doAccept(visitor, 0);
	}

	@Override
	protected void doAccept(IModelDeltaVisitor visitor, int depth) {
		if (visitor.visit(this, depth)) {
			ModelDelta[] childDeltas = getChildDeltas();
			for (int i = 0; i < childDeltas.length; i++) {
				((VMDelta) childDeltas[i]).doAccept(visitor, depth + 1);
			}
		}
	}

	@Override
	public void setIndex(final int index) {
		fIndex = index;
	}
}
