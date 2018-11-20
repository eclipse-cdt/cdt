/*******************************************************************************
 * Copyright (c) 2011, 2014 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.osview;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.IResourceClass;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @since 2.4
 */
public class ResourceClassContributionItem extends ContributionItem {

	// In some places below, we are trying to determine size hint for Combo, given the list of
	// content. However, while we can determine width of content, we don't know how much width
	// the combobox itself is adding. This constant is our guess.
	private static final int COMBO_TRIM_WIDTH = 64;

	interface Listener {
		void resourceClassChanged(String newClass);
	}

	private Combo fResourceClassCombo;
	private IResourceClass[] resourceClasses = new IResourceClass[0];
	private String fResourceClassId = null;

	private Listener fListener;
	private boolean blockListener = false;
	private ToolItem toolItem;

	private boolean enabled = true;

	private static IDialogSettings settings;

	private static IDialogSettings getDialogSettings() {
		if (settings != null)
			return settings;

		IDialogSettings topSettings = GdbUIPlugin.getDefault().getDialogSettings();
		settings = topSettings.getSection(ResourceClassContributionItem.class.getName());
		if (settings == null) {
			settings = topSettings.addNewSection(ResourceClassContributionItem.class.getName());
		}
		return settings;
	}

	public void setListener(Listener listener) {

		fListener = listener;
	}

	public void setEnabled(boolean enable) {
		// It appears that every update of action bars will call 'fill' action below, creating
		// combo. So, we want to keep 'enabled' state as member variable, to make sure it is kept
		// if combo is recreated.
		enabled = enable;
		if (fResourceClassCombo != null)
			fResourceClassCombo.setEnabled(enable);
	}

	public String updateClasses(IResourceClass[] resourceClasses) {

		boolean different = false;
		if (this.resourceClasses.length != resourceClasses.length)
			different = true;
		else
			for (int i = 0; i < this.resourceClasses.length; ++i) {
				if (!this.resourceClasses[i].getId().equals(resourceClasses[i].getId()) || !this.resourceClasses[i]
						.getHumanDescription().equals(resourceClasses[i].getHumanDescription())) {
					different = true;
					break;
				}
			}

		if (!different)
			return fResourceClassId;

		this.resourceClasses = resourceClasses;

		fResourceClassCombo.removeAll();
		final int width = populateCombo();
		// Now change the width. Call to setWidth causes relayout automatically.
		// IMPORTANT: the visibility check is critical. Without it, we appear to have hit
		// an SWT/Gtk bug whenever a debug session and a previously use OS resources view
		// is not shown. The bug manifests by 100% CPU consumption inside event loop, and
		// it further blocks asyncExec runnables from ever executing. I suppose it might
		// be specific to relayout of invisible toolbar.

		// If we're invisible, we don't arrange for relayout to happen when the view becomes
		// available, because it is not exactly trivial (we need to events on the right control)
		// and it only matters when we start a new session and it has a different set of
		// resource classes and that requires longer combobox.
		if (toolItem.getParent().isVisible())
			toolItem.setWidth(width);

		return fResourceClassId;
	}

	/** Populate the combobox with resource classes. Return the width the
	 * combobox must have, including any trim. If there are no resource classes,
	 * returns some reasonable default width.
	 */
	private int populateCombo() {

		int width = 0;
		String lastResourceClassId = getDialogSettings().get("resourceClass"); //$NON-NLS-1$
		int index = -1;
		int i = 0;
		GC gc = new GC(fResourceClassCombo);
		for (i = 0; i < resourceClasses.length; ++i) {
			String description = resourceClasses[i].getHumanDescription();
			width = Math.max(width, gc.textExtent(description).x);
			fResourceClassCombo.add(description);
			if (resourceClasses[i].getId().equals(lastResourceClassId))
				index = i;
		}

		if (index != -1) {
			fResourceClassId = lastResourceClassId;
			blockListener = true;
			fResourceClassCombo.select(index);
			blockListener = false;
		}

		if (width == 0) {
			// We have some hints what the longest element in combo will be. Even if it's different
			// in new GDB version, no problem -- the combo will be resized when it's populated.
			width = gc.textExtent("Shared memory regions").x; //$NON-NLS-1$
		}

		// Because there's no way whatsoever to set the width
		// of the combobox list, only complete length, we just add
		// random padding.
		width = width + COMBO_TRIM_WIDTH;

		return width;
	}

	public String getResourceClassId() {
		return fResourceClassId;
	}

	@Override
	public void fill(ToolBar parent, int toolbarItemIndex) {

		fResourceClassCombo = new Combo(parent, SWT.NONE);
		fResourceClassCombo.setEnabled(enabled);
		int width = populateCombo();

		fResourceClassCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String description = fResourceClassCombo.getText();
				String id = null;
				for (int i = 0; i < resourceClasses.length; ++i)
					if (resourceClasses[i].getHumanDescription().equals(description)) {
						id = resourceClasses[i].getId();
						break;
					}

				// id is never null here, unless we messed up our data structures.
				assert id != null;
				if (id != null && !id.equals(fResourceClassId)) {
					fResourceClassId = id;
					getDialogSettings().put("resourceClass", id); //$NON-NLS-1$
					if (fListener != null && !blockListener)
						fListener.resourceClassChanged(fResourceClassId);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		toolItem = new ToolItem(parent, SWT.SEPARATOR);
		toolItem.setControl(fResourceClassCombo);
		toolItem.setWidth(width);
	}

	@Override
	public boolean isDynamic() {
		return false;
	}
}
