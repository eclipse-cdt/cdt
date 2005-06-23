/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.dnd;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * DelegatingDropAdapter
 */
public class DelegatingDropAdapter implements DropTargetListener {
	private TransferDropTargetListener[] fListeners;
	TransferDropTargetListener fCurrentListener;
	private int fOriginalDropType;

	/**
	 * Creates a new delegating drop adapter.
	 * 
	 * @param listeners an array of potential listeners
	 */
	public DelegatingDropAdapter(TransferDropTargetListener[] listeners) {
		Assert.isNotNull(listeners);
		fListeners= listeners;
	}

	/**
	 * The cursor has entered the drop target boundaries. The current listener
	 * is updated, and <code>#dragEnter()</code> is forwarded to the current
	 * listener.
	 * 
	 * @param event the drop target event
	 * @see DropTargetListener#dragEnter(DropTargetEvent)
	 */
	public void dragEnter(DropTargetEvent event) {
		fOriginalDropType= event.detail;
		updateCurrentListener(event);
	}

	/**
	 * The cursor has left the drop target boundaries. The event is forwarded to
	 * the current listener.
	 * 
	 * @param event the drop target event
	 * @see DropTargetListener#dragLeave(DropTargetEvent)
	 */
	public void dragLeave(final DropTargetEvent event) {
		setCurrentListener(null, event);
	}

	/**
	 * The operation being performed has changed (usually due to the user
	 * changing a drag modifier key while dragging). Updates the current
	 * listener and forwards this event to that listener.
	 * 
	 * @param event the drop target event
	 * @see DropTargetListener#dragOperationChanged(DropTargetEvent)
	 */
	public void dragOperationChanged(final DropTargetEvent event) {
		fOriginalDropType= event.detail;
		TransferDropTargetListener oldListener= getCurrentListener();
		updateCurrentListener(event);
		final TransferDropTargetListener newListener= getCurrentListener();
		// only notify the current listener if it hasn't changed based on the
		// operation change. otherwise the new listener would get a dragEnter
		// followed by a dragOperationChanged with the exact same event.
		if (newListener != null && newListener == oldListener) {
			Platform.run(new SafeRunnable() {
				public void run() throws Exception {
					newListener.dragOperationChanged(event);
				}
			});
		}
	}

	/**
	 * The cursor is moving over the drop target. Updates the current listener
	 * and forwards this event to that listener. If no listener can handle the
	 * drag operation the <code>event.detail</code> field is set to
	 * <code>DND.DROP_NONE</code> to indicate an invalid drop.
	 * 
	 * @param event the drop target event
	 * @see DropTargetListener#dragOver(DropTargetEvent)
	 */
	public void dragOver(final DropTargetEvent event) {
		TransferDropTargetListener oldListener= getCurrentListener();
		updateCurrentListener(event);
		final TransferDropTargetListener newListener= getCurrentListener();

		// only notify the current listener if it hasn't changed based on the
		// drag over. otherwise the new listener would get a dragEnter
		// followed by a dragOver with the exact same event.
		if (newListener != null && newListener == oldListener) {
			Platform.run(new SafeRunnable() {
				public void run() throws Exception {
					newListener.dragOver(event);
				}
			});
		}
	}

	/**
	 * Forwards this event to the current listener, if there is one. Sets the
	 * current listener to <code>null</code> afterwards.
	 * 
	 * @param event the drop target event
	 * @see DropTargetListener#drop(DropTargetEvent)
	 */
	public void drop(final DropTargetEvent event) {
		updateCurrentListener(event);
		if (getCurrentListener() != null) {
			Platform.run(new SafeRunnable() {
				public void run() throws Exception {
					getCurrentListener().drop(event);
				}
			});
		}
		setCurrentListener(null, event);
	}

