/**
 * 
 */
package com.ashling.riscfree.globalvariable.view.datamodel;

import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * @author vinod
 *
 */
public class GlobalVariableDMContext extends AbstractDMContext
		implements IGlobalVariableDMContext, IVariableDMContext, Comparable<IGlobalVariableDMContext> {

	private GlobalVariableDMNode globalVariableData;

	public GlobalVariableDMContext(DsfSession session, IDMContext[] parents, GlobalVariableDMNode globalVariableData) {
		super(session, parents);
		this.globalVariableData = globalVariableData;
	}

	@Override
	public int compareTo(IGlobalVariableDMContext o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		return globalVariableData.getName();
	}

	@Override
	public String getRelativeFilePath() {
		return globalVariableData.getFileName();
	}

	@Override
	public String getAbsoluteFilePath() {
		return globalVariableData.getFullname();
	}

	@Override
	public int getLineNumber() {
		return globalVariableData.getLine();
	}

	@Override
	public String getType() {
		return globalVariableData.getType();
	}

	@Override
	public String getDescription() {
		return globalVariableData.getDescription();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GlobalVariableDMContext) {
			GlobalVariableDMContext context = (GlobalVariableDMContext) obj;
			return baseEquals(context) && globalVariableData.equals(context.globalVariableData);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return baseHashCode() + globalVariableData.hashCode();
	}

	@Override
	public String toString() {
		return "[" + getSessionId() + ", " + getName() + ", " + getRelativeFilePath() + ", " + getLineNumber() + ", \""
				+ getDescription() + "\"" + "]";
	}
}
