package org.eclipse.cdt.linkerscript.ui.form;

import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.cdt.linkerscript.linkerScript.Memory;
import org.eclipse.jface.viewers.ColumnLabelProvider;

public class MemoryLabelProvider extends ColumnLabelProvider {
	private final Supplier<ILinkerScriptModel> modelSupplier;
	private final Function<Memory, String> op;

	public MemoryLabelProvider(Supplier<ILinkerScriptModel> modelSupplier, Function<Memory, String> op) {
		this.modelSupplier = modelSupplier;
		this.op = op;
	}

	@Override
	public String getText(Object element) {
		return modelSupplier.get().readModel(element, Memory.class, "", op);
	}
}