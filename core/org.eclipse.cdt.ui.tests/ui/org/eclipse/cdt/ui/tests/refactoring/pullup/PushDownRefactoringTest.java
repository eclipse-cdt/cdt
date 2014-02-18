package org.eclipse.cdt.ui.tests.refactoring.pullup;

import org.eclipse.ltk.core.refactoring.Refactoring;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.pullup.PushDownInformation;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PushDownRefactoring;

public class PushDownRefactoringTest extends RefactoringTestBase {

	private PushDownRefactoring refactoring;
	
	
	@Override
	protected Refactoring createRefactoring() {
		this.refactoring = new PushDownRefactoring(this.getSelectedTranslationUnit(), 
				this.getSelection(), this.getCProject());
		return this.refactoring;
	}

	
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}
	
	
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	
	@Override
	protected void simulateUserInput() {
		final PushDownInformation infos = this.refactoring.getInformation();
	}
}
