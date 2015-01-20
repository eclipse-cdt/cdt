package org.eclipse.cdt.dsf.debug.internal.ui.actions;


public class AddRegistersExpressionCommandHandler extends AddLocalsExpressionCommandHandler {
	@Override
	protected String getExpression() {
		return "$*"; //$NON-NLS-1$
	}
}
