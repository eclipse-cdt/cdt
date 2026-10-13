package org.eclipse.launchbar.core.internal.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.ILaunchBarListener;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.internal.LaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * Test Launch bar object for collecting event handling information
 */
public class TestLaunchBarListener implements ILaunchBarListener {

	public static final String attr_activeDesc = "attr_activeDesc";//$NON-NLS-1$
	public static final String attr_activeTarget = "attr_activeTarget";//$NON-NLS-1$
	public static final String attr_activeMode = "attr_activeMode";//$NON-NLS-1$

	private LaunchBarManager launchBarManager = null;

	private List<Map<String, Object>> descChangedRecord = new ArrayList<>();
	private List<Map<String, Object>> targetChangedRecord = new ArrayList<>();
	private List<Map<String, Object>> modeChangedRecord = new ArrayList<>();

	public TestLaunchBarListener(LaunchBarManager launchBarManager) {
		this.launchBarManager = launchBarManager;
	}

	public List<Map<String, Object>> getDescriptorChangedRecords() {
		return List.copyOf(descChangedRecord);
	}

	public List<Map<String, Object>> getTargetChangedRecords() {
		return List.copyOf(targetChangedRecord);
	}

	public List<Map<String, Object>> getModeChangedRecords() {
		return List.copyOf(modeChangedRecord);
	}

	@Override
	public void activeLaunchDescriptorChanged(ILaunchDescriptor descriptor) {
		Map<String, Object> state = new HashMap<>();
		state.put(attr_activeDesc, descriptor);
		state.put(attr_activeTarget, launchBarManager.getActiveLaunchTarget());
		state.put(attr_activeMode, launchBarManager.getActiveLaunchMode());
		descChangedRecord.add(state);
	}

	@Override
	public void activeLaunchModeChanged(ILaunchMode mode) {
		Map<String, Object> state = new HashMap<>();
		state.put(attr_activeDesc, launchBarManager.getActiveLaunchDescriptor());
		state.put(attr_activeTarget, launchBarManager.getActiveLaunchTarget());
		state.put(attr_activeMode, mode);
		modeChangedRecord.add(state);
	}

	@Override
	public void activeLaunchTargetChanged(ILaunchTarget target) {
		Map<String, Object> state = new HashMap<>();
		state.put(attr_activeDesc, launchBarManager.getActiveLaunchDescriptor());
		state.put(attr_activeTarget, target);
		state.put(attr_activeMode, launchBarManager.getActiveLaunchMode());
		targetChangedRecord.add(state);
	}
}
