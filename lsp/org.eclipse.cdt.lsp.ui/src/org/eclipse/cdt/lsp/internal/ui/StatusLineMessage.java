/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.ui;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

@SuppressWarnings("restriction")
public class StatusLineMessage implements Consumer<String> {

	private final int width = 28;
	private final String id = "org.eclipse.cdt.lsp.ui.status"; //$NON-NLS-1$

	@Override
	public void accept(String message) {
		Display.getDefault().asyncExec(() -> //
		Arrays.stream(PlatformUI.getWorkbench().getWorkbenchWindows())//
				.map(this::ensure)//
				.forEach(item -> item.setText(message)));
	}

	private StatusLineContributionItem ensure(IWorkbenchWindow window) {
		//FIXME: find via MToolControl with "org.eclipse.ui.StatusLine" identifier
		StatusLineManager line = ((WorkbenchWindow) window).getStatusLineManager();
		return Optional.ofNullable(line.find(id))//
				.filter(StatusLineContributionItem.class::isInstance)//
				.map(StatusLineContributionItem.class::cast)//
				.orElseGet(() -> create(line));
	}

	private StatusLineContributionItem create(StatusLineManager line) {
		StatusLineContributionItem item = new StatusLineContributionItem(id, width);
		line.add(item);
		return item;
	}
}
