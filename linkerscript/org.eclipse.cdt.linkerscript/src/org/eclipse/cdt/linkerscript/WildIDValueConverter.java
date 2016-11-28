package org.eclipse.cdt.linkerscript;

public class WildIDValueConverter extends IDValueConverter {
	@Override
	protected boolean idNeedsQuoting(String id) {
		if ("*".equals(id)) {
			return false;
		}

		return super.idNeedsQuoting(id);
	}
}