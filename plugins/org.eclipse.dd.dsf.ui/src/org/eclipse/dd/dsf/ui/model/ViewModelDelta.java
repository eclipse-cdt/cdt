/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - adapted to use in DSF
 *******************************************************************************/
package org.eclipse.dd.dsf.ui.model;

import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.ModelDelta;

/**
 * This delta class mostly just duplicates the ModelDelta implemention, but
 * it allows clients to modify the flags after the original object is 
 * constructed. 
 * <p>
 * TODO: This class derives from ModelDelta as opposed to just implementing IModelDelta
 * because of a reference in IModelDelta to ModelDelta.  Need to file a bug on it.
 * @see IModelDelta#getNodes()
 */
@SuppressWarnings("restriction")
public class ViewModelDelta extends ModelDelta {

	private ViewModelDelta fParent;
	private IViewModelContext fVmcElement;
    private Object fElement;
	private int fFlags;
	private ViewModelDelta[] fNodes = EMPTY_NODES;
	private Object fReplacement;
	private int fIndex;
	private static final ViewModelDelta[] EMPTY_NODES = new ViewModelDelta[0];

	/**
	 * Constructs a new delta for the given element.
	 * 
	 * @param vmcElement model element
	 * @param flags change flags
	 */
	public ViewModelDelta(IViewModelContext vmcElement, int flags) {
        super(vmcElement, flags);
		fVmcElement = vmcElement;
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
	public ViewModelDelta(IViewModelContext vmcElement, Object replacement, int flags) {
        super(vmcElement, replacement, flags);
        fVmcElement = vmcElement;
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
    public ViewModelDelta(IViewModelContext vmcElement, int index, int flags) {
        super(vmcElement, index, flags);
        fVmcElement = vmcElement;
        fIndex = index;
        fFlags = flags;
    }
    
    /**
     * Constructor for model delta based on non-VMC element.  This delta is 
     * only needed for creating delta nodes for parent elements in the tree
     * if the VMC elements are not at the root of the tree.
     * @param element Element to create the delta for.
     */
    public ViewModelDelta(Object element) {
        super(element, IModelDelta.NO_CHANGE);
        fElement = element;
    }
        

    /** 
     * Returns the non-VMC element if one is set, otherwise returns the VMC
     * element of this delta node.
     * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getElement()
     */
    public Object getElement() {
		return fElement != null ? fElement : fVmcElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getFlags()
	 */
	public int getFlags() {
		return fFlags;
	}

    public void addFlags(int flags) {
        fFlags |= flags;
    }
    
    
    public IViewModelContext getVMC() { return fVmcElement; }
    
	/**
	 * Adds a child node to this delta with the given element and change flags,
	 * and returns the child delta.
	 * 
	 * @param element child element to add
	 * @param flags change flags for child
	 * @return newly created child delta
	 */
	public ViewModelDelta addNode(IViewModelContext element, int flags) {
		ViewModelDelta node = new ViewModelDelta(element, flags);
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
    public ViewModelDelta addNode(IViewModelContext element, Object replacement, int flags) {
        ViewModelDelta node = new ViewModelDelta(element, replacement, flags);
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
    public ViewModelDelta addNode(IViewModelContext element, int index, int flags) {
        ViewModelDelta node = new ViewModelDelta(element, index, flags);
        node.setParent(this);
        addDelta(node);
        return node;
    }
    
    /**
     * Sets the parent delta of this delta
     * 
     * @param node parent delta
     */
	void setParent(ViewModelDelta node) {
		fParent = node;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getParent()
	 */
	public IModelDelta getParent() {
		return fParent;
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getReplacementElement()
     */
    public Object getReplacementElement() {
        return fReplacement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getIndex()
     */
    public int getIndex() {
        return fIndex;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getNodes()
	 */
	public ViewModelDelta[] getNodes() {
		return fNodes;
	}
	
	private void addDelta(ViewModelDelta delta) {
		if (fNodes.length == 0) {
			fNodes = new ViewModelDelta[]{delta};
		} else {
			ViewModelDelta[] nodes = new ViewModelDelta[fNodes.length + 1];
			System.arraycopy(fNodes, 0, nodes, 0, fNodes.length);
			nodes[fNodes.length] = delta;
			fNodes = nodes;
		}
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Model Delta Start\n"); //$NON-NLS-1$
		appendDetail(buf, this);
		buf.append("Model Delta End\n"); //$NON-NLS-1$
		return buf.toString();
	}
	
	private void appendDetail(StringBuffer buf, ViewModelDelta delta) {
		buf.append("\tElement: "); //$NON-NLS-1$
		buf.append(delta.getElement());
		buf.append('\n');
		buf.append("\t\tFlags: "); //$NON-NLS-1$
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
		}
		buf.append('\n');
		ViewModelDelta[] nodes = delta.getNodes();
		for (int i = 0; i < nodes.length; i++) {
			appendDetail(buf, nodes[i]);
		}
	}
}
