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
package org.eclipse.dd.dsf.ui.viewmodel;

import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.ModelDelta;

/**
 * This delta class mostly just duplicates the ModelDelta implemention, but
 * it allows clients to modify the flags after the original object is 
 * constructed. 
 *
 * @see IModelDelta#getNodes()
 */
@SuppressWarnings("restriction")
public class VMDelta extends ModelDelta {

	private VMDelta fParent;
	private IVMContext fVmcElement;
    private Object fElement;
	private int fFlags;
	private VMDelta[] fNodes = EMPTY_NODES;
	private Object fReplacement;
	private int fIndex;
	private static final VMDelta[] EMPTY_NODES = new VMDelta[0];

	/**
	 * Constructs a new delta for the given element.
	 * 
	 * @param vmcElement model element
	 * @param flags change flags
	 */
	public VMDelta(IVMContext vmcElement, int flags) {
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
	public VMDelta(IVMContext vmcElement, Object replacement, int flags) {
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
    public VMDelta(IVMContext vmcElement, int index, int flags) {
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
     * @param vmcElement Optional VMC element for this node, it can be used
     * by other nodes in the delta to set their VMC parent element correctly.
     */
    public VMDelta(Object element, IVMContext vmcElement) {
        super(element, IModelDelta.NO_CHANGE);
        fElement = element;
        fVmcElement = vmcElement;
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
    
    public IVMContext getVMC() { return fVmcElement; }
    
	/**
	 * Adds a child node to this delta with the given element and change flags,
	 * and returns the child delta.
	 * 
	 * @param element child element to add
	 * @param flags change flags for child
	 * @return newly created child delta
	 */
	public VMDelta addNode(IVMContext element, int flags) {
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
    public VMDelta addNode(IVMContext element, Object replacement, int flags) {
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
    public VMDelta addNode(IVMContext element, int index, int flags) {
        VMDelta node = new VMDelta(element, index, flags);
        node.setParent(this);
        addDelta(node);
        return node;
    }
    
    /**
     * Adds a node to the delta for a non-VMC element.  This is used to 
     * construct the root branch of the delta before it is handed off to 
     * ViewModelProvider.handleDataModelEvent() 
     * @param element Element in the asynchronous view to create the new node for.
     * @param vmcElement Optional VMC element for this node, it can be used
     * by other nodes in the delta to set their VMC parent element correctly.
     * @return Returns the added delta node.
     */
    public VMDelta addNode(Object element, IVMContext vmcElement) {
        VMDelta node = new VMDelta(element, vmcElement);
        node.setParent(this);
        addDelta(node);
        return node;
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
	public VMDelta[] getNodes() {
		return fNodes;
	}
	
	private void addDelta(VMDelta delta) {
		if (fNodes.length == 0) {
			fNodes = new VMDelta[]{delta};
		} else {
			VMDelta[] nodes = new VMDelta[fNodes.length + 1];
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
	
	private void appendDetail(StringBuffer buf, VMDelta delta) {
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
		VMDelta[] nodes = delta.getNodes();
		for (int i = 0; i < nodes.length; i++) {
			appendDetail(buf, nodes[i]);
		}
	}
}
