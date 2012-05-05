/*******************************************************************************
 * Copyright (c) 2011 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.osview;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ResourceClassContributionItem extends ContributionItem {
	
	interface Listener
	{
		void resourceClassChanged(String newClass);
	}
		
	// Specifies mapping from user-friendly names of resource classes to
	// the names that are passed to the '-info-os' command.
	private static Map<String, String> resourceClasses = new LinkedHashMap<String, String>();

	static {
		resourceClasses.put(Messages.ResourceClassContributionItem_0, "processes"); //$NON-NLS-1$
		resourceClasses.put(Messages.ResourceClassContributionItem_2, "procgroups"); //$NON-NLS-1$
		resourceClasses.put(Messages.ResourceClassContributionItem_4, "threads"); //$NON-NLS-1$
		resourceClasses.put(Messages.ResourceClassContributionItem_6, "files"); //$NON-NLS-1$
		resourceClasses.put(Messages.ResourceClassContributionItem_8, "sockets"); //$NON-NLS-1$
		resourceClasses.put(Messages.ResourceClassContributionItem_10, "shm"); //$NON-NLS-1$
		resourceClasses.put(Messages.ResourceClassContributionItem_12, "semaphores"); //$NON-NLS-1$
		resourceClasses.put(Messages.ResourceClassContributionItem_14, "msg"); //$NON-NLS-1$
		resourceClasses.put(Messages.ResourceClassContributionItem_16, "modules"); //$NON-NLS-1$
	}
		
	private Combo fResourceClassCombo;	
	private String fResourceClass = null;
	private IPreferenceStore fPreferences;
	private Listener fListener;
	
	public ResourceClassContributionItem() {
		
		fPreferences = GdbUIPlugin.getDefault().getPreferenceStore();
				
	}
	
	public void setListener(Listener listener) {
		
		fListener = listener;
	}
	
	public void setEnabled(boolean enable) {
		fResourceClassCombo.setEnabled(enable);
	}
	
	public String getResourceClass() {
		return fResourceClass;
	}
			
	@Override
	public void fill(ToolBar parent, int toolbarItemIndex) {
		
		fResourceClassCombo = new Combo(parent, SWT.NONE);
		int width = 0;
		String lastResourceClass = fPreferences.getString("resourceClass"); //$NON-NLS-1$
		int index = -1;
		int i = 0;
		GC gc = new GC(fResourceClassCombo);
		for (String rc : resourceClasses.keySet()) {
			width = Math.max(width, gc.textExtent(rc).x);
			fResourceClassCombo.add(rc);
			if (rc.equals(lastResourceClass))
				index = i;
			++i;
		}
		if (index != -1) {
			fResourceClass = resourceClasses.get(lastResourceClass);
			fResourceClassCombo.select(index);
		}
				
		// Because there's no way whatsoever to set the width
		// of the combobox list, only complete length, we just add
		// random padding.
		width = width + 64;
		
		fResourceClassCombo.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				String rc = resourceClasses.get(fResourceClassCombo.getText());
				if (!rc.equals(fResourceClass))
				{
					fResourceClass = rc;
					fPreferences.setValue("resourceClass", fResourceClassCombo.getText()); //$NON-NLS-1$
					if (fListener != null)
						fListener.resourceClassChanged(fResourceClass);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

			
		ToolItem item = new ToolItem(parent, SWT.SEPARATOR);
		item.setControl(fResourceClassCombo);
		item.setWidth(width);
	}	
}
