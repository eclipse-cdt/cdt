package org.eclipse.cdt.make.ui.dialogs;

import java.util.HashMap;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.RadioButtonsArea;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class BuildTargetDialog extends TitleAreaDialog {
	private IMakeTarget fSelection;
	public static int OPEN_MODE_BUILD = 1;
	public static int OPEN_MODE_CREATE_NEW = 2;
	public static int OPEN_MODE_RENAME = 2;

	private int openMode;

	private static final String PREFIX = "SettingsBlock"; //$NON-NLS-1$

	private static final String MAKE_SETTING_GROUP = PREFIX + ".makeSetting.group_label"; //$NON-NLS-1$
	private static final String MAKE_SETTING_KEEP_GOING = PREFIX + ".makeSetting.keepOnGoing"; //$NON-NLS-1$
	private static final String MAKE_SETTING_STOP_ERROR = PREFIX + ".makeSetting.stopOnError"; //$NON-NLS-1$

	private static final String MAKE_CMD_GROUP = PREFIX + ".makeCmd.group_label"; //$NON-NLS-1$
	private static final String MAKE_CMD_USE_DEFAULT = PREFIX + ".makeCmd.use_default"; //$NON-NLS-1$
	private static final String MAKE_CMD_LABEL = PREFIX + ".makeCmd.label"; //$NON-NLS-1$

	private static final String KEEP_ARG = "keep"; //$NON-NLS-1$
	private static final String STOP_ARG = "stop"; //$NON-NLS-1$

	RadioButtonsArea stopRadioButtons;
	Text buildCommand;
	Button defButton;

	IMakeBuilderInfo fBuildInfo;
	IMakeTargetManager fTargetManager;

	/**
	 * @param parentShell
	 */
	public BuildTargetDialog(Shell parentShell, IContainer container) {
		super(parentShell);
		fTargetManager = MakeCorePlugin.getDefault().getTargetManager();
		String[] id = fTargetManager.getTargetBuilders(container.getProject());
		if (id != null) {
			fBuildInfo = MakeCorePlugin.createBuildInfo(new HashMap(), id[0]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);

		createSettingControls(parent);
		createBuildCmdControls(parent);

		return control;
	}

	protected void createSettingControls(Composite parent) {
		String[][] radios = new String[][] { { MakeUIPlugin.getResourceString(MAKE_SETTING_STOP_ERROR), STOP_ARG }, {
				MakeUIPlugin.getResourceString(MAKE_SETTING_KEEP_GOING), KEEP_ARG }
		};
		stopRadioButtons = new RadioButtonsArea(parent, MakeUIPlugin.getResourceString(MAKE_SETTING_GROUP), 1, radios);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		stopRadioButtons.setLayout(layout);
		if (fBuildInfo.isStopOnError())
			stopRadioButtons.setSelectValue(STOP_ARG);
		else
			stopRadioButtons.setSelectValue(KEEP_ARG);
	}

	protected void createBuildCmdControls(Composite parent) {
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(MAKE_CMD_GROUP), 1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.horizontalSpacing = 0;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		defButton = ControlFactory.createCheckBox(group, MakeUIPlugin.getResourceString(MAKE_CMD_USE_DEFAULT));
		defButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (defButton.getSelection() == true) {
					buildCommand.setEnabled(false);
					stopRadioButtons.setEnabled(true);
				} else {
					buildCommand.setEnabled(true);
					stopRadioButtons.setEnabled(false);
				}
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		defButton.setLayoutData(gd);
		Label label = ControlFactory.createLabel(group, MakeUIPlugin.getResourceString(MAKE_CMD_LABEL));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		buildCommand = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		((GridData) (buildCommand.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (buildCommand.getLayoutData())).grabExcessHorizontalSpace = true;
		buildCommand.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
			}
		});
		if (fBuildInfo.getBuildCommand() != null) {
			StringBuffer cmd = new StringBuffer(fBuildInfo.getBuildCommand().toOSString());
			if (!fBuildInfo.isDefaultBuildCmd()) {
				String args = fBuildInfo.getBuildArguments();
				if (args != null && !args.equals("")) { //$NON-NLS-1$
					cmd.append(" "); //$NON-NLS-1$
					cmd.append(args);
				}
			}
			buildCommand.setText(cmd.toString());
		}
		if (fBuildInfo.isDefaultBuildCmd()) {
			buildCommand.setEnabled(false);
		} else {
			stopRadioButtons.setEnabled(false);
		}
		defButton.setSelection(fBuildInfo.isDefaultBuildCmd());
	}

	public void setOpenMode(int mode) {
		openMode = mode;
	}

	public void setSelectedTarget(IMakeTarget target) {
		fSelection = target;
	}

	public IMakeTarget getSelectedTarget() {
		return null;
	}

}
