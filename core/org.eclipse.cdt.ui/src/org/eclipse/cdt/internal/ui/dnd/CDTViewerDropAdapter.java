/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.ui.dnd;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A drag and drop adapter to be used together with structured viewers.
 * The adapater delegates the <code>dragEnter</code>, <code>dragOperationChanged
 * </code>, <code>dragOver</code> and <code>dropAccept</code> method to the
 * <code>validateDrop</code> method. Furthermore it adds location feedback.
 */
public class CDTViewerDropAdapter implements DropTargetListener {

	/**
	 * Constant describing the position of the mouse cursor relative 
	 * to the target object.  This means the mouse is not positioned
	 * over or near any valid target.
	 */
	public static final int LOCATION_NONE= DND.FEEDBACK_NONE;
	
	/**
	 * Constant describing the position of the mouse cursor relative 
	 * to the target object.  This means the mouse is positioned
	 * directly on the target.
	 */
	public static final int LOCATION_ON= DND.FEEDBACK_SELECT;
	
	/**
	 * Constant describing the position of the mouse cursor relative 
	 * to the target object.  This means the mouse is positioned
	 * slightly before the target.
	 */
	public static final int LOCATION_BEFORE= DND.FEEDBACK_INSERT_BEFORE;
	
	/**
	 * Constant describing the position of the mouse cursor relative 
	 * to the target object.  This means the mouse is positioned
	 * slightly after the target.
	 */
	public static final int LOCATION_AFTER= DND.FEEDBACK_INSERT_AFTER;
	
	/**
	 * The threshold used to determine if the mouse is before or after
	 * an item.
	 */
	private static final int LOCATION_EPSILON= 5; 
	
	/**
	 * Style to enable location feedback.
	 */
	public static final int INSERTION_FEEDBACK= 1 << 1; 

	private StructuredViewer fViewer;
	private int fFeedback;
	private boolean fShowInsertionFeedback;
	private int fRequestedOperation;
	private int fLastOperation;
	protected int fLocation;
	protected Object fTarget;

	public CDTViewerDropAdapter(StructuredViewer viewer, int feedback) {
		Assert.isNotNull(viewer);
		fViewer= viewer;
		fFeedback= feedback;
		fLastOperation= -1;
	}

	/**
	 * Controls whether the drop adapter shows insertion feedback or not.
	 * 
	 * @param showInsertionFeedback <code>true</code> if the drop adapter is supposed
	 *	to show insertion feedback. Otherwise <code>false</code>
	 */
	public void showInsertionFeedback(boolean showInsertionFeedback) {
		fShowInsertionFeedback= showInsertionFeedback;
	}
	
	/**
	 * Returns the viewer this adapter is working on.
	 */
	protected StructuredViewer getViewer() {
		return fViewer;
	} 
	
	//---- Hooks to override -----------------------------------------------------
	
	/**
	 * The actual drop has occurred. Calls <code>drop(Object target, DropTargetEvent event)
	 * </code>.
	 * @see DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
	 */	 
	public void drop(DropTargetEvent event) {
		drop(fTarget, event);
	}
	
	/**
	 * The actual drop has occurred.
	 * @param target the drop target in form of a domain element.
	 * @param event the drop traget event
	 */	 
	public void drop(Object target, DropTargetEvent event) {
	}
	
	/**
	 * Checks if the drop is valid. The method calls <code>validateDrop
	 * (Object target, DropTargetEvent event). Implementors can alter the 
	 * <code>currentDataType</code> field and the <code>detail</code> field 
	 * to give feedback about drop acceptence.
	 */
	public void validateDrop(DropTargetEvent event) {
		validateDrop(fTarget, event, fRequestedOperation);
	}
	
	/**
	 * Checks if the drop on the current target is valid. The method
	 * can alter the <code>currentDataType</code> field and the <code>
	 * detail</code> field to give feedback about drop acceptence.
	 * @param target the drop target in form of a domain element.
	 * @param event the drop traget event
	 * @param operation the operation requested by the user.
	 */
	public void validateDrop(Object target, DropTargetEvent event, int operation) {
	}
	
