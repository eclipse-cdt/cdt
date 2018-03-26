package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.jface.text.IRegion;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public abstract class SelectedExpressionFinder extends ASTVisitor {
	private IASTExpression selectedExpression;
	private IRegion selectedRegion;

	public SelectedExpressionFinder(IRegion selectedRegion) {
		this.selectedRegion = selectedRegion;
	}

	{
		shouldVisitExpressions = true;
	}

	protected abstract boolean isSearchedType(IASTExpression expression);

	@Override
	public int visit(IASTExpression expression) {
		if (SelectionHelper.nodeMatchesSelection(expression, selectedRegion)) {
			selectedExpression = expression;
			return PROCESS_ABORT;
		} else if (isSearchedType(expression) &&
				SelectionHelper.isSelectionInsideNode(expression, selectedRegion)) {
			selectedExpression = expression;
			return PROCESS_ABORT;
		}
		return super.visit(expression);
	}

	public IASTExpression findSelectedExpression(IASTTranslationUnit ast) {
		ast.accept(this);
		return selectedExpression;
	}
}