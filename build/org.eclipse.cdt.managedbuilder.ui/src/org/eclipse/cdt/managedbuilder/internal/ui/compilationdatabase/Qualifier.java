package org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase;

import java.util.function.Supplier;

public final class Qualifier implements Supplier<String> {

	@Override
	public String get() {
		return "org.eclipse.cdt.managedbuilder.ui"; //$NON-NLS-1$
	}

}
