package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;

@SuppressWarnings("nls")
public class CaseBreakQuickFixTest extends QuickFixTestCase {
	@SuppressWarnings("restriction")
	@Override
	protected AbstractCodanCMarkerResolution createQuickFix() {
		return new CaseBreakQuickFixBreak();
	}

	// void func() {
	//	 int a;
	//   switch(a) {
	//	   case 1:
	//	     hello();
	//     case 2:
	//	 }
	// }
	public void testMiddleCase() {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("break;     case 2:", result);
	}

	// void func() {
	//	 int a;
	//   switch(a) {
	//	   case 1:
	//	     hello();
	//	 }
	// }
	public void testLastCase() {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("break;	 }", result);
	}
	// void func() {
	//	 int a;
	//   switch(a) {
	//	   case 1: {
	//	     hello();
	//     }
	//	 }
	// }
	public void testLastCaseComp() {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("hello();    break;", result);
	}
}
