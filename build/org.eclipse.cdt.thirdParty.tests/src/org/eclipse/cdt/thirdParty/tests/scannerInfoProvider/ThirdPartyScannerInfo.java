package org.eclipse.cdt.thirdParty.tests.scannerInfoProvider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.parser.IExtendedScannerInfo;

public class ThirdPartyScannerInfo implements IExtendedScannerInfo {

	@Override
	public Map<String, String> getDefinedSymbols() {
		Map<String, String> symbols = new HashMap<String, String>();
		symbols.put("X", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("Y", "2"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("Z", "3"); //$NON-NLS-1$ //$NON-NLS-2$
		return symbols;
	}

	@Override
	public String[] getIncludePaths() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getMacroFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getIncludeFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getLocalIncludePath() {
		// TODO Auto-generated method stub
		return null;
	}

}
