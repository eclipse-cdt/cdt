package org.eclipse.cdt.make.ui.dialogs;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

public class BuildTargetDialog extends Dialog {
	
	public BuildTargetDialog(Shell shell, IContainer fContainer) {
		super(shell);		
	}

	public void setTarget(IMakeTarget target) {
		
	}

	public IMakeTarget getTarget() {
		return null;
	}

}
