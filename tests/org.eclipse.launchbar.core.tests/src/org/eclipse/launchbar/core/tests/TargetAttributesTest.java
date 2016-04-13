package org.eclipse.launchbar.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.junit.Test;

@SuppressWarnings("nls")
public class TargetAttributesTest {

	@Test
	public void testAttribute() {
		ILaunchTargetManager manager = Activator.getLaunchTargetManager();
		String targetType = "testType";
		String targetId = "testTarget";
		ILaunchTarget target = manager.getLaunchTarget(targetType, targetId);
		if (target != null) {
			manager.removeLaunchTarget(target);
		}
		target = manager.addLaunchTarget(targetType, targetId);
		String attributeKey = "testKey";
		String attributeValue = "testValue";
		assertEquals(target.getAttribute(attributeKey, ""), "");
		ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
		assertNotEquals(target, wc);
		wc.setAttribute(attributeKey, attributeValue);
		assertEquals(wc.getAttribute(attributeKey, ""), attributeValue);
		ILaunchTarget savedTarget = wc.save();
		assertEquals(target, savedTarget);
		assertEquals(target.getAttribute(attributeKey, ""), attributeValue);
	}
}
