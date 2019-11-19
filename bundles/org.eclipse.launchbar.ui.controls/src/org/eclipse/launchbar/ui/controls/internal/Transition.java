/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

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
