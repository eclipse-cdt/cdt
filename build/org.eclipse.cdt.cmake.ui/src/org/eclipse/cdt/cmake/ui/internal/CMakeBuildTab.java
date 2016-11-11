package org.eclipse.cdt.cmake.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.cmake.core.internal.CMakeBuildConfiguration;
import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchConfigDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CMakeBuildTab extends AbstractLaunchConfigurationTab {

	private Button unixGenButton;
	private Button ninjaGenButton;
	private Text cmakeArgsText;
	private Text buildCommandText;
	private Text cleanCommandText;

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		setControl(comp);

		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.CMakeBuildTab_Generator);

		Composite genComp = new Composite(comp, SWT.BORDER);
		genComp.setLayout(new GridLayout(2, true));

		unixGenButton = new Button(genComp, SWT.RADIO);
		unixGenButton.setText(Messages.CMakeBuildTab_UnixMakefiles);
		unixGenButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		ninjaGenButton = new Button(genComp, SWT.RADIO);
		ninjaGenButton.setText(Messages.CMakeBuildTab_Ninja);
		ninjaGenButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.CMakeBuildTab_CMakeArgs);

		cmakeArgsText = new Text(comp, SWT.BORDER);
		cmakeArgsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		cmakeArgsText.addModifyListener(e -> updateLaunchConfigurationDialog());

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.CMakeBuildTab_BuildCommand);

		buildCommandText = new Text(comp, SWT.BORDER);
		buildCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buildCommandText.addModifyListener(e -> updateLaunchConfigurationDialog());

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.CMakeBuildTab_CleanCommand);

		cleanCommandText = new Text(comp, SWT.BORDER);
		cleanCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		cleanCommandText.addModifyListener(e -> updateLaunchConfigurationDialog());
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		String mode = getLaunchConfigurationDialog().getMode();
		configuration.removeAttribute("COREBUILD_" + mode); //$NON-NLS-1$
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String mode = getLaunchConfigurationDialog().getMode();
			// TODO find a home for the attribute name
			Map<String, String> properties = configuration.getAttribute("COREBUILD_" + mode, //$NON-NLS-1$
					new HashMap<>());

			String generator = properties.get(CMakeBuildConfiguration.CMAKE_GENERATOR);
			updateGeneratorButtons(generator);

			String cmakeArgs = properties.get(CMakeBuildConfiguration.CMAKE_ARGUMENTS);
			if (cmakeArgs != null) {
				cmakeArgsText.setText(cmakeArgs);
			} else {
				cmakeArgsText.setText(""); //$NON-NLS-1$
			}

			String buildCommand = properties.get(CMakeBuildConfiguration.BUILD_COMMAND);
			if (buildCommand != null) {
				buildCommandText.setText(buildCommand);
			} else {
				buildCommandText.setText(""); //$NON-NLS-1$
			}

			String cleanCommand = properties.get(CMakeBuildConfiguration.CLEAN_COMMAND);
			if (cleanCommand != null) {
				cleanCommandText.setText(buildCommand);
			} else {
				cleanCommandText.setText(""); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private void updateGeneratorButtons(String generator) {
		if (generator != null && generator.equals("Ninja")) { //$NON-NLS-1$
			ninjaGenButton.setSelection(true);
		} else {
			unixGenButton.setSelection(true);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		Map<String, String> properties = new HashMap<>();

		if (ninjaGenButton.getSelection()) {
			properties.put(CMakeBuildConfiguration.CMAKE_GENERATOR, "Ninja"); //$NON-NLS-1$
		}

		String cmakeArgs = cmakeArgsText.getText().trim();
		if (!cmakeArgs.isEmpty()) {
			properties.put(CMakeBuildConfiguration.CMAKE_ARGUMENTS, cmakeArgs);
		}

		String buildCommand = buildCommandText.getText().trim();
		if (!buildCommand.isEmpty()) {
			properties.put(CMakeBuildConfiguration.BUILD_COMMAND, buildCommand);
		}

		String cleanCommand = cleanCommandText.getText().trim();
		if (!cleanCommand.isEmpty()) {
			properties.put(CMakeBuildConfiguration.CLEAN_COMMAND, cleanCommand);
		}

		String buildAttribute = CoreBuildLaunchConfigDelegate
				.getBuildAttributeName(getLaunchConfigurationDialog().getMode());
		if (!properties.isEmpty()) {
			configuration.setAttribute(buildAttribute, properties);
		} else {
			configuration.removeAttribute(buildAttribute);
		}
	}

	@Override
	public String getName() {
		return Messages.CMakeBuildTab_Cmake;
	}

}
