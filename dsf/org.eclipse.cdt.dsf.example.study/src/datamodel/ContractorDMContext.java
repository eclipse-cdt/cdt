package datamodel;

import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Data Model context representing a Contractor Context.
 */
public class ContractorDMContext extends AbstractDMContext {

	public ContractorDMContext(DsfSession session, IDMContext[] parents) {
		super(session, parents);
	}

	@Override
	public boolean equals(Object obj) {
		return false;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
