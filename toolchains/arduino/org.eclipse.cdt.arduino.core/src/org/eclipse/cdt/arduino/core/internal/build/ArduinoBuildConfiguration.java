package org.eclipse.cdt.arduino.core.internal.build;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoardManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPackage;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

public class ArduinoBuildConfiguration {

	private static final String PACKAGE_NAME = "packageId";
	private static final String PLATFORM_NAME = "platformName";
	private static final String BOARD_NAME = "boardName";

	private final IBuildConfiguration config;

	private ArduinoBuildConfiguration(IBuildConfiguration config) {
		this.config = config;
	}

	public static class Factory implements IAdapterFactory {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
			if (adapterType.equals(ArduinoBuildConfiguration.class) && adaptableObject instanceof IBuildConfiguration) {
				return (T) new ArduinoBuildConfiguration((IBuildConfiguration) adaptableObject);
			}
			return null;
		}

		@Override
		public Class<?>[] getAdapterList() {
			return new Class<?>[] { ArduinoBuildConfiguration.class };
		}
	}

	public IEclipsePreferences getSettings() {
		return (IEclipsePreferences) new ProjectScope(config.getProject()).getNode(Activator.getId()).node("config") //$NON-NLS-1$
				.node(config.getName());
	}

	public void setBoard(ArduinoBoard board) throws CoreException {
		ArduinoPlatform platform = board.getPlatform();
		ArduinoPackage pkg = platform.getPackage();

		IEclipsePreferences settings = getSettings();
		settings.put(PACKAGE_NAME, pkg.getName());
		settings.put(PLATFORM_NAME, platform.getName());
		settings.put(BOARD_NAME, board.getName());
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Saving preferences", e));
		}
	}

	public ArduinoBoard getBoard() throws CoreException {
		IEclipsePreferences settings = getSettings();
		String packageName = settings.get(PACKAGE_NAME, ""); //$NON-NLS-1$
		String platformName = settings.get(PLATFORM_NAME, ""); //$NON-NLS-1$
		String boardName = settings.get(BOARD_NAME, ""); //$NON-NLS-1$
		return ArduinoBoardManager.instance.getBoard(boardName, platformName, packageName);
	}

}
