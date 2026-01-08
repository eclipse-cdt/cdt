package org.eclipse.cdt.dsf.example.study.datamodel;

import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Data Model context representing a Employee Context
 *
 * The timers are represented using a Data Model context object, which implements IDMContext
 */
public class EmployeeDMContext extends AbstractDMContext {
	private final int fId;

	public EmployeeDMContext(DsfSession session, int fId) {
		super(session.getId(), new IDMContext[0]);
		this.fId = fId;
	}

	public int getTimerNumberId() {
		return fId;
	}

	// EmployeeDMContext objects are created as needed and not cached, so the
	// equals method implementation is critical.
	// use LinkedHashMap
	@Override
	public boolean equals(Object other) {
		return baseEquals(other) && ((EmployeeDMContext) other).fId == fId;
	}

	@Override
	public int hashCode() {
		return baseHashCode() + fId;
	}

	@Override
	public String toString() {
		return baseToString() + ".employee[" + fId + "]";
	}

}
