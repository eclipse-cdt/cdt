package org.eclipse.cdt.debug.dap.gdbjtag;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.dap.DapLaunchDelegate;
import org.eclipse.cdt.debug.gdbjtag.core.Activator;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConnection;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContribution;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContributionFactory;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice;
import org.eclipse.cdt.launch.LaunchUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

public class DapGdbJtagLaunchDelegate extends DapLaunchDelegate {

	// Fields as described in https://github.com/eclipse-cdt/cdt-gdb-adapter/blob/eccf6bbb091aedd855adf0eaa1b28d341d8405d5/src/GDBTargetDebugSession.ts#L46

	// target field and sub-fields
	public static final String TARGET = "target"; //$NON-NLS-1$
	public static final String TYPE = "type"; //$NON-NLS-1$
	public static final String PARAMETERS = "parameters"; //$NON-NLS-1$
	public static final String HOST = "host"; //$NON-NLS-1$
	public static final String PORT = "port"; //$NON-NLS-1$
	public static final String CONNECT_COMMANDS = "connectCommands"; //$NON-NLS-1$
	// imageAndSymbols field and sub-fields
	public static final String IMAGE_AND_SYMBOLS = "imageAndSymbols"; //$NON-NLS-1$
	public static final String SYMBOL_FILE_NAME = "symbolFileName"; //$NON-NLS-1$
	public static final String SYMBOL_OFFSET = "symbolOffset"; //$NON-NLS-1$
	public static final String IMAGE_FILE_NAME = "imageFileName"; //$NON-NLS-1$
	public static final String IMAGE_OFFSET = "imageOffset"; //$NON-NLS-1$
	// preRunCommands field
	public static final String PRE_RUN_COMMANDS = "preRunCommands"; //$NON-NLS-1$

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor,
			boolean targetDebugAdapter, Map<String, Object> params) throws CoreException {
		params.put("request", "attach"); //$NON-NLS-1$//$NON-NLS-2$

		HashMap<String, Object> target = new HashMap<>();
		params.put(TARGET, target);

		Map<String, Object> attributes = configuration.getAttributes();
		IGDBJtagDevice jtagDevice = getJtagDevice(attributes);
		addTargetCommands(target, jtagDevice, attributes);
		addInitCommands(params, jtagDevice, attributes);
		addImageAndSymbols(params, jtagDevice, configuration, attributes);
		addPreRunCommands(params, jtagDevice, attributes);

		super.launch(configuration, mode, launch, monitor, true, params);
	}

	private void addImageAndSymbols(Map<String, Object> params, IGDBJtagDevice jtagDevice,
			ILaunchConfiguration configuration, Map<String, Object> attributes) throws CoreException {
		HashMap<String, Object> imageAndSymbols = null;
		try {
			if (CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_LOAD_SYMBOLS,
					IGDBJtagConstants.DEFAULT_LOAD_SYMBOLS)) {
				String symbolsFileName = null;

				// New setting in Helios. Default is true. Check for existence
				// in order to support older launch configs
				if (attributes.containsKey(IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_SYMBOLS)
						&& CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_SYMBOLS,
								IGDBJtagConstants.DEFAULT_USE_PROJ_BINARY_FOR_SYMBOLS)) {
					String programFile = LaunchUtils.getProgramPath(configuration);
					if (programFile != null) {
						symbolsFileName = programFile;
					}
				} else {
					symbolsFileName = CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_SYMBOLS_FILE_NAME,
							IGDBJtagConstants.DEFAULT_SYMBOLS_FILE_NAME);
					if (symbolsFileName.length() > 0) {
						symbolsFileName = VariablesPlugin.getDefault().getStringVariableManager()
								.performStringSubstitution(symbolsFileName);
					} else {
						symbolsFileName = null;
					}
				}

				if (symbolsFileName == null) {
					throw newCoreException("Symbolics loading was requested but file was not specified or not found.");
				}

				String symbolsOffset = CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_SYMBOLS_OFFSET,
						IGDBJtagConstants.DEFAULT_SYMBOLS_OFFSET);
				if (symbolsOffset != null && symbolsOffset.length() > 0) {
					symbolsOffset = "0x" + symbolsOffset;
				}
				List<String> commands = new ArrayList<>();
				jtagDevice.doLoadSymbol(symbolsFileName, symbolsOffset, commands);

				// The DAP implementation does not support arbitrary arguments for setting symbols, so
				// check if a custom doLoadSymbol is provided in the JTAG Device info
				DefaultGDBJtagDeviceImpl defaultGDBJtagDeviceImpl = new DefaultGDBJtagDeviceImpl();
				List<String> defaultCommands = new ArrayList<>();
				defaultGDBJtagDeviceImpl.doLoadSymbol(symbolsFileName, symbolsOffset, defaultCommands);
				if (commands.equals(defaultCommands)) {
					imageAndSymbols = new HashMap<>();
					imageAndSymbols.put(SYMBOL_FILE_NAME, symbolsFileName);
					if (symbolsOffset != null && symbolsOffset.length() > 0) {
						imageAndSymbols.put(SYMBOL_OFFSET, symbolsOffset);
					}
				} else {
					throw newCoreException(String.format(
							"Device '%s' has non-standard method for loading symbol table which is not supported by the debug adapter.",
							getGDBJtagDeviceName(attributes)));
				}
			}
		} catch (CoreException e) {
			throw newCoreException("Cannot load symbol", e);
		}

		try {
			String imageFileName = null;
			if (CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_LOAD_IMAGE,
					IGDBJtagConstants.DEFAULT_LOAD_IMAGE)) {
				// New setting in Helios. Default is true. Check for existence
				// in order to support older launch configs
				if (attributes.containsKey(IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_IMAGE)
						&& CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_IMAGE,
								IGDBJtagConstants.DEFAULT_USE_PROJ_BINARY_FOR_IMAGE)) {
					String programFile = LaunchUtils.getProgramPath(configuration);
					if (programFile != null) {
						imageFileName = programFile;
					}
				} else {
					imageFileName = CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_IMAGE_FILE_NAME,
							IGDBJtagConstants.DEFAULT_IMAGE_FILE_NAME);
					if (imageFileName.length() > 0) {
						imageFileName = VariablesPlugin.getDefault().getStringVariableManager()
								.performStringSubstitution(imageFileName);
					} else {
						imageFileName = null;
					}
				}

				if (imageFileName == null) {
					throw newCoreException("Image loading was requested but file was not specified or not found.");

				}

				String imageOffset = CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_IMAGE_OFFSET,
						IGDBJtagConstants.DEFAULT_IMAGE_OFFSET);
				if (imageOffset != null && imageOffset.length() > 0) {
					imageOffset = "0x" + CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_IMAGE_OFFSET,
							IGDBJtagConstants.DEFAULT_IMAGE_OFFSET);
				}
				List<String> commands = new ArrayList<>();
				jtagDevice.doLoadImage(imageFileName, imageOffset, commands);

				// The DAP implementation does not support arbitrary arguments for loading images, so
				// check if a custom doLoadImage is provided in the JTAG Device info
				DefaultGDBJtagDeviceImpl defaultGDBJtagDeviceImpl = new DefaultGDBJtagDeviceImpl();
				List<String> defaultCommands = new ArrayList<>();
				defaultGDBJtagDeviceImpl.doLoadImage(imageFileName, imageOffset, defaultCommands);
				if (commands.equals(defaultCommands)) {
					if (imageAndSymbols == null) {
						imageAndSymbols = new HashMap<>();
					}
					imageAndSymbols.put(IMAGE_FILE_NAME, imageFileName);
					if (imageOffset != null && imageOffset.length() > 0) {
						imageAndSymbols.put(IMAGE_OFFSET, imageOffset);
					}
				} else {
					throw newCoreException(String.format(
							"Device '%s' has non-standard method for loading image which is not supported by the debug adapter.",
							getGDBJtagDeviceName(attributes)));
				}
			}
		} catch (CoreException e) {
			throw newCoreException("Cannot load image", e);
		}

		if (imageAndSymbols != null) {
			params.put(IMAGE_AND_SYMBOLS, imageAndSymbols);
		}
	}

	private void addTargetCommands(HashMap<String, Object> target, IGDBJtagDevice jtagDevice,
			Map<String, Object> attributes) throws CoreException {
		if (CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_USE_REMOTE_TARGET,
				IGDBJtagConstants.DEFAULT_USE_REMOTE_TARGET)) {
			List<String> commands = new ArrayList<>();
			if (jtagDevice instanceof IGDBJtagConnection) {
				URI uri;
				try {
					uri = new URI(CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_CONNECTION,
							IGDBJtagConstants.DEFAULT_CONNECTION));
				} catch (URISyntaxException e) {
					throw newCoreException("Invalid remote target connection syntax", e);
				}

				IGDBJtagConnection device = (IGDBJtagConnection) jtagDevice;
				device.doRemote(uri.getSchemeSpecificPart(), commands);
			} else {
				// Handle legacy network device contributions that don't understand URIs
				String ipAddress = CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_IP_ADDRESS,
						IGDBJtagConstants.DEFAULT_IP_ADDRESS);
				int portNumber = CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_PORT_NUMBER,
						IGDBJtagConstants.DEFAULT_PORT_NUMBER);
				jtagDevice.doRemote(ipAddress, portNumber, commands);
			}
			target.put(CONNECT_COMMANDS, commands);
		}
	}

	private void addInitCommands(Map<String, Object> params, IGDBJtagDevice jtagDevice, Map<String, Object> attributes)
			throws CoreException {

		List<String> commands = new ArrayList<>();
		if (CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_DO_RESET, IGDBJtagConstants.DEFAULT_DO_RESET)) {
			int size = commands.size();
			jtagDevice.doReset(commands);
			if (size == commands.size()) {
				throw newCoreException(
						String.format("Reset command not defined for device '%s'", getGDBJtagDeviceName(attributes)));
			}
		}
		if (CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_DO_RESET, IGDBJtagConstants.DEFAULT_DO_RESET)) {
			int defaultDelay = jtagDevice.getDefaultDelay();
			int delay = CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_DELAY, defaultDelay);
			int size = commands.size();
			jtagDevice.doDelay(delay, commands);
			if (size == commands.size() && (delay != 0)) {
				throw newCoreException(
						String.format("Delay command not defined for device '%s'", getGDBJtagDeviceName(attributes)));
			}
		}
		if (CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_DO_HALT, IGDBJtagConstants.DEFAULT_DO_HALT)) {
			int size = commands.size();
			jtagDevice.doHalt(commands);
			if (size == commands.size()) {
				throw newCoreException(
						String.format("Halt command not defined for device '%s'", getGDBJtagDeviceName(attributes)));
			}
		}

		String userCmd = CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_INIT_COMMANDS,
				IGDBJtagConstants.DEFAULT_INIT_COMMANDS);
		try {
			userCmd = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(userCmd);
		} catch (CoreException e) {
			throw newCoreException(
					String.format("Cannot run user defined init commands", getGDBJtagDeviceName(attributes)));
		}
		if (userCmd.length() > 0) {
			String[] userCommandsSplit = userCmd.split("\\r?\\n"); //$NON-NLS-1$
			commands.addAll(Arrays.asList(userCommandsSplit));
		}

		if (!commands.isEmpty()) {
			params.put(DapLaunchDelegate.INIT_COMMANDS, commands);
		}
	}

	private void addPreRunCommands(Map<String, Object> params, IGDBJtagDevice jtagDevice,
			Map<String, Object> attributes) throws CoreException {
		List<String> commands = new ArrayList<>();
		if (CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_SET_PC_REGISTER,
				IGDBJtagConstants.DEFAULT_SET_PC_REGISTER)) {
			String pcRegister = CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_PC_REGISTER,
					CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_IMAGE_OFFSET,
							IGDBJtagConstants.DEFAULT_PC_REGISTER));
			jtagDevice.doSetPC(pcRegister, commands);
		}

		if (CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_SET_STOP_AT,
				IGDBJtagConstants.DEFAULT_SET_STOP_AT)) {
			String stopAt = CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_STOP_AT,
					IGDBJtagConstants.DEFAULT_STOP_AT);
			jtagDevice.doStopAt(stopAt, commands);
		}

		try {
			String userCmd = CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_RUN_COMMANDS,
					IGDBJtagConstants.DEFAULT_RUN_COMMANDS);
			if (userCmd.length() > 0) {
				userCmd = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(userCmd);
				String[] userCommandsSplit = userCmd.split("\\r?\\n"); //$NON-NLS-1$
				commands.addAll(Arrays.asList(userCommandsSplit));
			}
		} catch (CoreException e) {
			throw newCoreException(
					String.format("Cannot run user defined run commands", getGDBJtagDeviceName(attributes)));
		}

		if (!commands.isEmpty()) {
			params.put(DapLaunchDelegate.INIT_COMMANDS, commands);
		}
	}

	private IGDBJtagDevice getJtagDevice(Map<String, Object> attributes) throws CoreException {
		GDBJtagDeviceContribution deviceContribution = getGDBJtagDeviceContribution(attributes);
		if (deviceContribution == null) {
			throw newCoreException("Cannot get Jtag device information", null);
		}
		IGDBJtagDevice device = deviceContribution.getDevice();
		if (device == null) {
			throw newCoreException("Cannot get Jtag device instance", null);
		}
		return device;
	}

	private String getGDBJtagDeviceName(Map<String, Object> attributes) throws CoreException {
		GDBJtagDeviceContribution contribution = getGDBJtagDeviceContribution(attributes);
		if (contribution != null) {
			return contribution.getDeviceName();
		}
		return IGDBJtagConstants.DEFAULT_JTAG_DEVICE_NAME;
	}

	@SuppressWarnings("deprecation")
	private GDBJtagDeviceContribution getGDBJtagDeviceContribution(Map<String, Object> attributes)
			throws CoreException {
		if (attributes.containsKey(IGDBJtagConstants.ATTR_JTAG_DEVICE_ID)) {
			String deviceId = CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_JTAG_DEVICE_ID, "");
			if (!deviceId.isEmpty()) {
				return GDBJtagDeviceContributionFactory.getInstance().findByDeviceId(deviceId);
			}
		}

		// Fall back to old behavior with name only if ID is missing
		if (attributes.containsKey(IGDBJtagConstants.ATTR_JTAG_DEVICE)) {
			String deviceName = CDebugUtils.getAttribute(attributes, IGDBJtagConstants.ATTR_JTAG_DEVICE, "");
			if (!deviceName.isEmpty()) {
				return GDBJtagDeviceContributionFactory.getInstance().findByDeviceName(deviceName);
			}
		}

		// No matching device contribution found
		return null;
	}

	private CoreException newCoreException(String message, Throwable e) {
		return new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, message, e));
	}

	private CoreException newCoreException(String message) {
		return newCoreException(message, null);
	}

}
