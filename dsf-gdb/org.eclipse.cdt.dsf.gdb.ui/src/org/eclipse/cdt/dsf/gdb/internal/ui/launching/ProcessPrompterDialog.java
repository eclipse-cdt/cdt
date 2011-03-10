package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

public class ProcessPrompterDialog extends TwoPaneElementSelector {
	private static final int NEW_BUTTON_ID = 9876;
	private String fBinaryPath;
	private boolean fSupportsNewProcess;

	public ProcessPrompterDialog(Shell parent, ILabelProvider elementRenderer,
			ILabelProvider qualifierRenderer, boolean supportsNewProcess) {
		super(parent, elementRenderer, qualifierRenderer);
		fSupportsNewProcess = supportsNewProcess;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button newButton = createButton(
				parent, NEW_BUTTON_ID, LaunchUIMessages.getString("ProcessPrompterDialog.New"), false); //$NON-NLS-1$
		newButton.setEnabled(fSupportsNewProcess);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == NEW_BUTTON_ID) {
			FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
			fBinaryPath = fd.open();

			setReturnCode(OK);
			close();
		}
		super.buttonPressed(buttonId);
	}
	
	public String getBinaryPath() {
		return fBinaryPath;
	}
	
}
