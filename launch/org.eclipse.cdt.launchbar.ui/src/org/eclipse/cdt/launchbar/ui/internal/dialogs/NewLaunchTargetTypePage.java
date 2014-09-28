package org.eclipse.cdt.launchbar.ui.internal.dialogs;

import java.util.Map.Entry;

import org.eclipse.cdt.launchbar.core.ILaunchTargetType;
import org.eclipse.cdt.launchbar.core.internal.Activator;
import org.eclipse.cdt.launchbar.core.internal.ExecutableExtension;
import org.eclipse.cdt.launchbar.ui.internal.LaunchBarUIManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;

public class NewLaunchTargetTypePage extends WizardPage {

	private final LaunchBarUIManager uiManager;
	private Table table;
	private ExecutableExtension<INewWizard> currentExtension;
	private INewWizard nextWizard;

	public NewLaunchTargetTypePage(LaunchBarUIManager uiManager) {
		super("NewLaunchTargetTypePage");
		setTitle("Launch Target Type");
		setDescription("Select type of launch target to create.");
		this.uiManager = uiManager;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());

		table = new Table(comp, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(data);

		setPageComplete(false);
		for (Entry<ILaunchTargetType, ExecutableExtension<INewWizard>> entry : uiManager.getNewTargetWizards().entrySet()) {
			TableItem item = new TableItem(table, SWT.NONE);
			ILaunchTargetType targetType = entry.getKey();
			item.setText(uiManager.getTargetTypeName(targetType));
			Image icon = uiManager.getTargetTypeIcon(targetType);
			if (icon != null) {
				item.setImage(icon);
			}
			item.setData(entry.getValue());
			table.select(0);
			setPageComplete(true);
		}

		setControl(comp);
	}

	@Override
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}

	@Override
	public IWizardPage getNextPage() {
		@SuppressWarnings("unchecked")
		ExecutableExtension<INewWizard> extension = (ExecutableExtension<INewWizard>) table.getSelection()[0].getData();
		if (extension != currentExtension) {
			try {
				nextWizard = extension.create();
				nextWizard.init(PlatformUI.getWorkbench(), null);
				nextWizard.addPages();
				currentExtension = extension;
			} catch (CoreException e) {
				Activator.log(e.getStatus());
			}
		}

		if (nextWizard != null) {
			IWizardPage [] pages = nextWizard.getPages();
			if (pages.length > 0) {
				return pages[0];
			}
		}

		return super.getNextPage();
	}

}