	/**
	 * Forwards this event to the current listener if there is one.
	 * 
	 * @param event the drop target event
	 * @see DropTargetListener#dropAccept(DropTargetEvent)
	 */
	public void dropAccept(final DropTargetEvent event) {
		if (getCurrentListener() != null) {
			Platform.run(new SafeRunnable() {
				public void run() throws Exception {
					getCurrentListener().dropAccept(event);
				}
			});
		}
	}

	/**
	 * Returns the listener which currently handles drop events.
	 * 
	 * @return the <code>TransferDropTargetListener</code> which currently
	 *         handles drop events.
	 */
	TransferDropTargetListener getCurrentListener() {
		return fCurrentListener;
	}

	/**
	 * Returns the transfer data type supported by the given listener. Returns
	 * <code>null</code> if the listener does not support any of the specified
	 * data types.
	 * 
	 * @param dataTypes available data types
	 * @param listener <code>TransferDropTargetListener</code> to use for
	 *        testing supported data types.
	 * @return the transfer data type supported by the given listener or
	 *         <code>null</code>.
	 */
	private TransferData getSupportedTransferType(TransferData[] dataTypes, TransferDropTargetListener listener) {
		for (int i= 0; i < dataTypes.length; i++) {
			if (listener.getTransfer().isSupportedType(dataTypes[i])) {
				return dataTypes[i];
			}
		}
		return null;
	}

	/**
	 * Returns the combined set of <code>Transfer</code> types of all
	 * <code>TransferDropTargetListeners</code>.
	 * 
	 * @return the combined set of <code>Transfer</code> types
	 */
	public Transfer[] getTransfers() {
		Transfer[] types= new Transfer[fListeners.length];
		for (int i= 0; i < fListeners.length; i++) {
			types[i]= fListeners[i].getTransfer();
		}
		return types;
	}

	/**
	 * Sets the current listener to <code>listener</code>. Sends the given
	 * <code>DropTargetEvent</code> if the current listener changes.
	 * 
	 * @return <code>true</code> if the new listener is different than the
	 *         previous <code>false</code> otherwise
	 */
	private boolean setCurrentListener(TransferDropTargetListener listener, final DropTargetEvent event) {
		if (fCurrentListener == listener)
			return false;
		if (fCurrentListener != null) {
			Platform.run(new SafeRunnable() {
				public void run() throws Exception {
					fCurrentListener.dragLeave(event);
				}
			});
		}
		fCurrentListener= listener;
		if (fCurrentListener != null) {
			Platform.run(new SafeRunnable() {
				public void run() throws Exception {
					fCurrentListener.dragEnter(event);
				}
			});
		}
		return true;
	}

	/**
	 * Updates the current listener to one that can handle the drop. There can
	 * be many listeners and each listener may be able to handle many
	 * <code>TransferData</code> types. The first listener found that can
	 * handle a drop of one of the given <code>TransferData</code> types will
	 * be selected. If no listener can handle the drag operation the
	 * <code>event.detail</code> field is set to <code>DND.DROP_NONE</code>
	 * to indicate an invalid drop.
	 * 
	 * @param event the drop target event
	 */
	private void updateCurrentListener(DropTargetEvent event) {
		int originalDetail= event.detail;
		// Revert the detail to the "original" drop type that the User
		// indicated. This is necessary because the previous listener 
		// may have changed the detail to something other than what the 
		// user indicated.
		event.detail= fOriginalDropType;

		for (int i= 0; i < fListeners.length; i++) {
			TransferDropTargetListener listener= fListeners[i];
			TransferData dataType= getSupportedTransferType(event.dataTypes, listener);
			if (dataType != null) {
				TransferData originalDataType= event.currentDataType;
				// set the data type supported by the drop listener
				event.currentDataType= dataType;
				if (listener.isEnabled(event)) {
					// if the listener stays the same, set its previously
					// determined
					// event detail
					if (!setCurrentListener(listener, event))
						event.detail= originalDetail;
					return;
				}
				event.currentDataType= originalDataType;
			}
		}
		setCurrentListener(null, event);
		event.detail= DND.DROP_NONE;
	}

}
