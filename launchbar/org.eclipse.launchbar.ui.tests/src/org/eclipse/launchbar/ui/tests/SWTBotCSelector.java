/*******************************************************************************
 * Copyright (c) 2017, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui.tests;

import org.eclipse.launchbar.ui.controls.internal.CSelector;
import org.eclipse.launchbar.ui.controls.internal.LaunchBarWidgetIds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.SWTBotWidget;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;

@SWTBotWidget(clasz = CSelector.class, preferredName = "cselector")
public class SWTBotCSelector extends AbstractSWTBotControl<CSelector> {

	public static SWTBotCSelector withId(String id) throws WidgetNotFoundException {
		return new SWTBotCSelector(new SWTBot().widget(WidgetMatcherFactory.withId(id)));
	}

	public SWTBotCSelector(CSelector w) throws WidgetNotFoundException {
		super(w);
	}

	public SWTBot bot() {
		return new SWTBot(widget);
	}

	public void click(int x, int y) {
		notify(SWT.MouseEnter);
		notify(SWT.MouseMove);
		notify(SWT.Activate);
		notify(SWT.FocusIn);
		notify(SWT.MouseDown, createMouseEvent(x, y, 1, SWT.NONE, 1));
		notify(SWT.MouseUp, createMouseEvent(x, y, 1, SWT.BUTTON1, 1));
	}

	@Override
	public SWTBotCSelector click() {
		Point size = syncExec((Result<Point>) () -> widget.getSize());
		click(size.x / 2, size.y / 2);
		return this;
	}

	public void clickEdit() {
		bot().buttonWithId(LaunchBarWidgetIds.EDIT).click(); // $NON-NLS-1$
	}

	public void select(String text) {
		click();
	}

}
