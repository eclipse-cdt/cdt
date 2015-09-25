package org.eclipse.cdt.arduino.core.internal.build;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPackage;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoTool;
import org.eclipse.cdt.core.build.CToolChain;
import org.eclipse.cdt.core.build.IToolChainFactory;
import org.eclipse.cdt.core.build.gcc.GCCToolChain;
import org.osgi.service.prefs.Preferences;

public class ArduinoGCCToolChain extends GCCToolChain {

	private static final String PACKAGE = "arduinoPackage"; //$NON-NLS-1$
	private static final String TOOL = "arduinoTool"; //$NON-NLS-1$

	private final ArduinoTool tool;

	public ArduinoGCCToolChain(String id, Preferences settings) {
		super(id, settings);
		ArduinoPackage pkg = ArduinoManager.instance.getPackage(settings.get(PACKAGE, "")); //$NON-NLS-1$
		if (pkg != null) {
			this.tool = pkg.getLatestTool(settings.get(TOOL, "")); //$NON-NLS-1$
		} else {
			// TODO where did it go?
			this.tool = null;
		}
	}

	public ArduinoGCCToolChain(ArduinoTool tool) {
		super(tool.getName());
		this.tool = tool;
	}

	public static class ArduinoFactory implements IToolChainFactory {
		@Override
		public CToolChain createToolChain(String id, Preferences settings) {
			return new ArduinoGCCToolChain(id, settings);
		}
	}

	@Override
	public String getFamily() {
		return "Arduino GCC"; //$NON-NLS-1$
	}

	public ArduinoTool getTool() {
		return tool;
	}

	@Override
	public void save(Preferences settings) {
		super.save(settings);
		settings.put(TOOL, tool.getName());
		settings.put(PACKAGE, tool.getPackage().getName());
	}

}
