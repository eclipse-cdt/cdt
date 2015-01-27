package org.eclipse.remote.internal.ui.views;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.internal.ui.Messages;
import org.eclipse.remote.internal.ui.RemoteUIPlugin;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * @since 2.0 API tooling made me put this in even though it's an internal package...
 */
public class NewRemoteConnectionTypePage extends WizardPage {

	private Table table;
	private IRemoteUIConnectionWizard nextWizard;

	public NewRemoteConnectionTypePage() {
		super("NewLaunchTargetTypePage"); //$NON-NLS-1$
		setTitle(Messages.NewRemoteConnectionTypePage_LaunchTargetType);
		setDescription(Messages.NewRemoteConnectionTypePage_SelectTargetType);
	}

	public IRemoteUIConnectionWizard getNextWizard() {
		return nextWizard;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());

		table = new Table(comp, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(data);

		setPageComplete(false);

		IRemoteServicesManager remoteManager = RemoteUIPlugin.getService(IRemoteServicesManager.class);
		for (IRemoteConnectionType connectionType : remoteManager.getAllConnectionTypes()) {
			if ((connectionType.getCapabilities() & IRemoteConnectionType.CAPABILITY_ADD_CONNECTIONS) == 0)
				continue;

			IRemoteUIConnectionService connManager = connectionType.getService(IRemoteUIConnectionService.class);
			if (connManager == null)
				continue;

			IRemoteUIConnectionWizard wizard = connManager.getConnectionWizard(parent.getShell());
			if (wizard == null)
				continue;

			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(connectionType.getName());
			item.setData(wizard);
			// TODO connection type icons somehow
//			Image icon = ui.getIcon();
//			if (icon != null) {
//				item.setImage(icon);
//			}

			// TODO select the last selected entry
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
		nextWizard = (IRemoteUIConnectionWizard)table.getSelection()[0].getData();
		if (nextWizard != null) {
			nextWizard.addPages();
			IWizardPage [] pages = nextWizard.getPages();
			if (pages.length > 0) {
				return pages[0];
			}
		}

		return super.getNextPage();
	}

}
