package org.eclipse.cdt.codan.ui.handlers;

import org.eclipse.cdt.codan.internal.ui.actions.RunCodeAnalysis;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command to run code analysis on selected resources
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class RunCodanCommand extends AbstractHandler {

	public RunCodanCommand() {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		RunCodeAnalysis action = new RunCodeAnalysis();
		action.selectionChanged(null, currentSelection);
		action.run(null);
		return null;
	}
}
