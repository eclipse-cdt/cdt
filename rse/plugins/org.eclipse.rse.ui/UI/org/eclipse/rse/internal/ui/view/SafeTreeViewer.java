/********************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation.
 ********************************************************************************/
package org.eclipse.rse.internal.ui.view;

import org.eclipse.jface.viewers.IDelayedLabelDecorator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

/**
 * The SafeTreeViewer wraps an SWT TreeViewer in order to avoid
 * exceptions due to accessing already disposed items.
 *
 * The main reason for this wrapping is that delayed label providers
 * (i.e. instances of {@link IDelayedLabelDecorator}) can trigger tree
 * item updates called in the viewer after the tree item itself got
 * disposed already. This happens especially if items appear and
 * disappear very fast within the tree which then will lead to nasty
 * SWT Widget already disposed exceptions.
 *
 * Clients may subclass this class.
 *
 * @since RSE 2.0
 */
public class SafeTreeViewer extends TreeViewer {

	/**
	 * Constructor.
	 *
	 * @param parent The parent control.
	 */
	public SafeTreeViewer(Composite parent) {
		super(parent);
	}

	/**
	 * Constructor.
	 *
	 * @param tree The tree control.
	 */
	public SafeTreeViewer(Tree tree) {
		super(tree);
	}

	/**
	 * Constructor.
	 *
	 * @param parent The parent control.
	 * @param style The SWT style bits passed to the tree creation.
	 */
	public SafeTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeViewer#doUpdateItem(org.eclipse.swt.widgets.Item, java.lang.Object)
	 */
	protected void doUpdateItem(Item item, Object element) {
		if (item != null && item.isDisposed()) return;
		super.doUpdateItem(item, element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#doUpdateItem(org.eclipse.swt.widgets.Widget, java.lang.Object, boolean)
	 */
	protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
		if (widget != null && widget.isDisposed()) return;
		super.doUpdateItem(widget, element, fullMap);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeViewer#getItemCount(org.eclipse.swt.widgets.Control)
	 */
	protected int getItemCount(Control widget) {
		if (widget != null && widget.isDisposed()) return 0;
		return super.getItemCount(widget);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeViewer#getItemCount(org.eclipse.swt.widgets.Item)
	 */
	protected int getItemCount(Item item) {
		if (item != null && item.isDisposed()) return 0;
		return super.getItemCount(item);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeViewer#showItem(org.eclipse.swt.widgets.Item)
	 */
	protected void showItem(Item item) {
		if (item != null && item.isDisposed()) return;
		super.showItem(item);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeViewer#updatePlus(org.eclipse.swt.widgets.Item, java.lang.Object)
	 */
	protected void updatePlus(Item item, Object element) {
		if (item != null && item.isDisposed()) return;
		super.updatePlus(item, element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeViewer#createChildren(org.eclipse.swt.widgets.Widget)
	 */
	protected void createChildren(Widget widget) {
		if (widget != null && widget.isDisposed()) return;
		super.createChildren(widget);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeViewer#getChild(org.eclipse.swt.widgets.Widget, int)
	 */
	protected Item getChild(Widget widget, int index) {
		if (widget != null && widget.isDisposed()) return null;
		return super.getChild(widget, index);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeViewer#getChildren(org.eclipse.swt.widgets.Widget)
	 */
	protected Item[] getChildren(Widget widget) {
		if (widget != null && widget.isDisposed()) return new Item[0];
		return super.getChildren(widget);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeViewer#getExpanded(org.eclipse.swt.widgets.Item)
	 */
	protected boolean getExpanded(Item item) {
		if (item != null && item.isDisposed()) return false;
		return super.getExpanded(item);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeViewer#getItems(org.eclipse.swt.widgets.Item)
	 */
	protected Item[] getItems(Item item) {
		if (item != null && item.isDisposed()) return new Item[0];
		return super.getItems(item);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeViewer#getParentItem(org.eclipse.swt.widgets.Item)
	 */
	protected Item getParentItem(Item item) {
		if (item != null && item.isDisposed()) return null;
		return super.getParentItem(item);
	}
}
