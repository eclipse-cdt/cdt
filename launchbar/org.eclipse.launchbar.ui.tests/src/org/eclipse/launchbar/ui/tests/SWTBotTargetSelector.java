/*******************************************************************************
 * Copyright (c) 2025 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui.tests;

import org.eclipse.launchbar.ui.controls.internal.LaunchBarWidgetIds;
import org.eclipse.launchbar.ui.controls.internal.TargetSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

public class SWTBotTargetSelector extends SWTBotCSelector {

	private class ActionArea extends AbstractSWTBotControl<Composite> {
		public ActionArea(SWTBotShell shell) throws WidgetNotFoundException {
			super(shell.bot().widget(WidgetMatcherFactory.withId(LaunchBarWidgetIds.TARGET_NEW)));
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
		public ActionArea click() {
			Point size = syncExec((Result<Point>) () -> widget.getSize());
			click(size.x / 2, size.y / 2);
			return this;
		}
	}

	public static class NewTargetDialog extends SWTBotShell {
		public NewTargetDialog(Shell shell) {
			super(shell);
		}

		public NewTargetDialog setType(String type) {
			bot().table().select(type);
			return this;
		}

		public NewTargetDialog next() {
			bot().button("Next >").click();
			return this;
		}

		public NewTargetDialog finish() {
			bot().button("Finish").click();
			return this;
		}
	}

	public static class EditTargetDialog extends SWTBotShell {
		public EditTargetDialog(Shell shell) {
			super(shell);
		}

		public EditTargetDialog finish() {
			bot().button("Finish").click();
			return this;
		}
	}

	public SWTBotTargetSelector(TargetSelector targetSelector) {
		super(targetSelector);
	}

	public SWTBotTargetSelector(SWTBot bot) {
		this(bot.widget(WidgetMatcherFactory.withId(LaunchBarWidgetIds.TARGET_SELECTOR)));
	}

	public NewTargetDialog newTargetDialog() {
		click();
		new ActionArea(bot().shellWithId(LaunchBarWidgetIds.TARGET_POPUP)).click();
		return new NewTargetDialog(bot().shell("New Launch Target").widget);
	}

	@Override
	public void clickEdit() {
		bot().canvasWithId(LaunchBarWidgetIds.TARGET_EDIT).click();
	}

	public EditTargetDialog editSerialTargetDialog() {
		clickEdit();
		return new EditTargetDialog(bot().shell("Edit Serial Launch Target").widget);
	}

	public EditTargetDialog editTCPTargetDialog() {
		clickEdit();
		return new EditTargetDialog(bot().shell("Edit TCP Launch Target").widget);
	}
}
