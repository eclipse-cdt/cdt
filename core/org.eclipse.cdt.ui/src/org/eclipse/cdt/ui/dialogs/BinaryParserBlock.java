/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;

public class BinaryParserBlock extends AbstractBinaryParserPage {

	private static final int DEFAULT_HEIGHT = 160;
	private static final String PREFIX = "BinaryParserBlock"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String DESC = PREFIX + ".desc"; //$NON-NLS-1$

	private static final String ATTR_FILTER = "filter"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_NAME_VISIBILITY = "visibility"; //$NON-NLS-1$
	private static final String ATTR_VALUE = "value"; //$NON-NLS-1$
	private static final String ATTR_VALUE_PRIVATE = "private"; //$NON-NLS-1$

	protected CheckedListDialogField binaryList;
	Map configMap;
	List initialSelected;

	protected class BinaryParserConfiguration {

		IExtension fExtension;
		public BinaryParserConfiguration(IExtension extension) {
			fExtension = extension;
		}

		public String getID() {
			return fExtension.getUniqueIdentifier();
		}

		public String getName() {
			return fExtension.getLabel();
		}

		public String toString() {
			return fExtension.getUniqueIdentifier();
		}

		public boolean equals(Object obj) {
			if (obj instanceof BinaryParserConfiguration) {
				return this.getID().equals(((BinaryParserConfiguration) obj).getID());
			}
			return super.equals(obj);
		}
	}

	protected class BinaryParserLabelProvider extends LabelProvider {

		public String getText(Object element) {
			return ((BinaryParserConfiguration) element).getName();
		}
	}

	public BinaryParserBlock() {
		super(CUIPlugin.getResourceString(LABEL));
		setDescription(CUIPlugin.getResourceString(DESC));
		String[] buttonLabels = new String[]{
		/* 0 */CUIMessages.getString("BinaryParserBlock.button.up"), //$NON-NLS-1$
				/* 1 */CUIMessages.getString("BinaryParserBlock.button.down")}; //$NON-NLS-1$

		IListAdapter listAdapter = new IListAdapter() {

			public void customButtonPressed(ListDialogField field, int index) {
			}

			public void selectionChanged(ListDialogField field) {
				handleBinaryParserChanged();
			}

			public void doubleClicked(ListDialogField field) {
			}

		};

		binaryList = new CheckedListDialogField(listAdapter, buttonLabels, new BinaryParserLabelProvider()) {

			protected int getListStyle() {
				int style = super.getListStyle();
				return style & ~SWT.MULTI;
			}
		};
		binaryList.setDialogFieldListener(new IDialogFieldListener() {

			public void dialogFieldChanged(DialogField field) {
				if (getContainer() != null) {
					getContainer().updateContainer();
					handleBinaryParserChanged();
				}
			}
		});
		binaryList.setLabelText(CUIMessages.getString("BinaryParserBlock.binaryParser")); //$NON-NLS-1$
		binaryList.setUpButtonIndex(0);
		binaryList.setDownButtonIndex(1);
		initializeParserList();
	}

