package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.cdt.internal.ui.cview.IncludeRefContainer;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.actions.ActionDelegate;

public class IncludeGroupAction extends ActionDelegate {
	@Override
	public void runWithEvent(IAction action, Event event) {
		var id = action.getId();
		var store = CUIPlugin.getDefault().getPreferenceStore();
		var valstr = IncludeRefContainer.groupProp;

		if (id.equals("org.eclipse.cdt.make.ui.incgroup.list")) { //$NON-NLS-1$
			store.setValue(valstr, IncludeRefContainer.Representation.List.toInt());
		} else if (id.equals("org.eclipse.cdt.make.ui.incgroup.once")) { //$NON-NLS-1$
			store.setValue(valstr, IncludeRefContainer.Representation.Single.toInt());
		} else if (id.equals("org.eclipse.cdt.make.ui.incgroup.possible")) { //$NON-NLS-1$
			store.setValue(valstr, IncludeRefContainer.Representation.Compact.toInt());
		} else {
			assert (false);
		}

	}
}
