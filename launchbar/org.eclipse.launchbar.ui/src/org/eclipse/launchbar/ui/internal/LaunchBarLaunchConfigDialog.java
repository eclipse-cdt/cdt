package org.eclipse.launchbar.ui.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPresentationManager;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.launchbar.core.DefaultLaunchDescriptor;
import org.eclipse.launchbar.core.DefaultLaunchDescriptorType;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.ui.ILaunchBarLaunchConfigDialog;
import org.eclipse.launchbar.ui.ILaunchBarUIManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LaunchBarLaunchConfigDialog extends TitleAreaDialog implements ILaunchBarLaunchConfigDialog {

	private final ILaunchConfigurationWorkingCopy workingCopy;
	private final ILaunchDescriptor descriptor;
	private final ILaunchMode mode;
	private final ILaunchTarget target;
	private final ILaunchConfigurationTabGroup buildTabGroup;
	private final String originalName;

	private ILaunchConfigurationTabGroup group;
	private Text nameText;
	private CTabFolder tabFolder;
	private CTabItem lastSelection;
	private ProgressMonitorPart pmPart;
	private boolean initing;

	public static final int ID_DUPLICATE = IDialogConstants.CLIENT_ID + 1;
	public static final int ID_DELETE = IDialogConstants.CLIENT_ID + 2;

	public LaunchBarLaunchConfigDialog(Shell shell, ILaunchConfigurationWorkingCopy workingCopy,
			ILaunchDescriptor descriptor, ILaunchMode mode, ILaunchTarget target,
			ILaunchConfigurationTabGroup buildTabGroup) {
		super(shell);

		this.workingCopy = workingCopy;
		this.descriptor = descriptor;
		this.mode = mode;
		this.target = target;
		this.buildTabGroup = buildTabGroup;
		this.originalName = workingCopy.getName();

		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected int getDialogBoundsStrategy() {
		// Don't persist the size since it'll be different for every config
		return DIALOG_PERSISTLOCATION;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		initing = true;

		getShell().setText(Messages.LaunchBarLaunchConfigDialog_EditConfiguration);
		boolean supportsTargets = true;
		try {
			supportsTargets = descriptor.getType().supportsTargets();
		} catch (CoreException e) {
			Activator.log(e);
		}

		try {
			ILaunchBarUIManager uiManager = Activator.getService(ILaunchBarUIManager.class);
			ILabelProvider labelProvider = uiManager.getLabelProvider(descriptor);
			String descName = labelProvider != null ? labelProvider.getText(descriptor) : descriptor.getName();
			String typeName = workingCopy.getType().getName();
			if (supportsTargets) {
				setTitle(String.format(Messages.LaunchBarLaunchConfigDialog_Edit2, typeName, descName, mode.getLabel(),
						target.getId()));
			} else {
				setTitle(
						String.format(Messages.LaunchBarLaunchConfigDialog_Edit1, typeName, descName, mode.getLabel()));
			}
		} catch (CoreException e) {
			Activator.log(e);
		}

		setMessage(Messages.LaunchBarLaunchConfigDialog_SetParameters);

		// create the top level composite for the dialog area
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());

		Composite nameComp = new Composite(composite, SWT.NONE);
		nameComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		nameComp.setLayout(new GridLayout(2, false));

		Label nameLabel = new Label(nameComp, SWT.NONE);
		nameLabel.setText(Messages.LaunchBarLaunchConfigDialog_LaunchConfigName);

		nameText = new Text(nameComp, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		nameText.setText(workingCopy.getName());
		nameText.addModifyListener(e -> updateMessage());

		tabFolder = new CTabFolder(composite, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabFolder.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				CTabItem selItem = tabFolder.getSelection();
				if (selItem != null) {
					selItem.getControl().setFocus();
				}
			}
		});
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ILaunchConfigurationTab oldTab = (ILaunchConfigurationTab) lastSelection.getData();
				oldTab.deactivated(workingCopy);

				CTabItem selItem = tabFolder.getSelection();
				ILaunchConfigurationTab newTab = (ILaunchConfigurationTab) selItem.getData();
				newTab.activated(workingCopy);

				selItem.getControl().setFocus();
			}
		});

		try {
			if (buildTabGroup != null) {
				buildTabGroup.createTabs(this, mode.getIdentifier());

				for (ILaunchConfigurationTab configTab : buildTabGroup.getTabs()) {
					installTab(configTab, tabFolder);
				}

				buildTabGroup.initializeFrom(workingCopy);
			}

			group = LaunchConfigurationPresentationManager.getDefault().getTabGroup(workingCopy, mode.getIdentifier());
			group.createTabs(this, mode.getIdentifier());

			for (ILaunchConfigurationTab configTab : group.getTabs()) {
				CTabItem tabItem = installTab(configTab, tabFolder);
				if (lastSelection == null) {
					// Select the first tab by default
					tabFolder.setSelection(tabItem);
					lastSelection = tabItem;
				}
			}

			group.initializeFrom(workingCopy);
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}

		pmPart = new ProgressMonitorPart(composite, new GridLayout(), true);
		pmPart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		pmPart.setVisible(false);

		initing = false;
		return composite;
	}

	private CTabItem installTab(ILaunchConfigurationTab tab, CTabFolder tabFolder) {
		tab.setLaunchConfigurationDialog(this);

		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setData(tab);
		tabItem.setText(tab.getName());
		tabItem.setImage(tab.getImage());

		Composite tabComp = new Composite(tabFolder, SWT.NONE);
		tabComp.setLayout(new GridLayout());
		tabItem.setControl(tabComp);

		tab.createControl(tabComp);
		Control configControl = tab.getControl();
		configControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return tabItem;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite buttonBar = (Composite) super.createButtonBar(parent);
		Control[] children = buttonBar.getChildren();
		Control okCancelButtons = children[children.length - 1];
		Control configButtons = createConfigButtons(buttonBar);

		// insert our buttons ahead of the OK/Cancel buttons
		configButtons.moveAbove(okCancelButtons);

		return buttonBar;
	}

	protected Control createConfigButtons(Composite parent) {
		((GridLayout) parent.getLayout()).numColumns++;
		Composite composite = new Composite(parent, SWT.NONE);
		// create a layout with spacing and margins appropriate for the font size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 0; // this is incremented by createButton
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_CENTER);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());

		// Allow Duplicate only if the resulting configuration is public
		try {
			if (DefaultLaunchDescriptorType.isPublic(workingCopy.getOriginal())) {
				createButton(composite, ID_DUPLICATE, Messages.LaunchBarLaunchConfigDialog_Duplicate, false);
			}
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}

		String deleteText;
		if (descriptor instanceof DefaultLaunchDescriptor) {
			deleteText = Messages.LaunchBarLaunchConfigDialog_Delete;
		} else {
			deleteText = Messages.LaunchBarLaunchConfigDialog_Reset;
		}

		// TODO if the descriptor is not a launch config, this should really say Reset
		createButton(composite, ID_DELETE, deleteText, false);

		return composite;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == ID_DUPLICATE) {
			duplicatePressed();
		} else if (buttonId == ID_DELETE) {
			deletePressed();
		} else {
			super.buttonPressed(buttonId);
		}
	}

	protected void deletePressed() {
		String title, message;
		if (descriptor instanceof DefaultLaunchDescriptor) {
			title = Messages.LaunchBarLaunchConfigDialog_DeleteTitle;
			message = Messages.LaunchBarLaunchConfigDialog_DeleteConfirm;
		} else {
			title = Messages.LaunchBarLaunchConfigDialog_ResetTitle;
			message = Messages.LaunchBarLaunchConfigDialog_ResetConfirm;
		}

		if (MessageDialog.openConfirm(getShell(), title, String.format(message, workingCopy.getName()))) {
			setReturnCode(ID_DELETE);
			close();
		}
	}

	protected void duplicatePressed() {
		setReturnCode(ID_DUPLICATE);
		close();
	}

	@Override
	protected void okPressed() {
		String newName = nameText.getText().trim();
		if (!newName.equals(originalName)) {
			workingCopy.rename(newName);
		}

		if (buildTabGroup != null) {
			buildTabGroup.performApply(workingCopy);
		}
		group.performApply(workingCopy);
		super.okPressed();
	}

	@Override
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		Control lastControl = getShell().getDisplay().getFocusControl();
		if (lastControl != null && lastControl.getShell() != getShell()) {
			lastControl = null;
		}
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
		pmPart.attachToCancelComponent(null);

		try {
			ModalContext.run(runnable, fork, pmPart, getShell().getDisplay());
		} finally {
			pmPart.removeFromCancelComponent(null);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
			getButton(IDialogConstants.CANCEL_ID).setEnabled(true);
			if (lastControl != null) {
				lastControl.setFocus();
			}
			updateButtons();
		}
	}

	@Override
	public void updateButtons() {
		// Lots of tabs want to be applied when this is called
		if (!initing) {
			ILaunchConfigurationTab[] tabs = getTabs();
			if (tabFolder != null && tabs != null) {
				int pageIndex = tabFolder.getSelectionIndex();
				if (pageIndex >= 0) {
					tabs[pageIndex].performApply(workingCopy);
				}
			}
		}
	}

	private String getTabsErrorMessage() {
		ILaunchConfigurationTab activeTab = getActiveTab();
		if (activeTab != null) {
			String message = activeTab.getErrorMessage();
			if (message != null) {
				return message;
			}
		}

		for (ILaunchConfigurationTab tab : getTabs()) {
			if (tab != activeTab) {
				String message = tab.getErrorMessage();
				if (message != null) {
					return message;
				}
			}
		}

		return null;
	}

	private String getTabsMessage() {
		ILaunchConfigurationTab activeTab = getActiveTab();
		if (activeTab != null) {
			String message = activeTab.getMessage();
			if (message != null) {
				return message;
			}
		}

		for (ILaunchConfigurationTab tab : getTabs()) {
			if (tab != activeTab) {
				String message = tab.getMessage();
				if (message != null) {
					return message;
				}
			}
		}

		return null;
	}

	@Override
	public void updateMessage() {
		if (initing) {
			return;
		}

		String newName = nameText.getText().trim();
		if (newName.isEmpty()) {
			setMessage(Messages.LaunchBarLaunchConfigDialog_LCMustHaveName, IMessageProvider.ERROR);
			return;
		}

		if (!newName.equals(originalName)) {
			// make sure it's not taken
			try {
				ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

				if (manager.isExistingLaunchConfigurationName(newName)) {
					setMessage(Messages.LaunchBarLaunchConfigDialog_LCNameExists, IMessageProvider.ERROR);
					return;
				}

				if (!manager.isValidLaunchConfigurationName(newName)) {
					setMessage(Messages.LaunchBarLaunchConfigDialog_LCNameNotValid, IMessageProvider.ERROR);
					return;
				}
			} catch (CoreException e1) {
				Activator.log(e1.getStatus());
			}
		}

		for (ILaunchConfigurationTab tab : getTabs()) {
			tab.isValid(workingCopy);
		}

		Button okButton = getButton(IDialogConstants.OK_ID);

		String message = getTabsErrorMessage();
		if (message != null) {
			setMessage(message, IMessageProvider.ERROR);
			okButton.setEnabled(false);
			return;
		}

		message = getTabsMessage();
		setMessage(message);
		okButton.setEnabled(true);
	}

	@Override
	public void setName(String name) {
		if (nameText != null && !nameText.isDisposed()) {
			nameText.setText(name);
		}
	}

	@Override
	public String generateName(String name) {
		return DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(name);
	}

	@Override
	public ILaunchConfigurationTab[] getTabs() {
		if (buildTabGroup != null) {
			ILaunchConfigurationTab[] buildTabs = buildTabGroup.getTabs();
			ILaunchConfigurationTab[] mainTabs = group.getTabs();
			ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[buildTabs.length + mainTabs.length];
			System.arraycopy(buildTabs, 0, tabs, 0, buildTabs.length);
			System.arraycopy(mainTabs, 0, tabs, buildTabs.length, mainTabs.length);
			return tabs;
		} else {
			return group.getTabs();
		}
	}

	@Override
	public ILaunchConfigurationTab getActiveTab() {
		CTabItem selItem = tabFolder.getSelection();
		if (selItem != null) {
			return (ILaunchConfigurationTab) selItem.getData();
		} else {
			return null;
		}
	}

	@Override
	public String getMode() {
		return mode.getIdentifier();
	}

	@Override
	public ILaunchTarget getLaunchTarget() {
		return target;
	}

	@Override
	public void setActiveTab(ILaunchConfigurationTab tab) {
		for (CTabItem item : tabFolder.getItems()) {
			if (tab.equals(item.getData())) {
				tabFolder.setSelection(item);
				return;
			}
		}
	}

	@Override
	public void setActiveTab(int index) {
		tabFolder.setSelection(index);
	}

}
