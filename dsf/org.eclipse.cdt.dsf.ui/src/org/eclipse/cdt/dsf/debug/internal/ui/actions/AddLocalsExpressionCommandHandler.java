package org.eclipse.cdt.dsf.debug.internal.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;

public class AddLocalsExpressionCommandHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
		IExpression[] expressions = expressionManager.getExpressions();
		String exprText = getExpression();
		for (IExpression old : expressions) {
			if (old.getExpressionText().equals(exprText))
				return null; // exists already
		}
		expressionManager.addExpression(expressionManager.newWatchExpression(exprText));
		return null;
	}

	protected String getExpression() {
		return "*"; //$NON-NLS-1$
	}
}
