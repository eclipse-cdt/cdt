package org.eclipse.cdt.thirdParty.tests.scannerInfoProvider;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IResource;

/**
 * Sample ScannerInfoProvider for Bug 398056
 */
public class ThirdPartyScannerInfoProvider extends AbstractCExtension implements IScannerInfoProvider {

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		return new ThirdPartyScannerInfo();
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {

	}
}
