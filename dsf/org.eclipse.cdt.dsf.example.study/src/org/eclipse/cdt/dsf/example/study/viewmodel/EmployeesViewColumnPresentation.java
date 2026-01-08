package org.eclipse.cdt.dsf.example.study.viewmodel;

import org.eclipse.cdt.dsf.example.study.internal.CdtDsfStudyPluginActivator;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 *
 */
@SuppressWarnings("restriction")
public class EmployeesViewColumnPresentation implements IColumnPresentation {

	public static final String ID = CdtDsfStudyPluginActivator.PLUGIN_ID + ".TIMER_COLUMN_PRESENTATION_ID"; //$NON-NLS-1$
	public static final String COL_ID = ID + ".COL_ID"; //$NON-NLS-1$
	public static final String COL_VALUE = ID + ".COL_VALUE"; //$NON-NLS-1$

	@Override
	public void init(IPresentationContext context) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public String[] getAvailableColumns() {
		return new String[] { COL_ID, COL_VALUE };
	}

	@Override
	public String getHeader(String id) {
		if (COL_ID.equals(id)) {
			return "ID"; //$NON-NLS-1$
		} else if (COL_VALUE.equals(id)) {
			return "Value"; //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ImageDescriptor getImageDescriptor(String id) {
		return null;
	}

	@Override
	public String[] getInitialColumns() {
		return getAvailableColumns();
	}

	@Override
	public boolean isOptional() {
		return true;
	}

}
