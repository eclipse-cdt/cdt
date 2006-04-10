/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.widgets;

import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * Static methods that can be used when writing SWT GUI code.
 * They simply make it more productive.
 */
public class SystemFileWidgetHelpers extends SystemWidgetHelpers
{


	/**
	 * Creates a new remote system folder combobox instance and sets the default
	 * layout data, with tooltip text.
	 * <p>
	 * Assign the listener to the passed in implementer of Listener.
	 * <p>
	 * A remote system folder combobox is one that mimics the local folder selection combobox, but
	 * works with remote file systems instead. It has a label, a historical dropdown, and a browse button.
	 * <p>
	 * @param parent composite to put the combo into.
	 * @param listener object to listen for selection events. Can be null.          
	 *   If you want to listen for modify events, call addListener(SWT.Modify,this) on your own.
	 * @param historyKey The key with which to remember/restore the history for this combo. Pass null to use the overall default.
	 * @param horizontalSpan number of columns this should span
	 * @param readOnly true if the combo is to be readonly
	 */
	public static SystemRemoteFolderCombo createFolderCombo(Composite parent, SelectionListener listener, int horizontalSpan, String historyKey, boolean readOnly) {
		if (historyKey == null)
			historyKey = ISystemPreferencesConstants.HISTORY_FOLDER;
		SystemRemoteFolderCombo combo = new SystemRemoteFolderCombo(parent, SWT.NULL, historyKey, readOnly);
		if (listener != null)
			combo.addSelectionListener(listener);
		boolean hasGridData = (combo.getLayoutData() != null) && (combo.getLayoutData() instanceof GridData);
		//System.out.println("history directory griddata non-null? " + hasGridData);
		int minwidth = 250;
		if (!hasGridData) {
			GridData data = new GridData();
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			data.widthHint = minwidth;
			data.verticalAlignment = GridData.CENTER;
			data.grabExcessVerticalSpace = false;
			data.horizontalSpan = horizontalSpan;
			combo.setLayoutData(data);
		} else {
			((GridData) combo.getLayoutData()).horizontalSpan = horizontalSpan;
			((GridData) combo.getLayoutData()).horizontalAlignment = GridData.FILL;
			((GridData) combo.getLayoutData()).grabExcessHorizontalSpace = true;
			((GridData) combo.getLayoutData()).widthHint = minwidth;
		}
		return combo;
	}

	/**
	 * Creates a new remote system directory combobox instance and sets the default
	 * layout data, with tooltip text. Each remote directory is qualified by its connection name.
	 * These combos are always readonly.
	 * <p>
	 * Assign the listener to the passed in implementer of Listener.
	 * <p>
	 * A remote system qualified-directory combobox is one that mimics the local directory selection combobox, but
	 * works with remote file systems instead. It has a label, a historical dropdown, and a browse button.
	 * <p>
	 * @param parent composite to put the combo into.
	 * @param listener object to listen for selection events. Can be null.          
	 *   If you want to listen for modify events, call addListener(SWT.Modify,this) on your own.
	 * @param historyKey The key with which to remember/restore the history for this combo. Pass null to use the overall default.
	 * @param horizontalSpan number of columns this should span
	 */
	public static SystemQualifiedRemoteFolderCombo createQualifiedDirectoryCombo(Composite parent, SelectionListener listener, int horizontalSpan, String historyKey) {
		if (historyKey == null)
			historyKey = ISystemPreferencesConstants.HISTORY_QUALIFIED_FOLDER;
		SystemQualifiedRemoteFolderCombo combo = new SystemQualifiedRemoteFolderCombo(parent, SWT.NULL, historyKey);
		if (listener != null)
			combo.addSelectionListener(listener);
		boolean hasGridData = (combo.getLayoutData() != null) && (combo.getLayoutData() instanceof GridData);
		//System.out.println("history directory griddata non-null? " + hasGridData);
		int minwidth = 250;
		if (!hasGridData) {
			GridData data = new GridData();
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			data.widthHint = minwidth;
			data.verticalAlignment = GridData.CENTER;
			data.grabExcessVerticalSpace = false;
			data.horizontalSpan = horizontalSpan;
			combo.setLayoutData(data);
		} else {
			((GridData) combo.getLayoutData()).horizontalSpan = horizontalSpan;
			((GridData) combo.getLayoutData()).horizontalAlignment = GridData.FILL;
			((GridData) combo.getLayoutData()).grabExcessHorizontalSpace = true;
			((GridData) combo.getLayoutData()).widthHint = minwidth;
		}
		return combo;
	}


}