package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.cdt.internal.ui.cview.IncludeRefContainer;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.internal.PluginAction;

public class IncludeGroupAction extends ActionDelegate {
	@Override
	public void runWithEvent(IAction action, Event event) {
		var id = action.getId();

		var pAction = (PluginAction) action;
		//TODO Is there a better way to get the selection without huge overhead?
		var selection = (IStructuredSelection) pAction.getSelection();

		IResource prj = null;

		for (var o : selection.toArray()) {
			if (o instanceof IncludeRefContainer) {
				prj = ((IncludeRefContainer) o).getCProject().getResource();
			}
		}

		assert prj != null;
		if (prj == null)
			return;

		var valstr = IncludeRefContainer.qname;

		try {
			if (id.equals("org.eclipse.cdt.make.ui.incgroup.list")) { //$NON-NLS-1$
				prj.setPersistentProperty(valstr, IncludeRefContainer.Representation.List.toString());
			} else if (id.equals("org.eclipse.cdt.make.ui.incgroup.single")) { //$NON-NLS-1$
				prj.setPersistentProperty(valstr, IncludeRefContainer.Representation.Single.toString());
			} else if (id.equals("org.eclipse.cdt.make.ui.incgroup.compact")) { //$NON-NLS-1$
				prj.setPersistentProperty(valstr, IncludeRefContainer.Representation.Compact.toString());
			} else if (id.equals("org.eclipse.cdt.make.ui.incgroup.smart")) { //$NON-NLS-1$
				prj.setPersistentProperty(valstr, IncludeRefContainer.Representation.Smart.toString());
			} else {
				assert (false);
			}

			// TODO: Project-Explorer is not updated
			// As it's not possible to listen to a PersistantProperty, update must be done here. :(
			IWorkbenchPartReference refs[] = CUIPlugin.getActiveWorkbenchWindow().getActivePage().getViewReferences();
			for (IWorkbenchPartReference ref : refs) {
				IWorkbenchPart part = ref.getPart(false);
				if (part != null && part instanceof IPropertyChangeListener)
					((IPropertyChangeListener) part).propertyChange(
							new PropertyChangeEvent(event, PreferenceConstants.PREF_SHOW_CU_CHILDREN, null, null));

			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}
}
