/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.downloads;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.cdt.arduino.ui.internal.FormTextHoverManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class SelectPlatformsDialog extends Dialog {

	private Collection<ArduinoPlatform> platforms;
	private Collection<ArduinoPlatform> selectedPlatforms;
	private Table table;

	protected SelectPlatformsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		if (size.x < 500 || size.y < 300) {
			return new Point(500, 300);
		} else {
			return size;
		}
	}

	public void setPlatforms(Collection<ArduinoPlatform> platforms) {
		this.platforms = platforms;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));

		table = new Table(comp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumnLayout tableLayout = new TableColumnLayout();

		TableColumn packageColumn = new TableColumn(table, SWT.LEAD);
		packageColumn.setText("Package");
		tableLayout.setColumnData(packageColumn, new ColumnWeightData(2, 75, true));

		TableColumn platformColumn = new TableColumn(table, SWT.LEAD);
		platformColumn.setText("Platform");
		tableLayout.setColumnData(platformColumn, new ColumnWeightData(5, 150, true));

		TableColumn versionColumn = new TableColumn(table, SWT.LEAD);
		versionColumn.setText("Version");
		tableLayout.setColumnData(versionColumn, new ColumnWeightData(2, 75, true));

		comp.setLayout(tableLayout);

		for (ArduinoPlatform platform : ArduinoManager.getSortedPlatforms(platforms)) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData(platform);
			item.setText(0, platform.getPackage().getName());
			item.setText(1, platform.getName());
			item.setText(2, platform.getVersion());
		}

		FormTextHoverManager hoverManager = new FormTextHoverManager() {
			@Override
			protected void computeInformation() {
				TableItem item = table.getItem(getHoverEventLocation());
				if (item != null) {
					ArduinoPlatform platform = (ArduinoPlatform) item.getData();
					setInformation(platform.toFormText(), item.getBounds());
				} else {
					setInformation(null, null);
				}
			}
		};
		hoverManager.install(table);

		applyDialogFont(comp);
		return comp;
	}

	@Override
	protected void okPressed() {
		selectedPlatforms = new ArrayList<>();
		for (TableItem item : table.getItems()) {
			if (item.getChecked()) {
				selectedPlatforms.add((ArduinoPlatform) item.getData());
			}
		}

		super.okPressed();
	}

	public Collection<ArduinoPlatform> getSelectedPlatforms() {
		return selectedPlatforms;
	}

}
