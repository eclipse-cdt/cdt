package org.eclipse.cdt.internal.p2.touchpoint.natives.actions;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.touchpoint.natives.actions.CleanupzipAction;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningAction;

/**
 * Cleanup an untared artifact.
 * 
 * syntax: cleanupuntar(source:@artifact, target:${installFolder}/<subdir>, compression:[gz|bz2])
 * 
 * @author DSchaefe
 *
 */
public class CleanupUntarAction extends ProvisioningAction {

	@Override
	public IStatus execute(Map parameters) {
		return cleanup(parameters);
	}

	@Override
	public IStatus undo(Map parameters) {
		return UntarAction.untar(parameters);
	}

	public static IStatus cleanup(Map parameters) {
		return CleanupzipAction.cleanupzip(parameters);
	}
}
