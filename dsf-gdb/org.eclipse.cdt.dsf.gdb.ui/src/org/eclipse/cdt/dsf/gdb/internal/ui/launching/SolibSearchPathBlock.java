/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryShared;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IListAdapter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cdt.debug.internal.ui.dialogfields.ListDialogField;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;

/**
 * The UI component to access the shared libraries search path.
 */
public class SolibSearchPathBlock extends Observable implements IMILaunchConfigurationComponent, IDialogFieldListener {

	class AddDirectoryDialog extends Dialog {

		protected Text fText;

		private Button fBrowseButton;

		private IPath fValue;

		/**
		 * Constructor for AddDirectoryDialog.
		 */
		public AddDirectoryDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);

			Composite subComp = ControlFactory.createCompositeEx(composite, 2, GridData.FILL_HORIZONTAL);
			((GridLayout) subComp.getLayout()).makeColumnsEqualWidth = false;
			GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
					| GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			subComp.setLayoutData(data);
			subComp.setFont(parent.getFont());

			fText = new Text(subComp, SWT.SINGLE | SWT.BORDER);
			fText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
			fText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					updateOKButton();
				}
			});

			fBrowseButton = ControlFactory.createPushButton(subComp,
					LaunchUIMessages.getString("GDBServerDebuggerPage.7")); //$NON-NLS-1$
			data = new GridData();
			data.horizontalAlignment = GridData.FILL;
			fBrowseButton.setLayoutData(data);
			fBrowseButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent evt) {
					DirectoryDialog dialog = new DirectoryDialog(AddDirectoryDialog.this.getShell());
					dialog.setMessage(LaunchUIMessages.getString("SolibSearchPathBlock.5")); //$NON-NLS-1$
					String res = dialog.open();
					if (res != null) {
						fText.setText(res);
					}
				}
			});

			applyDialogFont(composite);
			return composite;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(LaunchUIMessages.getString("SolibSearchPathBlock.Add_Directory")); //$NON-NLS-1$
		}

		public IPath getValue() {
			return fValue;
		}

		private void setValue(String value) {
			fValue = (value != null) ? new Path(value) : null;
		}

		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.OK_ID) {
				setValue(fText.getText());
			} else {
				setValue(null);
			}
			super.buttonPressed(buttonId);
		}

		protected void updateOKButton() {
			Button okButton = getButton(IDialogConstants.OK_ID);
			String text = fText.getText();
			okButton.setEnabled(isValid(text));
		}

		protected boolean isValid(String text) {
			return (text.trim().length() > 0);
		}

		@Override
		protected Control createButtonBar(Composite parent) {
			Control control = super.createButtonBar(parent);
			updateOKButton();
			return control;
		}
	}

	private Composite fControl;

	public class SolibSearchPathListDialogField extends ListDialogField {

		public SolibSearchPathListDialogField(IListAdapter adapter, String[] buttonLabels, ILabelProvider lprovider) {
			super(adapter, buttonLabels, lprovider);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.internal.ui.dialogfields.ListDialogField#managedButtonPressed(int)
		 */
		@Override
		protected boolean managedButtonPressed(int index) {
			boolean result = super.managedButtonPressed(index);
			if (result)
				buttonPressed(index);
			return result;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.mi.internal.ui.dialogfields.ListDialogField#getManagedButtonState(org.eclipse.jface.viewers.ISelection, int)
		 */
		@Override
		protected boolean getManagedButtonState(ISelection sel, int index) {
			if (index > 3)
				return getButtonState(sel, index);
			return super.getManagedButtonState(sel, index);
		}
	}

	private static String[] fgStaticButtonLabels = new String[] { LaunchUIMessages.getString("SolibSearchPathBlock.0"), //$NON-NLS-1$
			LaunchUIMessages.getString("SolibSearchPathBlock.1"), //$NON-NLS-1$
			LaunchUIMessages.getString("SolibSearchPathBlock.2"), //$NON-NLS-1$
			LaunchUIMessages.getString("SolibSearchPathBlock.3"), //$NON-NLS-1$
			LaunchUIMessages.getString("SolibSearchPathBlock.6"), //$NON-NLS-1$
			null, // separator
	};

	private IProject fProject;

	private Shell fShell;

	private SolibSearchPathListDialogField fDirList;

	private IListAdapter fCustomListAdapter;

	private File[] fAutoSolibs = new File[0];

	public SolibSearchPathBlock() {
		this(new String[0], null);
	}

	public SolibSearchPathBlock(String[] customButtonLabels, IListAdapter customListAdapter) {
		super();
		fCustomListAdapter = customListAdapter;
		int length = fgStaticButtonLabels.length;
		if (customButtonLabels.length > 0)
			length += customButtonLabels.length;
		String[] buttonLabels = new String[length];
		System.arraycopy(fgStaticButtonLabels, 0, buttonLabels, 0, fgStaticButtonLabels.length);
		if (length > fgStaticButtonLabels.length) {
			for (int i = fgStaticButtonLabels.length; i < length; ++i)
				buttonLabels[i] = customButtonLabels[i - fgStaticButtonLabels.length];
		}
		IListAdapter listAdapter = new IListAdapter() {
			@Override
			public void customButtonPressed(DialogField field, int index) {
				buttonPressed(index);
			}

			@Override
			public void selectionChanged(DialogField field) {
			}
		};
		ILabelProvider lp = new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IPath)
					return ((IPath) element).toOSString();
				return super.getText(element);
			}
		};
		fDirList = new SolibSearchPathListDialogField(listAdapter, buttonLabels, lp);
		fDirList.setLabelText(LaunchUIMessages.getString("SolibSearchPathBlock.4")); //$NON-NLS-1$
		fDirList.setUpButtonIndex(1);
		fDirList.setDownButtonIndex(2);
		fDirList.setRemoveButtonIndex(3);

		fDirList.setDialogFieldListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		fShell = parent.getShell();
		Composite comp = ControlFactory.createCompositeEx(parent, 2, GridData.FILL_BOTH);
		((GridLayout) comp.getLayout()).makeColumnsEqualWidth = false;
		((GridLayout) comp.getLayout()).marginHeight = 0;
		((GridLayout) comp.getLayout()).marginWidth = 0;
		comp.setFont(parent.getFont());
		PixelConverter converter = new PixelConverter(comp);
		fDirList.doFillIntoGrid(comp, 3);
		LayoutUtil.setHorizontalSpan(fDirList.getLabelControl(null), 2);
		LayoutUtil.setWidthHint(fDirList.getLabelControl(null), converter.convertWidthInCharsToPixels(30));
		LayoutUtil.setHorizontalGrabbing(fDirList.getListControl(null));
		fControl = comp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		IProject project = null;
		try {
			String projectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					(String) null);
			if (projectName != null) {
				projectName = projectName.trim();
				if (!projectName.isEmpty()) {
					project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				}
			}
		} catch (CoreException e) {
		}
		setProject(project);

		if (fDirList != null) {
			try {
				List<String> values = configuration.getAttribute(
						IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, Collections.emptyList());
				ArrayList<Path> paths = new ArrayList<>(values.size());
				Iterator<String> it = values.iterator();
				while (it.hasNext()) {
					paths.add(new Path(it.next()));
				}
				fDirList.addElements(paths);
			} catch (CoreException e) {
			}
		}

		try {
			fAutoSolibs = getAutoSolibs(configuration);
		} catch (CoreException e) {
		}
	}

	public static File[] getAutoSolibs(ILaunchConfiguration configuration) throws CoreException {
		List<String> autoSolibs = configuration
				.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB_LIST, Collections.emptyList());

		List<File> list = new ArrayList<>(autoSolibs.size());
		Iterator<String> it = autoSolibs.iterator();
		while (it.hasNext()) {
			list.add(new File(it.next()));
		}
		return list.toArray(new File[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, Collections.emptyList());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (fDirList != null) {

			@SuppressWarnings("unchecked")
			List<IPath> elements = fDirList.getElements();

			ArrayList<String> values = new ArrayList<>(elements.size());
			Iterator<IPath> it = elements.iterator();
			while (it.hasNext()) {
				values.add((it.next()).toOSString());
			}
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, values);
		}
		ArrayList<String> autoLibs = new ArrayList<>(fAutoSolibs.length);
		for (int i = 0; i < fAutoSolibs.length; ++i)
			autoLibs.add(fAutoSolibs[i].getPath());
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB_LIST, autoLibs);
	}

	protected void buttonPressed(int index) {
		boolean changed = false;
		if (index == 0) { // Add button
			changed = addDirectory();
		} else if (index == 4) { //Select from list
			changed = selectFromList();
		} else if (index >= fgStaticButtonLabels.length && fCustomListAdapter != null) {
			fCustomListAdapter.customButtonPressed(fDirList, index);
			changed = true;
		}
		if (changed) {
			setChanged();
			notifyObservers();
		}
	}

	protected boolean getButtonState(ISelection sel, int index) {
		if (index == 4) { // select from list
			return (!sel.isEmpty());
		}
		return true;
	}

	protected Shell getShell() {
		return fShell;
	}

	private boolean addDirectory() {
		boolean changed = false;
		AddDirectoryDialog dialog = new AddDirectoryDialog(getShell());
		dialog.open();
		IPath result = dialog.getValue();
		if (result != null && !contains(result)) {
			fDirList.addElement(result);
			changed = true;
		}
		return changed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#dispose()
	 */
	@Override
	public void dispose() {
		deleteObservers();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#getControl()
	 */
	@Override
	public Control getControl() {
		return fControl;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean contains(IPath path) {
		@SuppressWarnings("unchecked")
		List<IPath> list = fDirList.getElements();

		Iterator<IPath> it = list.iterator();
		while (it.hasNext()) {
			IPath p = it.next();
			if (p.toFile().equals(path.toFile()))
				return true;
		}
		return false;
	}

	protected IProject getProject() {
		return fProject;
	}

	private void setProject(IProject project) {
		fProject = project;
	}

	protected boolean selectFromList() {
		boolean changed = false;

		@SuppressWarnings("unchecked")
		List<IPath> dirList = fDirList.getSelectedElements();

		final HashSet<File> libs = new HashSet<>(10);
		if (generateLibraryList(dirList.toArray(new IPath[dirList.size()]), libs)) {
			ITreeContentProvider cp = new ITreeContentProvider() {
				@Override
				public Object[] getChildren(Object parentElement) {
					return getElements(parentElement);
				}

				@Override
				public Object getParent(Object element) {
					if (libs.contains(element))
						return libs;
					return null;
				}

				@Override
				public boolean hasChildren(Object element) {
					return false;
				}

				@Override
				public Object[] getElements(Object inputElement) {
					if (inputElement instanceof Set) {
						return ((Set<?>) inputElement).toArray();
					}
					return new Object[0];
				}

				@Override
				public void dispose() {
				}

				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}
			};

			LabelProvider lp = new LabelProvider() {

				@Override
				public String getText(Object element) {
					if (element instanceof File)
						return ((File) element).getName();
					return super.getText(element);
				}
			};
			CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(getShell(), lp, cp);
			dialog.setTitle(LaunchUIMessages.getString("SolibSearchPathBlock.7")); //$NON-NLS-1$
			dialog.setMessage(LaunchUIMessages.getString("SolibSearchPathBlock.8")); //$NON-NLS-1$
			dialog.setEmptyListMessage(LaunchUIMessages.getString("SolibSearchPathBlock.9")); //$NON-NLS-1$
			dialog.setComparator(new ViewerComparator());
			dialog.setInput(libs);
			dialog.setInitialElementSelections(Arrays.asList(fAutoSolibs));
			if (dialog.open() == Window.OK) {
				Object[] result = dialog.getResult();
				fAutoSolibs = Arrays.asList(result).toArray(new File[result.length]);
				changed = true;
			}
		}
		return changed;
	}

	private boolean generateLibraryList(final IPath[] paths, final Set<File> libs) {
		boolean result = true;

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				for (int i = 0; i < paths.length; ++i) {
					File dir = paths[i].toFile();
					if (dir.exists() && dir.isDirectory()) {
						File[] all = dir.listFiles();
						for (int j = 0; j < all.length; ++j) {
							if (monitor.isCanceled()) {
								throw new InterruptedException();
							}
							monitor.subTask(all[j].getPath());
							String libName = getSharedLibraryName(all[j]);
							if (libName != null) {
								libs.add(new File(libName));
							}
						}
					}
				}
			}
		};
		try {
			IRunnableContext context = new ProgressMonitorDialog(getShell());
			context.run(true, true, runnable);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
			result = false;
		}
		return result;
	}

	protected String getSharedLibraryName(File file) {
		if (!file.isFile())
			return null;
		IProject project = getProject();
		if (project != null) {
			IPath fullPath = new Path(file.getPath());
			try {
				ICConfigExtensionReference[] binaryParsersExt = CCorePlugin.getDefault()
						.getDefaultBinaryParserExtensions(project);
				for (int i = 0; i < binaryParsersExt.length; i++) {
					IBinaryParser parser = CoreModelUtil.getBinaryParser(binaryParsersExt[i]);
					try {
						IBinaryFile bin = parser.getBinary(fullPath);
						if (bin instanceof IBinaryShared) {
							String soname = ((IBinaryShared) bin).getSoName();
							return (soname.length() != 0) ? soname : file.getName();
						}
					} catch (IOException e) {
					}
				}
			} catch (CoreException e) {
			}
			return null;
		}
		// no project: for now
		IPath path = new Path(file.getPath());
		String name = path.lastSegment();
		String extension = path.getFileExtension();
		if (extension != null && (extension.compareTo("so") == 0 || extension.compareToIgnoreCase("dll") == 0)) //$NON-NLS-1$ //$NON-NLS-2$
			return name;
		return (name.indexOf(".so.") >= 0) ? name : null; //$NON-NLS-1$
	}

	protected boolean isSharedLibrary(File file) {
		if (!file.isFile())
			return false;
		IProject project = getProject();
		if (project != null) {
			IPath fullPath = new Path(file.getPath());
			try {
				ICConfigExtensionReference[] binaryParsersExt = CCorePlugin.getDefault()
						.getDefaultBinaryParserExtensions(project);
				for (int i = 0; i < binaryParsersExt.length; i++) {
					IBinaryParser parser = CoreModelUtil.getBinaryParser(binaryParsersExt[i]);
					try {
						IBinaryFile bin = parser.getBinary(fullPath);
						return (bin instanceof IBinaryShared);
					} catch (IOException e) {
					}
				}
			} catch (CoreException e) {
			}
			return false;
		}
		// no project: for now
		IPath path = new Path(file.getPath());
		String extension = path.getFileExtension();
		if (extension != null && (extension.compareTo("so") == 0 || extension.compareToIgnoreCase("dll") == 0)) //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		String name = path.lastSegment();
		return (name.indexOf(".so.") >= 0); //$NON-NLS-1$
	}

	@Override
	public void dialogFieldChanged(DialogField field) {
		setChanged();
		notifyObservers();
	}
}
