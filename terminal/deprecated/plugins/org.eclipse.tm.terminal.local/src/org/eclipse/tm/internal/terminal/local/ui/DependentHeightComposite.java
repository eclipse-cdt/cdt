/***************************************************************************************************
 * Copyright (c) 2008 Mirko Raner.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - initial implementation for Eclipse Bug 196337
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local.ui;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * The class {@link DependentHeightComposite} is a special kind of SWT {@link Composite} whose
 * height depends on the height of some master {@link Composite} that does not have to be a direct
 * parent or child of it. This class was introduced as a work-around for UI resizing problems with
 * the Terminal API's PageBook class (which uses a StackLayout).
 *
 * @author Mirko Raner
 * @version $Revision: 1.1 $
 */
public class DependentHeightComposite extends Composite implements ControlListener {

	private Composite master;
	private int preferredHeight = -1;
	private int extraHeight;

	/**
	 * Creates a new {@link DependentHeightComposite}.
	 *
	 * @param parent the parent {@link Composite}
	 * @param style the SWT style
	 * @param master the master {@link Composite} that determines the height
	 * @param extra the additional height in pixels (may be negative, to create a smaller height
	 * than the master {@link Composite})
	 */
	public DependentHeightComposite(Composite parent, int style, Composite master, int extra) {

		super(parent, style);
		this.master = master;
		this.extraHeight = extra;
		master.addControlListener(this);
	}

	/**
	 * This method does nothing.
	 *
	 * @see ControlListener#controlMoved(ControlEvent)
	 */
	public void controlMoved(ControlEvent event) {

		// Does nothing...
	}

	/**
	 * Adjusts the {@link DependentHeightComposite} height if the master {@link Composite}'s size
	 * changed.
	 *
	 * @param event the {@link ControlEvent}
	 */
	public void controlResized(ControlEvent event) {

		setSize(getSize().x, master.getClientArea().height+extraHeight);
		preferredHeight = master.getClientArea().height+extraHeight;
		master.layout();
	}

	/**
	 * Computes the preferred size of this {@link DependentHeightComposite}.
	 *
	 * @see Composite#computeSize(int, int, boolean)
	 */
	public Point computeSize(int widthHint, int heightHint, boolean changed) {

		Point size = super.computeSize(widthHint, heightHint, changed);
		if (preferredHeight != -1) {

			size.y = preferredHeight;
		}
		return size;
	}
}
