package org.eclipse.cdt.cmake.ui.properties;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page for CMake projects. The only thing we have here at the moment is a button
 * to launch the CMake GUI configurator (cmake-qt-gui).
 * 
 * We assume that the build directory is in project/build/configname, which is where
 * the CMake project wizard puts it. We also assume that "cmake-gui" is in the user's 
 * PATH.
 * 
 * @author jesperes
 */
public class CMakePropertyPage extends PropertyPage {

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		Button b = new Button(composite, SWT.NONE);
		b.setText("Launch CMake GUI...");
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IProject project = (IProject) getElement();
				try {
					String configName = project.getActiveBuildConfig().getName();
					String sourceDir = project.getLocation().toOSString();
					String buildDir = project.getLocation().append("build").append(configName).toOSString();
					
					Runtime.getRuntime().exec(new String[] { "cmake-gui", "-H" + sourceDir, "-B" + buildDir });
				} catch (CoreException | IOException e1) {
					MessageDialog.openError(parent.getShell(), "Failed to run CMake GUI", 
							"Failed to run the CMake GUI: " + e1.getMessage());
				}
			}
		});
		
		return composite;
	}
}
