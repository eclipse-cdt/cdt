/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.ui.internal.controls;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class Transition implements Runnable {

	private final Control control;
	private final int tick;
	private int current;
	private int to;
	private int incr;
	
	public Transition(Control control, int current, int tick) {
		this.control = control;
		this.current = current;
		this.tick = tick;
	}

	@Override
	public void run() {
		current += incr;
		if (!control.isDisposed())
			control.redraw();
		if (!done())
			Display.getCurrent().timerExec(tick, this);
	}
	
	public void to(int to) {
		if (current == to)
			return;

		this.to = to;
		this.incr = current > to ? -1 : 1;
		Display.getCurrent().timerExec(tick, this);
	}

	public int getCurrent() {
		return current;
	}

	public boolean done() {
		return current == to;
	}

}
