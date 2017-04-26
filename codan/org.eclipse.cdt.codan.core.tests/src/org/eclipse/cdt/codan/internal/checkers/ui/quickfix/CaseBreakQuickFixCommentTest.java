package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;

public class CaseBreakQuickFixCommentTest extends QuickFixTestCase {
	@SuppressWarnings("restriction")
	@Override
	protected AbstractCodanCMarkerResolution createQuickFix() {
		return new CaseBreakQuickFixComment();
	}

	//void hello() {}
	//void func() {
	//	int a;
	//	switch(a) {
	//	case 1:
	//		hello();
	//	case 2:
	//		break;
	//	}
	//}
	public void testSimpleCase_515814() throws Exception {
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("/* no break */\tcase 2:", result);
	}

	//void hello() {}
	//void func() {
	//	int a;
	//	switch(a) {
	//	case 1: {
	//		hello();
	//	}
	//	case 2:
	//		break;
	//	}
	//}
	public void testCompositeCase_515814() throws Exception {
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("/* no break */\tcase 2:", result);
	}
}
