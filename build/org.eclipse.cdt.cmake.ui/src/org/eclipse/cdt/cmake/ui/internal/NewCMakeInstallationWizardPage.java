package org.eclipse.cdt.cmake.ui.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.cdt.cmake.core.CMakeInstallation;
import org.eclipse.cdt.cmake.core.ICMakeInstallation;
import org.eclipse.cdt.cmake.core.ICMakeInstallationManager;
import org.eclipse.cdt.cmake.core.ICMakeInstallation.Type;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewCMakeInstallationWizardPage extends WizardPage {

	public static final String PAGE_NAME = "NewCMakeInstallationWizardPage";
	
	private final ICMakeInstallationManager manager;
	
	private Text pathInputText;
	private Path cmakeInstallationPath;
	
	public NewCMakeInstallationWizardPage(ICMakeInstallationManager manager) {
		super(PAGE_NAME, Messages.NewCMakeInstallationPage_Title, null);
		this.manager = manager;
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(2, false));
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label pathLabel = new Label(content, SWT.NONE);
		pathLabel.setText("Path:");
		pathLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		Composite pathInputComposite = new Composite(content, SWT.NONE);
		pathInputComposite.setLayout(new GridLayout(2, false));
		pathInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		pathInputText = new Text(pathInputComposite, SWT.NONE);
		pathInputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pathInputText.addModifyListener(event -> this.checkCMakeLocation());
		
		Button pathInputButton = new Button(pathInputComposite, SWT.PUSH);
		pathInputButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		pathInputButton.setText("Browse");
		pathInputButton.addListener(SWT.Selection, event -> {
			DirectoryDialog chooser = new DirectoryDialog(getShell());
			String path = chooser.open();
			if(path != null) {
				pathInputText.setText(path);
			}
		});
		
		checkCMakeLocation();
		setControl(content);
	}
	
	public ICMakeInstallation getInstallation() throws IOException {
		return new CMakeInstallation(cmakeInstallationPath, Type.CUSTOM);
	}

	private void checkCMakeLocation() {
		String content = pathInputText.getText();
		if(content.isEmpty()) {
			setErrorMessage("Please select a folder containing the CMake binary");
			setPageComplete(false);
			return;
		}
		
		try {
			Path path = Paths.get(content).toRealPath();
			Path binaryPath = path.resolve(ICMakeInstallation.CMAKE_COMMAND);
			if(!Files.exists(binaryPath) || Files.isDirectory(binaryPath)) {
				setErrorMessage("Could not find the CMake executable in the selected location");
				setPageComplete(false);
				return;
			}
	
			boolean alreadyRegistered = manager.getInstallations().stream()
				.map(ICMakeInstallation::getRoot)
				.anyMatch(command -> command.equals(path));
			if(alreadyRegistered) {
				setErrorMessage("This installation is already registered");
				setPageComplete(false);
				return;
			}
			cmakeInstallationPath = path;
			setErrorMessage(null);
			setPageComplete(true);
		} catch (IOException e) {
			Activator.log(e);
			setErrorMessage("Error while checking path: " + e.getMessage());
			setPageComplete(false);
		}
	}

}
