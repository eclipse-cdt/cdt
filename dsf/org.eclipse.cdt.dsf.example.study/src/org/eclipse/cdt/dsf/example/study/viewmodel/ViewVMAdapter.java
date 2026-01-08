package org.eclipse.cdt.dsf.example.study.viewmodel;

import org.eclipse.cdt.dsf.example.study.ui.CdtDsfStudyView;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMAdapter;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * This is the adapter that implements the flexible hierarchy viewer interfaces for providing content, labels, and event processing for the viewer.
 *  This adapter is registered with the DSF Session object,
 *  and is returned by the IDMContext.getAdapter() and IVMContext.getAdapter() methods, which both call DsfSession.getModelAdapter(Class).
 */
public class ViewVMAdapter extends AbstractDMVMAdapter {

	public ViewVMAdapter(DsfSession session) {
		super(session);
	}

	// abstract method getVMProvider()
	@Override
	protected IVMProvider createViewModelProvider(IPresentationContext context) {
		// Navigate to the provider based on the context id
		if (CdtDsfStudyView.ID_STUDY_VIEW_EMPLOYEES.equals(context.getId())) { //$NON-NLS-1$
			return new EmployeesVMProvider(this, context, getSession());
		} else if (CdtDsfStudyView.ID_STUDY_VIEW_DEFAULT.equals(context.getId())) { //$NON-NLS-1$
			return new DefaultVMProvider(this, context, getSession());
		}
		return null;
	}

}
