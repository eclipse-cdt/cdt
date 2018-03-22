/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.cdt.internal.meson.ui.tests.utils;

import static org.hamcrest.Matchers.notNullValue;

import org.assertj.core.api.Assertions;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * 
 */
public class SWTBotTreeItemAssertions extends AbstractSWTBotAssertions<SWTBotTreeItemAssertions, SWTBotTreeItem> {

	protected SWTBotTreeItemAssertions(final SWTBotTreeItem actual) {
		super(actual, SWTBotTreeItemAssertions.class);
	}

	public static SWTBotTreeItemAssertions assertThat(final SWTBotTreeItem containerPortsTreeItem) {
		return new SWTBotTreeItemAssertions(containerPortsTreeItem);
	}

	public SWTBotTreeItemAssertions isExpanded() {
		notNullValue();
		if(!actual.isExpanded()) {
			failWithMessage("Expected tree item %s to be expanded but it was not.", actual.getText());
		}
		return this;
	}

	/**
	 * Checks the number of items and also verifies that each item has an images and a text
	 * @param expectedCount
	 * @return
	 */
	public SWTBotTreeItemAssertions hasChildItems(final int expectedCount) {
		notNullValue();
		if(actual.getItems().length != expectedCount) {
			failWithMessage("Expected tree item %s to be have %s items but it had %s.", actual.getText(), expectedCount, actual.getItems().length);
		}
		for (SWTBotTreeItem swtBotTreeItem : actual.getItems()) {
			final String treeItemText = SWTUtils.syncExec(() -> swtBotTreeItem.getText());
			final Image treeItemWidgetImage = SWTUtils.syncExec(() -> swtBotTreeItem.widget.getImage());
			Assertions.assertThat(treeItemText).isNotNull();
			Assertions.assertThat(treeItemWidgetImage).isNotNull();
		}
		return this;
	}

	public SWTBotTreeItemAssertions hasText(final String expectedText) {
		notNullValue();
		if(!actual.getText().equals(expectedText)) {
			failWithMessage("Expected node to have text %s but it was %s", expectedText, actual.getText());
		}
		return this;
	}

}
