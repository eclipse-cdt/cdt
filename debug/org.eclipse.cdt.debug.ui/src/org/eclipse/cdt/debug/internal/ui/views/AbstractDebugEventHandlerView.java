/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views;

 
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.IStatusLineManager;

/**
 * A debug view that uses an event handler to update its
 * view/viewer.
 */
public abstract class AbstractDebugEventHandlerView extends AbstractDebugView {

	/**
	 * Event handler for this view
	 */
	private AbstractDebugEventHandler fEventHandler;

	/**
	 * Sets the event handler for this view
	 * 
	 * @param eventHandler event handler
	 */
	protected void setEventHandler(AbstractDebugEventHandler eventHandler) {
		this.fEventHandler = eventHandler;
	}
	
	/**
	 * Returns the event handler for this view
	 * 
	 * @return The event handler for this view
	 */
	protected AbstractDebugEventHandler getEventHandler() {
		return this.fEventHandler;
	}	
	
	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (getEventHandler() != null) {
			getEventHandler().dispose();
		}	
	}
	
	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesHidden()
	 */
	protected void becomesHidden() {
		super.becomesHidden();
		getEventHandler().viewBecomesHidden();
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesVisible()
	 */
	protected void becomesVisible() {
		super.becomesVisible();
		getEventHandler().viewBecomesVisible();
	}
	
	protected void clearStatusLine() {
		IStatusLineManager manager = getViewSite().getActionBars().getStatusLineManager(); 
		manager.setErrorMessage(null);
		manager.setMessage(null);
	}
}