	private void initializeParserList() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, CCorePlugin.BINARY_PARSER_SIMPLE_ID);
		if (point != null) {
			IExtension[] exts = point.getExtensions();
			configMap = new HashMap(exts.length);
			for (int i = 0; i < exts.length; i++) {
				if (isExtensionVisible(exts[i])) {
					configMap.put(exts[i].getUniqueIdentifier(), new BinaryParserConfiguration(exts[i]));
				}
			}
		}
	}

	private boolean isExtensionVisible(IExtension ext) {
 		IConfigurationElement[] elements = ext.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement[] children = elements[i].getChildren(ATTR_FILTER);
			for (int j = 0; j < children.length; j++) {
				String name = children[j].getAttribute(ATTR_NAME);
				if (name != null && name.equals(ATTR_NAME_VISIBILITY)) {
					String value = children[j].getAttribute(ATTR_VALUE);
					if (value != null && value.equals(ATTR_VALUE_PRIVATE)) {
						return false;
					}
				}
			}
			return true;
		}
		return false; // invalid extension definition (must have at least cextension elements)
	}

	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);

		Composite composite = ControlFactory.createComposite(parent, 1);
		((GridData) (composite.getLayoutData())).horizontalAlignment = GridData.FILL_HORIZONTAL;
		setControl(composite);

		WorkbenchHelp.setHelp(getControl(), ICHelpContextIds.BINARY_PARSER_PAGE);

		Composite listComposite = ControlFactory.createComposite(composite, 1);
		LayoutUtil.doDefaultLayout(listComposite, new DialogField[]{binaryList}, true);
		LayoutUtil.setHorizontalGrabbing(binaryList.getListControl(null));

		int buttonBarWidth = converter.convertWidthInCharsToPixels(15);
		binaryList.setButtonsMinWidth(buttonBarWidth);

		// Add the Parser UI contribution.
		
		Composite parserGroup = new Composite(composite, SWT.NULL);
		
		GridData gd = new GridData();
		gd.heightHint = converter.convertHorizontalDLUsToPixels(DEFAULT_HEIGHT);
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		parserGroup.setLayoutData(gd);
		// Must set the composite parent to super class.
		setCompositeParent(parserGroup);
		// fire a change event, to quick start.
		handleBinaryParserChanged();
		parent.layout(true);

	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(CUIMessages.getString("BinaryParserBlock.settingBinaryParser"), 2); //$NON-NLS-1$
		List parsers = binaryList.getElements();
		final List selected = new ArrayList(); // must do this to get proper order.
		for (int i = 0; i < parsers.size(); i++) {
			if (binaryList.isChecked(parsers.get(i))) {
				selected.add(parsers.get(i));
			}
		}
		if (selected != null) {
			if (getContainer().getProject() != null) {
				ICDescriptorOperation op = new ICDescriptorOperation() {

					public void execute(ICDescriptor descriptor, IProgressMonitor monitor) throws CoreException {
						if (initialSelected == null || !selected.equals(initialSelected)) {
							descriptor.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
							for (int i = 0; i < selected.size(); i++) {
								descriptor.create(CCorePlugin.BINARY_PARSER_UNIQ_ID,
										((BinaryParserConfiguration) selected.get(i)).getID());
							}
						}
						monitor.worked(1);
						// Give a chance to the contributions to save.
						// We have to do it last to make sure the parser id
						// is save
						// in .cdtproject
						for (int i = 0; i < selected.size(); i++) {
							ICOptionPage page = getBinaryParserPage(((BinaryParserConfiguration) selected.get(i)).getID());
							if (page != null && page.getControl() != null) {
								page.performApply(new SubProgressMonitor(monitor, 1));
							}
						}
					}
				};
				CCorePlugin.getDefault().getCDescriptorManager().runDescriptorOperation(getContainer().getProject(), op, monitor);
			} else {
				if (initialSelected == null || !selected.equals(initialSelected)) {
					Preferences store = getContainer().getPreferences();
					if (store != null) {
						store.setValue(CCorePlugin.PREF_BINARY_PARSER, arrayToString(selected.toArray()));
					}
				}
				monitor.worked(1);
				// Give a chance to the contributions to save.
				for (int i = 0; i < selected.size(); i++) {
					ICOptionPage page = getBinaryParserPage(((BinaryParserConfiguration) selected.get(i)).getID());
					if (page != null && page.getControl() != null) {
						page.performApply(new SubProgressMonitor(monitor, 1));
					}
				}
			}
			initialSelected = selected;
		}
		monitor.done();
	}

	public void setContainer(ICOptionContainer container) {
		super.setContainer(container);

		List elements = new ArrayList();

		if (getContainer().getProject() != null) {
			try {
				ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(getContainer().getProject(), true);
				ICExtensionReference[] ref = desc.get(CCorePlugin.BINARY_PARSER_UNIQ_ID);
				initialSelected = new ArrayList(ref.length);
				for (int i = 0; i < ref.length; i++) {
					if (configMap.get(ref[i].getID()) != null) {
						initialSelected.add(configMap.get(ref[i].getID()));
						elements.add(configMap.get(ref[i].getID()));
					}
				}
			} catch (CoreException e) {
			}
			Iterator iter = configMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				if (!elements.contains(entry.getValue())) {
					elements.add(entry.getValue());
				}
			}
			binaryList.setElements(elements);
			if (initialSelected != null)
				binaryList.setCheckedElements(initialSelected);
		}
		if (initialSelected == null) {
			Preferences store = getContainer().getPreferences();
			String id = null;
			if (store != null) {
				id = store.getString(CCorePlugin.PREF_BINARY_PARSER);
			}

			if (id != null && id.length() > 0) {
				String[] ids = parseStringToArray(id);
				initialSelected = new ArrayList(ids.length);
				for (int i = 0; i < ids.length; i++) {
					if (configMap.get(ids[i]) != null) {
						initialSelected.add(configMap.get(ids[i]));
						elements.add(configMap.get(ids[i]));
					}
				}
			}
			Iterator iter = configMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				if (!elements.contains(entry.getValue())) {
					elements.add(entry.getValue());
				}
			}
			binaryList.setElements(elements);
			if (initialSelected != null)
				binaryList.setCheckedElements(initialSelected);
			// reset this since we only want to prevent applying non-changed selections on the project
			// and project creation we always want to apply selection.
			initialSelected = null;
		}
	}
	private String arrayToString(Object[] array) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			buf.append(array[i].toString()).append(';');
		}
		return buf.toString();
	}

	private String[] parseStringToArray(String syms) {
		if (syms != null && syms.length() > 0) {
			StringTokenizer tok = new StringTokenizer(syms, ";"); //$NON-NLS-1$
			ArrayList list = new ArrayList(tok.countTokens());
			while (tok.hasMoreElements()) {
				list.add(tok.nextToken());
			}
			return (String[]) list.toArray(new String[list.size()]);
		}
		return new String[0];
	}

	public void performDefaults() {
		String id = null;

		// default current pages.
		List selected = binaryList.getCheckedElements();
		for (int i = 0; i < selected.size(); i++) {
			ICOptionPage page = getBinaryParserPage(((BinaryParserConfiguration) selected.get(i)).getID());
			if (page != null) {
				page.performDefaults();
			}
		}
		Preferences store = getContainer().getPreferences();
		if (store != null) {
			if (getContainer().getProject() != null) {
				id = store.getString(CCorePlugin.PREF_BINARY_PARSER);
			} else {
				id = store.getDefaultString(CCorePlugin.PREF_BINARY_PARSER);
			}
		}
		//default selection
		selected.clear();
		if (id != null) {
			String[] ids = parseStringToArray(id);
			for (int i = 0; i < ids.length; i++) {
				if (configMap.get(ids[i]) != null) {
					selected.add(configMap.get(ids[i]));
				}
			}
		}
		binaryList.setCheckedElements(selected);
		binaryList.getTableViewer().setSelection(new StructuredSelection(selected.get(0)), true);
		// Give a change to the UI contributors to react.
		// But do it last after the comboBox is set.
		handleBinaryParserChanged();
		getContainer().updateContainer();
	}

	protected String getCurrentBinaryParserID() {
		List list = binaryList.getSelectedElements();
		if (list.size() > 0) {
			BinaryParserConfiguration selected = (BinaryParserConfiguration) list.get(0);
			//if (binaryList.isChecked(selected)) {
			//	return selected.getID();
			//}
			return selected.getID();
		}
		return null;
	}

	protected String[] getBinaryParserIDs() {
		return (String[]) configMap.keySet().toArray(new String[configMap.keySet().size()]);
	}
}
