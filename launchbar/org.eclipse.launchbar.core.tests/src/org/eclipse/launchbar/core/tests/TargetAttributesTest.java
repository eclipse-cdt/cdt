package org.eclipse.launchbar.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.junit.Test;

public class TargetAttributesTest {

	@Test
	public void testAttribute() {
		ILaunchTargetManager manager = Activator.getLaunchTargetManager();
		String targetType = "testType";
		String targetId = "testTarget";
		String attributeKey = "testKey";
		String attributeValue = "testValue";
		// Make sure the target doesn't exist
		ILaunchTarget target = manager.getLaunchTarget(targetType, targetId);
		if (target != null) {
			manager.removeLaunchTarget(target);
		}
		// Add the target
		target = manager.addLaunchTarget(targetType, targetId);
		// Attribute should be empty
		assertEquals(target.getAttribute(attributeKey, ""), "");
		// Set the attribute and make sure it's set
		ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
		assertNotEquals(target, wc);
		wc.setAttribute(attributeKey, attributeValue);
		assertEquals(wc.getAttribute(attributeKey, ""), attributeValue);
		ILaunchTarget savedTarget = wc.save();
		// Make sure we get our original back
		assertEquals(target, savedTarget);
		assertEquals(target.getAttribute(attributeKey, ""), attributeValue);
		// Make sure remove removes the attribute
		manager.removeLaunchTarget(target);
		target = manager.addLaunchTarget(targetType, targetId);
		assertEquals(target.getAttribute(attributeKey, ""), "");
		// Cleanup
		manager.removeLaunchTarget(target);
	}
}
