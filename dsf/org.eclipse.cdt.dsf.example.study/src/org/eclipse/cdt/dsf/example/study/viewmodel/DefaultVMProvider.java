package org.eclipse.cdt.dsf.example.study.viewmodel;

import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

public class DefaultVMProvider extends AbstractDMVMProvider {

	public DefaultVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext,
			DsfSession session) {
		super(adapter, presentationContext, session);
	}

}
