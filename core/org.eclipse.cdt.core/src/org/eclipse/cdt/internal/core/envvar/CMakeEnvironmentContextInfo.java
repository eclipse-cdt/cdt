package org.eclipse.cdt.internal.core.envvar;

import java.util.ArrayList;
import java.util.Arrays;

public class CMakeEnvironmentContextInfo extends DefaultEnvironmentContextInfo {

	private IEnvironmentContextInfo fBaseInfo;
	private ICoreEnvironmentVariableSupplier fSuppliers[];

	public CMakeEnvironmentContextInfo(IEnvironmentContextInfo info) {
		super(info.getContext());
		fBaseInfo = info;
	}

	@Override
	public ICoreEnvironmentVariableSupplier[] getSuppliers() {
		if (fSuppliers == null) {
			ArrayList<ICoreEnvironmentVariableSupplier> suppliers = new ArrayList<>(
					Arrays.asList(fBaseInfo.getSuppliers()));
			suppliers.add(EnvironmentVariableManager.fCmakeSupplier);
			fSuppliers = suppliers.toArray(new ICoreEnvironmentVariableSupplier[0]);
		}
		return fSuppliers;
	}

	@Override
	public IEnvironmentContextInfo getNext() {
		IEnvironmentContextInfo baseNext = fBaseInfo.getNext();
		if (baseNext != null)
			return new CMakeEnvironmentContextInfo(baseNext);
		return null;
	}
}