	public void dragEnter(DropTargetEvent event) {
		dragOperationChanged(event);
	}
	
	public void dragLeave(DropTargetEvent event) {
		fTarget= null;
		fLocation= LOCATION_NONE;
	}
	
	public void dragOperationChanged(DropTargetEvent event) {
		fRequestedOperation= event.detail;
		fTarget= computeTarget(event);
		fLocation= computeLocation(event);
		validateDrop(event);
		fLastOperation= event.detail;
		computeFeedback(event);
	}
	
	public void dragOver(DropTargetEvent event) {
		Object oldTarget= fTarget;
		fTarget= computeTarget(event);
		
		//set the location feedback
		int oldLocation= fLocation;
		fLocation= computeLocation(event);
		if (oldLocation != fLocation || oldTarget != fTarget || fLastOperation != event.detail) {
			validateDrop(event);
			fLastOperation= event.detail;
		} else {
			event.detail= fLastOperation;
		}
		computeFeedback(event);
	}
	
	public void dropAccept(DropTargetEvent event) {
		fTarget= computeTarget(event);
		validateDrop(event);
		fLastOperation= event.detail;
	}
	
	/**
	 * Returns the data held by <code>event.item</code>. Inside a viewer
	 * this corresponds to the items data model element.
	 */
	protected Object computeTarget(DropTargetEvent event) {
		return event.item == null ? null : event.item.getData();
	}
	
	/**
	 * Returns the position of the given coordinates relative to the given target.
	 * The position is determined to be before, after, or on the item, based on 
	 * some threshold value. The return value is one of the LOCATION_* constants 
	 * defined in this class.
	 */
	final protected int computeLocation(DropTargetEvent event) {
		if (!(event.item instanceof Item))
			return LOCATION_NONE;
		
		Item item= (Item) event.item;
		Point coordinates= fViewer.getControl().toControl(new Point(event.x, event.y));
		Rectangle bounds= getBounds(item);
		if (bounds == null) {
			return LOCATION_NONE;
		}
		if ((coordinates.y - bounds.y) < LOCATION_EPSILON) {
			return LOCATION_BEFORE;
		}
		if ((bounds.y + bounds.height - coordinates.y) < LOCATION_EPSILON) {
			return LOCATION_AFTER;
		}
		return LOCATION_ON;
	}

	/**
	 * Returns the bounds of the given item, or <code>null</code> if it is not a 
	 * valid type of item.
	 */
	private Rectangle getBounds(Item item) {
		if (item instanceof TreeItem)
			return ((TreeItem) item).getBounds();
			
		if (item instanceof TableItem)
			return ((TableItem) item).getBounds(0);
			
		return null;
	}

	/**
	 * Sets the drag under feedback corresponding to the value of <code>fLocation</code>
	 * and the <code>INSERTION_FEEDBACK</code> style bit.
	 */
	protected void computeFeedback(DropTargetEvent event) {
		if (!fShowInsertionFeedback && fLocation != LOCATION_NONE) {
			event.feedback= DND.FEEDBACK_SELECT;
		} else {
			event.feedback= fLocation;
		}
		event.feedback|= fFeedback;
	}
	
	/**
	 * Sets the drop operation to </code>DROP_NODE<code>.
	 */
	protected void clearDropOperation(DropTargetEvent event) {
		event.detail= DND.DROP_NONE;
	}
	
	/**
	 * Returns the requested drop operation.
	 */
	protected int getRequestedOperation() {
		return fRequestedOperation;
	} 
	
	protected void setDefaultFeedback(int feedback) {
		fFeedback= feedback;
	}
	
	//---- helper methods to test DnD 
	
	public void internalTestSetLocation(int location) {
		fLocation= location;
	}

}
