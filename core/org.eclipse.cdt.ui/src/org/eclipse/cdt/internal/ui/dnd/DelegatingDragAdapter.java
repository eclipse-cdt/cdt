/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dnd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * A delegating drag adapter negotiates between a set of <code>TransferDragSourceListener</code>s
 * On <code>dragStart</code> the adapter determines the listener to be used for any further
 * <code>drag*</code> callbacks.
 */
public class DelegatingDragAdapter implements DragSourceListener {
	private TransferDragSourceListener[] fPossibleListeners;
	private List<TransferDragSourceListener> fActiveListeners;
	private TransferDragSourceListener fFinishListener;
	
	public DelegatingDragAdapter(TransferDragSourceListener[] listeners) {
		setPossibleListeners(listeners);
	}
	
	protected void setPossibleListeners(TransferDragSourceListener[] listeners) {
		Assert.isNotNull(listeners);
		Assert.isTrue(fActiveListeners == null, "Can only set possible listeners before drag operation has started"); //$NON-NLS-1$
		fPossibleListeners= listeners;
	}
	
	/* non Java-doc
	 * @see DragSourceListener
	 */
	@Override
	public void dragStart(DragSourceEvent event) {
		fFinishListener= null;
		boolean saveDoit= event.doit;
		Object saveData= event.data;
		boolean doIt= false;
		List<Transfer> transfers= new ArrayList<Transfer>(fPossibleListeners.length);
		fActiveListeners= new ArrayList<TransferDragSourceListener>(fPossibleListeners.length);
		
		for (TransferDragSourceListener listener : fPossibleListeners) {
			event.doit= saveDoit;
			listener.dragStart(event);
			if (event.doit) {
				transfers.add(listener.getTransfer());
				fActiveListeners.add(listener);
			}
			doIt= doIt || event.doit;
		}
		if (doIt) {
			((DragSource)event.widget).setTransfer(transfers.toArray(new Transfer[transfers.size()]));
		}
		event.data= saveData;
		event.doit= doIt;
	}

	/* non Java-doc
	 * @see DragSourceListener
	 */
	@Override
	public void dragSetData(DragSourceEvent event) {
		fFinishListener= getListener(event.dataType);
		if (fFinishListener != null) {
			fFinishListener.dragSetData(event);
		}
	}
	
	/* non Java-doc
	 * @see DragSourceListener
	 */
	@Override
	public void dragFinished(DragSourceEvent event) {
		try{
			if (fFinishListener != null) {
				fFinishListener.dragFinished(event);
			} else {
				// If the user presses Escape then we get a dragFinished without
				// getting a dragSetData before.
				fFinishListener= getListener(event.dataType);
				if (fFinishListener != null) {
					fFinishListener.dragFinished(event);
				}
			}
		} finally{
			fFinishListener= null;
			fActiveListeners= null;
		}	
	}
	
	private TransferDragSourceListener getListener(TransferData type) {
		if (type == null)
			return null;
			
		for (TransferDragSourceListener listener : fActiveListeners) {
			if (listener.getTransfer().isSupportedType(type))
				return listener;
		}
		return null;
	}	
}
