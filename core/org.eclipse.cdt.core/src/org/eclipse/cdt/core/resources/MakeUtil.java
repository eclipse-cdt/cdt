package org.eclipse.cdt.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

public class MakeUtil {

	final static String MAKE_GOALS = "goals";
	final static String MAKE_DIR = "buildir";
	final static String TARGET_ID = "org.eclipse.cdt.make";

	public static String[] decodeTargets(String property) {
		BufferedReader reader = new BufferedReader(new StringReader(property));
		ArrayList l = new ArrayList(5);
		try {
			String line = reader.readLine();
			while (line != null && !"".equals(line)) {
				l.add(line);
				line = reader.readLine();
			}
		} catch (IOException e) {
			// this should not happen, we're reading from a string.
		}
		String[] result = new String[l.size()];
		return (String[]) l.toArray(result);
	}

	public static String encodeTargets(String[] targets) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < targets.length; i++) {
			if (targets[i] != null) {
				buf.append(targets[i]);
				buf.append("\n");
			}
		}
		return (buf.length() == 0) ? null : buf.toString();
	}

	public static QualifiedName getQualifiedNameTarget() {
		return new QualifiedName(TARGET_ID, MAKE_GOALS);
	}

	public static QualifiedName getQualifiedNameDir() {
		return new QualifiedName(TARGET_ID, MAKE_DIR);
	}

	public static String getSessionTarget(IResource resource) {
		try {
			String property = (String) resource.getSessionProperty(getQualifiedNameTarget());
			if (property != null)
				return property;
		} catch (CoreException e) {
		}
		return new String();
	}

	public static void setSessionTarget(IResource resource, String target) {
		try {
			resource.setSessionProperty(getQualifiedNameTarget(), target);
		} catch (CoreException e) {
		}
	}

	public static void removeSessionTarget(IResource resource) {
		setSessionTarget(resource, null);
	}

	public static String getSessionBuildDir(IResource resource) {
		try {
			String dir = (String) resource.getSessionProperty(getQualifiedNameDir());
			if (dir != null)
				return dir;
		} catch (CoreException e) {
		}
		return new String();
	}

	public static void setSessionBuildDir(IResource resource, String dir) {
		try {
			resource.setSessionProperty(getQualifiedNameDir(), dir);
		} catch (CoreException e) {
		}
	}

	public static void removeSessionBuildDir(IResource resource) {
		setSessionBuildDir(resource, null);
	}

	public static String[] getPersistentTargets(IResource resource) {
		try {
			String property = resource.getPersistentProperty(getQualifiedNameTarget());
			if (property != null)
				return decodeTargets(property);
		} catch (CoreException e) {
		}
		return new String[0];
	}

	public static void setPersistentTargets(IResource resource, String[] targets) {
		String property = null;
		if (targets != null)
			property = encodeTargets(targets);
		//System.out.println ("PROPERTY " + property);
		try {
			resource.setPersistentProperty(getQualifiedNameTarget(), property);
		} catch (CoreException e) {
		}
	}

	public static void addPersistentTarget(IResource resource, String target) {
		String[] targets = MakeUtil.getPersistentTargets(resource);
		for (int i = 0; i < targets.length; i++) {
			if (targets[i].equals(target)) {
				return;
			}
		}
		String[] newTargets = new String[targets.length + 1];
		System.arraycopy(targets, 0, newTargets, 0, targets.length);
		newTargets[targets.length] = target;
		MakeUtil.setPersistentTargets(resource, newTargets);
	}

	public static void removePersistentTarget(IResource resource, String target) {
		String[] targets = MakeUtil.getPersistentTargets(resource);
		String[] newTargets = new String[targets.length];
		for (int i = 0; i < targets.length; i++) {
			if (!targets[i].equals(target)) {
				newTargets[i] = targets[i];
			}
		}
		MakeUtil.setPersistentTargets(resource, newTargets);
	}

	/**
	 * Replace a tag on a resource. Functionally equivalent to
	 *  removePersistantTag(resource, oldtarget)
	 *  addPersistantTag(resource, newtarget)
	 * If the oldtarget doesn't exist, the newtarget is added. 
	 * If the newtarget is null, the oldtarget is removed.
	 * @param resource The resource the tag applies to
	 * @param oldtarget The oldtarget tag
	 * @param newtarget The newtarget tag to replace the old target tag or null. If
	 * newtarget is null then this call is the same as removePersistantTarget(resource, oldtarget)
	 */
	public static void replacePersistentTarget(IResource resource, String oldtarget, String newtarget) {
		if (newtarget == null) {
			removePersistentTarget(resource, oldtarget);
			return;
		}

		String[] targets = getPersistentTargets(resource);
		for (int i = 0; i < targets.length; i++) {
			if (targets[i].equals(oldtarget)) {
				targets[i] = newtarget;
				setPersistentTargets(resource, targets);
				return;
			}
		}

		//The target wasn't found, create a new one
		addPersistentTarget(resource, newtarget);
	}

	private MakeUtil() {
	}

}
