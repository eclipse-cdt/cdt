/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alex Collins (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.testplugin;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import junit.framework.Assert;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Based on org.eclipse.core.tests.resources.ResourceDeltaVerifier with
 * additional support for ignoring changes in certain resources.
 *
 * Verifies the state of an <code>IResourceDelta</code> by comparing
 * it with a client's expectations.  The delta is considered valid
 * if it contains exactly the set of changes expected by the client,
 * and parents of those changes (having ignore filtered resources).
 *
 * <p>Example usage:
 * <code>
 * ResourceDeltaVerifier verifier = new ResourceDeltaVerifier();
 * IResourceChangeListener listener = (IResourceChangeListener)verifier;
 * IWorkspace workspace = ResourcesPlugin.getWorkspace();
 * IProject proj = workspace.getRoot().getProject("MyProject");
 * // Assume the project is accessible
 * workspace.addResourceChangeListener(listener);
 * verifier.addExpectedChange(proj, REMOVED, 0);
 * try {
 * 		proj.delete(true, true, null);
 * } catch(CoreException e){
 *     fail("1.0", e);
 * }
 * assert("2.0 "+verifier.getMessage(), verifier.isDeltaValid());
 * </code>
 */
public class ResourceDeltaVerifier extends Assert implements IResourceChangeListener {
	private class ExpectedChange {
		IResource fResource;
		IPath movedFromPath;
		IPath movedToPath;
		int fKind;
		int fChangeFlags;

		public ExpectedChange(IResource resource, int kind, int changeFlags, IPath movedFromPath, IPath movedToPath) {
			fResource = resource;
			fKind = kind;
			fChangeFlags = changeFlags;
			this.movedFromPath = movedFromPath;
			this.movedToPath = movedToPath;
		}

		public int getChangeFlags() {
			return fChangeFlags;
		}

		public IPath getMovedFromPath() {
			if ((fChangeFlags & IResourceDelta.MOVED_FROM) != 0) {
				return movedFromPath;
			}
			return null;
		}

		public IPath getMovedToPath() {
			if ((fChangeFlags & IResourceDelta.MOVED_TO) != 0) {
				return movedToPath;
			}
			return null;
		}

		public int getKind() {
			return fKind;
		}

		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer("ExpectedChange(");
			buf.append(fResource);
			buf.append(", ");
			buf.append(convertKind(fKind));
			buf.append(", ");
			buf.append(convertChangeFlags(fChangeFlags));
			buf.append(")");
			return buf.toString();
		}

	}

	/**
	 * Table of IPath -> ExpectedChange
	 */
	private Hashtable<IPath, ExpectedChange> fExpectedChanges = new Hashtable<IPath, ExpectedChange>();
	boolean fIsDeltaValid = true;
	private StringBuffer fMessage = new StringBuffer();
	/**
	 * The verifier can be in one of three states.  In the initial
	 * state, the verifier is still receiving inputs via the
	 * addExpectedChange() methods, and the state is RECEIVING_INPUTS.
	 * After a call to verifyDelta(), the state becomes DELTA_VERIFIED
	 * The verifier remains in the second state for any number of delta
	 * verifications.  When a getMessage() or isDeltaValid() method is
	 * called, the verification completes, and the state becomes
	 * VERIFICATION_COMPLETE.  While in this state, any number of
	 * getMessage() and isDeltaValid() methods can be called.
	 * While in the third state, any call to addExpectedChange()
	 * resets the verifier and puts it back in its RECEIVING_INPUTS state.
	 */
	private static final int RECEIVING_INPUTS = 0;
	private static final int DELTA_VERIFIED = 1;
	private static final int VERIFICATION_COMPLETE = 2;

	private int fState = RECEIVING_INPUTS;
	private Set<IResource> fIgnoreResources = new HashSet<IResource>();

	/**
	 * @see #addExpectedChange
	 */
	public void addExpectedChange(IResource[] resources, int status, int changeFlags) {
		for (int i = 0; i < resources.length; i++)
			addExpectedChange(resources[i], null, status, changeFlags, null, null);
	}

	/**
	 * Adds an expected deletion for the given resource and all children.
	 */
	public void addExpectedDeletion(IResource resource) {
		addExpectedChange(resource, IResourceDelta.REMOVED, 0);
		if (resource instanceof IContainer) {
			try {
				IResource[] children = ((IContainer) resource).members(IContainer.INCLUDE_PHANTOMS | IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS | IContainer.INCLUDE_HIDDEN);
				for (int i = 0; i < children.length; i++) {
					addExpectedDeletion(children[i]);
				}
			} catch (CoreException e) {
				e.printStackTrace();
				fail("Failed to get children in addExpectedDeletion");
			}
		}
	}

	/**
	 * Signals to the comparer that the given resource is expected to
	 * change in the specified way.  The change flags should be set to
	 * zero if no change is expected.
	 * @param resource the resource that is expected to change
	 * @param status the type of change (ADDED, REMOVED, CHANGED)
	 * @param changeFlags the type of change (CONTENT, SYNC, etc)
	 */
	public void addExpectedChange(IResource resource, int status, int changeFlags) {
		addExpectedChange(resource, null, status, changeFlags, null, null);
	}

	/**
	 * Signals to the comparer that the given resource is expected to
	 * change in the specified way.  The change flags should be set to
	 * zero if no change is expected.
	 * @param resource the resource that is expected to change
	 * @param status the type of change (ADDED, REMOVED, CHANGED)
	 * @param changeFlags the type of change (CONTENT, SYNC, etc)
	 */
	public void addExpectedChange(IResource resource, int status, int changeFlags, IPath movedFromPath, IPath movedToPath) {
		addExpectedChange(resource, null, status, changeFlags, movedFromPath, movedToPath);
	}

	/**
	 * Signals to the comparer that the given resource is expected to
	 * change in the specified way.  The change flags should be set to
	 * zero if no change is expected.
	 * @param resource the resource that is expected to change
	 * @param topLevelParent Do not added expected changes above this parent
	 * @param status the type of change (ADDED, REMOVED, CHANGED)
	 * @param changeFlags the type of change (CONTENT, SYNC, etc)
	 */
	public void addExpectedChange(IResource resource, IResource topLevelParent, int status, int changeFlags) {
		addExpectedChange(resource, topLevelParent, status, changeFlags, null, null);
	}

	/**
	 * Signals to the comparer that the given resource is expected to
	 * change in the specified way.  The change flags should be set to
	 * zero if no change is expected.
	 * @param resource the resource that is expected to change
	 * @param topLevelParent Do not added expected changes above this parent
	 * @param status the type of change (ADDED, REMOVED, CHANGED)
	 * @param changeFlags the type of change (CONTENT, SYNC, etc)
	 */
	public void addExpectedChange(IResource resource, IResource topLevelParent, int status, int changeFlags, IPath movedFromPath, IPath movedToPath) {
		resetIfNecessary();

		ExpectedChange expectedChange = new ExpectedChange(resource, status, changeFlags, movedFromPath, movedToPath);
		fExpectedChanges.put(resource.getFullPath(), expectedChange);

		// Add changes for all resources above this one and limited by the topLevelParent
		IResource parentResource = resource.getParent();
		IResource limit = (topLevelParent == null) ? null : topLevelParent.getParent();
		while (parentResource != null && !parentResource.equals(limit)) {
			//change table is keyed by resource path
			IPath key = parentResource.getFullPath();
			if (fExpectedChanges.get(key) == null) {
				ExpectedChange parentExpectedChange = new ExpectedChange(parentResource, IResourceDelta.CHANGED, 0, null, null);
				fExpectedChanges.put(key, parentExpectedChange);
			}
			parentResource = parentResource.getParent();
		}
	}

	private void checkChanges(IResourceDelta delta) {
		IResource resource = delta.getResource();

		ExpectedChange expectedChange = fExpectedChanges.remove(resource.getFullPath());

		int status = delta.getKind();
		int changeFlags = delta.getFlags();

		if (status == IResourceDelta.NO_CHANGE)
			return;

		if (expectedChange == null) {
			recordMissingExpectedChange(status, changeFlags);
		} else {
			int expectedStatus = expectedChange.getKind();
			int expectedChangeFlags = expectedChange.getChangeFlags();
			if (status != expectedStatus || changeFlags != expectedChangeFlags) {
				recordConflictingChange(expectedStatus, status, expectedChangeFlags, changeFlags);
			}
		}
	}

	private void checkChildren(IResourceDelta delta) {
		IResourceDelta[] affectedChildren = delta.getAffectedChildren(IResourceDelta.ALL_WITH_PHANTOMS, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS | IContainer.INCLUDE_HIDDEN);
		IResourceDelta[] addedChildren = delta.getAffectedChildren(IResourceDelta.ADDED, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS | IContainer.INCLUDE_HIDDEN);
		IResourceDelta[] changedChildren = delta.getAffectedChildren(IResourceDelta.CHANGED, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS | IContainer.INCLUDE_HIDDEN);
		IResourceDelta[] removedChildren = delta.getAffectedChildren(IResourceDelta.REMOVED, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS | IContainer.INCLUDE_HIDDEN);

		Hashtable<IResource, IResourceDelta> h = new Hashtable<IResource, IResourceDelta>(affectedChildren.length + 1);

		for (int i = 0; i < addedChildren.length; ++i) {
			IResourceDelta childDelta1 = addedChildren[i];
			IResource childResource = childDelta1.getResource();
			IResourceDelta childDelta2 = h.get(childResource);
			if (childDelta2 != null) {
				recordDuplicateChild(childResource.getFullPath(), childDelta2.getKind(), childDelta1.getKind(), IResourceDelta.ADDED);
			} else {
				h.put(childResource, childDelta1);
			}
			if (childDelta1.getKind() != IResourceDelta.ADDED) {
				recordIllegalChild(childResource.getFullPath(), IResourceDelta.ADDED, childDelta1.getKind());
			}
		}

		for (int i = 0; i < changedChildren.length; ++i) {
			IResourceDelta childDelta1 = changedChildren[i];
			IResource childResource = childDelta1.getResource();
			IResourceDelta childDelta2 = h.get(childResource);
			if (childDelta2 != null) {
				recordDuplicateChild(childResource.getFullPath(), childDelta2.getKind(), childDelta1.getKind(), IResourceDelta.CHANGED);
			} else {
				h.put(childResource, childDelta1);
			}
			if (childDelta1.getKind() != IResourceDelta.CHANGED) {
				recordIllegalChild(childResource.getFullPath(), IResourceDelta.CHANGED, childDelta1.getKind());
			}
		}

		for (int i = 0; i < removedChildren.length; ++i) {
			IResourceDelta childDelta1 = removedChildren[i];
			IResource childResource = childDelta1.getResource();
			IResourceDelta childDelta2 = h.get(childResource);
			if (childDelta2 != null) {
				recordDuplicateChild(childResource.getFullPath(), childDelta2.getKind(), childDelta1.getKind(), IResourceDelta.REMOVED);
			} else {
				h.put(childResource, childDelta1);
			}
			if (childDelta1.getKind() != IResourceDelta.REMOVED) {
				recordIllegalChild(childResource.getFullPath(), IResourceDelta.REMOVED, childDelta1.getKind());
			}
		}

		for (int i = 0; i < affectedChildren.length; ++i) {
			IResourceDelta childDelta1 = affectedChildren[i];
			IResource childResource = childDelta1.getResource();
			IResourceDelta childDelta2 = h.remove(childResource);
			if (childDelta2 == null) {
				int kind = childDelta1.getKind();
				//these kinds should have been added to h earlier
				if (kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED || kind == IResourceDelta.CHANGED) {
					recordMissingChild(childResource.getFullPath(), childDelta1.getKind(), false);
				}
			}
		}

		Enumeration<IResource> keys = h.keys();
		while (keys.hasMoreElements()) {
			IResource childResource = keys.nextElement();
			IResourceDelta childDelta = h.get(childResource);
			recordMissingChild(childResource.getFullPath(), childDelta.getKind(), true);
		}

		for (int i = 0; i < affectedChildren.length; ++i) {
			internalVerifyDelta(affectedChildren[i]);
		}

		keys = h.keys();
		while (keys.hasMoreElements()) {
			IResource childResource = keys.nextElement();
			IResourceDelta childDelta = h.get(childResource);
			internalVerifyDelta(childDelta);
		}
	}

	private void checkPaths(IResourceDelta delta) {
		IResource resource = delta.getResource();

		IPath expectedFullPath = resource.getFullPath();
		IPath actualFullPath = delta.getFullPath();
		if (!expectedFullPath.equals(actualFullPath)) {
			recordConflictingFullPaths(expectedFullPath, actualFullPath);
		}

		IPath expectedProjectRelativePath = resource.getProjectRelativePath();
		IPath actualProjectRelativePath = delta.getProjectRelativePath();
		if (expectedProjectRelativePath != actualProjectRelativePath) {
			if (expectedProjectRelativePath == null || !expectedProjectRelativePath.equals(actualProjectRelativePath)) {
				recordConflictingProjectRelativePaths(expectedProjectRelativePath, actualProjectRelativePath);
			}
		}

		ExpectedChange expectedChange = fExpectedChanges.get(resource.getFullPath());

		if (expectedChange != null) {
			IPath expectedMovedFromPath = expectedChange.getMovedFromPath();
			IPath actualMovedFromPath = delta.getMovedFromPath();
			if (expectedMovedFromPath != actualMovedFromPath) {
				if (expectedMovedFromPath == null || !expectedMovedFromPath.equals(actualMovedFromPath)) {
					recordConflictingMovedFromPaths(expectedMovedFromPath, actualMovedFromPath);
				}
			}

			IPath expectedMovedToPath = expectedChange.getMovedToPath();
			IPath actualMovedToPath = delta.getMovedToPath();
			if (expectedMovedToPath != actualMovedToPath) {
				if (expectedMovedToPath == null || !expectedMovedToPath.equals(actualMovedToPath)) {
					recordConflictingMovedToPaths(expectedMovedToPath, actualMovedToPath);
				}
			}
		}
	}

	String convertChangeFlags(int changeFlags) {
		if (changeFlags == 0) {
			return "0";
		}
		StringBuffer buf = new StringBuffer();

		if ((changeFlags & IResourceDelta.CONTENT) != 0) {
			changeFlags ^= IResourceDelta.CONTENT;
			buf.append("CONTENT | ");
		}
		if ((changeFlags & IResourceDelta.MOVED_FROM) != 0) {
			changeFlags ^= IResourceDelta.MOVED_FROM;
			buf.append("MOVED_FROM | ");
		}
		if ((changeFlags & IResourceDelta.MOVED_TO) != 0) {
			changeFlags ^= IResourceDelta.MOVED_TO;
			buf.append("MOVED_TO | ");
		}
		if ((changeFlags & IResourceDelta.OPEN) != 0) {
			changeFlags ^= IResourceDelta.OPEN;
			buf.append("OPEN | ");
		}
		if ((changeFlags & IResourceDelta.TYPE) != 0) {
			changeFlags ^= IResourceDelta.TYPE;
			buf.append("TYPE | ");
		}
		if ((changeFlags & IResourceDelta.MARKERS) != 0) {
			changeFlags ^= IResourceDelta.MARKERS;
			buf.append("MARKERS | ");
		}
		if ((changeFlags & IResourceDelta.REPLACED) != 0) {
			changeFlags ^= IResourceDelta.REPLACED;
			buf.append("REPLACED | ");
		}
		if ((changeFlags & IResourceDelta.ENCODING) != 0) {
			changeFlags ^= IResourceDelta.ENCODING;
			buf.append("ENCODING | ");
		}
		if ((changeFlags & IResourceDelta.DERIVED_CHANGED) != 0) {
			changeFlags ^= IResourceDelta.DERIVED_CHANGED;
			buf.append("DERIVED_CHANGED | ");
		}
		if ((changeFlags & IResourceDelta.DESCRIPTION) != 0) {
			changeFlags ^= IResourceDelta.DESCRIPTION;
			buf.append("DESCRIPTION | ");
		}
		if ((changeFlags & IResourceDelta.SYNC) != 0) {
			changeFlags ^= IResourceDelta.SYNC;
			buf.append("SYNC | ");
		}

		if (changeFlags != 0) {
			buf.append(changeFlags);
			buf.append(" | ");
		}

		String result = buf.toString();

		if (result.length() != 0) {
			result = result.substring(0, result.length() - 3);
		}

		return result;
	}

	String convertKind(int kind) {
		switch (kind) {
			case IResourceDelta.ADDED :
				return "ADDED";
			case IResourceDelta.CHANGED :
				return "CHANGED";
			case IResourceDelta.REMOVED :
				return "REMOVED";
			case IResourceDelta.ADDED_PHANTOM :
				return "ADDED_PHANTOM";
			case IResourceDelta.REMOVED_PHANTOM :
				return "REMOVED_PHANTOM";
			default :
				return "Unknown(" + kind + ")";
		}
	}

	/**
	 * Called to cleanup internal state and make sure expectations
	 * are met after iterating over a resource delta.
	 */
	private void finishVerification() {
		Hashtable<IPath, IPath> resourcePaths = new Hashtable<IPath, IPath>();

		Enumeration<IPath> keys = fExpectedChanges.keys();
		while (keys.hasMoreElements()) {
			IPath key = keys.nextElement();
			resourcePaths.put(key, key);
		}

		keys = resourcePaths.keys();
		while (keys.hasMoreElements()) {
			IPath resourcePath = keys.nextElement();

			fMessage.append("Checking expectations for ");
			fMessage.append(resourcePath);
			fMessage.append("\n");

			ExpectedChange expectedChange = fExpectedChanges.remove(resourcePath);
			if (expectedChange != null) {
				// List an ignored resource
				if (fIgnoreResources.contains(expectedChange.fResource))
					fMessage.append("\tIgnored\n");
				else
					recordMissingActualChange(expectedChange.getKind(), expectedChange.getChangeFlags());
			}
		}
	}

	/**
	 * Returns a message that describes the result of the resource
	 * delta verification checks.
	 */
	public String getMessage() {
		if (fState == RECEIVING_INPUTS) {
			if (hasExpectedChanges()) {
				fail("Verifier has not yet been given a resource delta");
			} else {
				fState = DELTA_VERIFIED;
			}
		}
		if (fState == DELTA_VERIFIED) {
			finishVerification();
			fState = VERIFICATION_COMPLETE;
		}
		return fMessage.toString();
	}

	/**
	 * Returns true if this verifier has received a delta notification
	 * since the last reset, and false otherwise.
	 */
	public boolean hasBeenNotified() {
		return fState == DELTA_VERIFIED;
	}

	/**
	 * Returns true if this verifier currently has an expected
	 * changes, and false otherwise.
	 */
	public boolean hasExpectedChanges() {
		return !fExpectedChanges.isEmpty();
	}

	/**
	 * Compares the given delta with the expected changes.  Recursively
	 * compares child deltas.
	 */
	void internalVerifyDelta(IResourceDelta delta) {
		try {
			// FIXME: bogus
			if (delta == null)
				return;
			fMessage.append("Verifying delta for ");
			fMessage.append(delta.getFullPath());
			fMessage.append("\n");

			// Don't check changes for the workspace
			// or for ignored resources
			if (delta.getResource() != null && !fIgnoreResources.contains(delta.getResource())) {
				checkPaths(delta);
				checkChanges(delta);
			}

			checkChildren(delta);
		} catch (Exception e) {
			e.printStackTrace();
			fMessage.append("Exception during event notification:" + e.getMessage());
			fIsDeltaValid = false;
		}
	}

	/**
	 * Returns whether the resource delta passed all verification
	 * checks.
	 */
	public boolean isDeltaValid() {
		if (fState == RECEIVING_INPUTS) {
			if (hasExpectedChanges()) {
				fail("Verifier has not yet been given a resource delta");
			} else {
				fState = DELTA_VERIFIED;
			}
		}
		if (fState == DELTA_VERIFIED) {
			finishVerification();
			fState = VERIFICATION_COMPLETE;
		}
		return fIsDeltaValid;
	}

	/**
	 * Tests message formatting.  This main method does not represent the
	 * intended use of the ResourceDeltaVerifier.  See the class comment
	 * for instructions on using the verifier.
	 */
	public static void main(String[] args) {
		ResourceDeltaVerifier comparer = new ResourceDeltaVerifier();

		int status = IResourceDelta.CHANGED;
		int changeFlags = IResourceDelta.CONTENT;
		int expectedStatus = IResourceDelta.CHANGED;
		int actualStatus = IResourceDelta.REMOVED;
		int expectedChangeFlags = IResourceDelta.OPEN;
		int actualChangeFlags = 0;
		int formerChildStatus = expectedStatus;
		int latterChildStatus = actualStatus;

		IPath path = new Path("/a/b/c");
		IPath path2 = new Path("/a/b/d");
		IPath expectedFullPath = path;
		IPath actualFullPath = path2;
		IPath expectedMovedFromPath = path;
		IPath actualMovedFromPath = path2;
		IPath expectedMovedToPath = path;
		IPath actualMovedToPath = path2;
		IPath expectedProjectRelativePath = new Path("b/c");
		IPath actualProjectRelativePath = new Path("b/d");

		comparer.fMessage.append("Checking delta for ");
		comparer.fMessage.append(path);
		comparer.fMessage.append("\n");

		comparer.recordConflictingChange(expectedStatus, actualStatus, expectedChangeFlags, actualChangeFlags);
		comparer.recordConflictingFullPaths(expectedFullPath, actualFullPath);
		comparer.recordConflictingMovedFromPaths(expectedMovedFromPath, actualMovedFromPath);
		comparer.recordConflictingMovedToPaths(expectedMovedToPath, actualMovedToPath);
		comparer.recordConflictingProjectRelativePaths(expectedProjectRelativePath, actualProjectRelativePath);
		comparer.recordDuplicateChild(path, formerChildStatus, latterChildStatus, expectedStatus);
		comparer.recordIllegalChild(path, expectedStatus, actualStatus);
		comparer.recordMissingActualChange(status, changeFlags);
		comparer.recordMissingChild(path, status, true);
		comparer.recordMissingChild(path, status, false);
		comparer.recordMissingExpectedChange(status, changeFlags);

		System.out.print(comparer.fMessage.toString());
	}

	private void recordConflictingChange(int expectedKind, int kind, int expectedChangeFlags, int changeFlags) {
		fIsDeltaValid = false;

		fMessage.append("\tConflicting change\n");

		if (expectedKind != kind) {
			fMessage.append("\t\tExpected kind: <");
			fMessage.append(convertKind(expectedKind));
			fMessage.append("> actual kind: <");
			fMessage.append(convertKind(kind));
			fMessage.append(">\n");
		}

		if (expectedChangeFlags != changeFlags) {
			fMessage.append("\t\tExpected change flags: <");
			fMessage.append(convertChangeFlags(expectedChangeFlags));
			fMessage.append("> actual change flags: <");
			fMessage.append(convertChangeFlags(changeFlags));
			fMessage.append(">\n");
		}
	}

	private void recordConflictingFullPaths(IPath expectedFullPath, IPath actualFullPath) {
		fIsDeltaValid = false;

		fMessage.append("\tConflicting full paths\n");

		fMessage.append("\t\tExpected full path: ");
		fMessage.append(expectedFullPath);
		fMessage.append("\n");

		fMessage.append("\t\tActual full path: ");
		fMessage.append(actualFullPath);
		fMessage.append("\n");
	}

	private void recordConflictingMovedFromPaths(IPath expectedMovedFromPath, IPath actualMovedFromPath) {
		fIsDeltaValid = false;

		fMessage.append("\tConflicting moved from paths\n");

		fMessage.append("\t\tExpected moved from path: ");
		fMessage.append(expectedMovedFromPath);
		fMessage.append("\n");

		fMessage.append("\t\tActual moved from path: ");
		fMessage.append(actualMovedFromPath);
		fMessage.append("\n");
	}

	private void recordConflictingMovedToPaths(IPath expectedMovedToPath, IPath actualMovedToPath) {
		fIsDeltaValid = false;

		fMessage.append("\tConflicting moved to paths\n");

		fMessage.append("\t\tExpected moved to path: ");
		fMessage.append(expectedMovedToPath);
		fMessage.append("\n");

		fMessage.append("\t\tActual moved to path: ");
		fMessage.append(actualMovedToPath);
		fMessage.append("\n");
	}

	private void recordConflictingProjectRelativePaths(IPath expectedProjectRelativePath, IPath actualProjectRelativePath) {
		fIsDeltaValid = false;

		fMessage.append("\tConflicting project relative paths\n");

		fMessage.append("\t\tExpected project relative path: ");
		fMessage.append(expectedProjectRelativePath);
		fMessage.append("\n");

		fMessage.append("\t\tActual project relative path: ");
		fMessage.append(actualProjectRelativePath);
		fMessage.append("\n");
	}

	private void recordDuplicateChild(IPath path, int formerChildKind, int latterChildKind, int expectedKind) {
		fIsDeltaValid = false;

		fMessage.append("\tDuplicate child: ");
		fMessage.append(path);
		fMessage.append("\n");

		fMessage.append("\t\tProduced by IResourceDelta.get");

		switch (expectedKind) {
			case IResourceDelta.ADDED :
				fMessage.append("Added");
				break;
			case IResourceDelta.CHANGED :
				fMessage.append("Changed");
				break;
			case IResourceDelta.REMOVED :
				fMessage.append("Removed");
				break;
		}

		fMessage.append("Children()\n");

		fMessage.append("\t\tFormer child's status: ");
		fMessage.append(convertKind(formerChildKind));
		fMessage.append("\n");

		fMessage.append("\t\tLatter child's status: ");
		fMessage.append(convertKind(latterChildKind));
		fMessage.append("\n");
	}

	private void recordIllegalChild(IPath path, int expectedKind, int actualKind) {
		fIsDeltaValid = false;

		fMessage.append("\tIllegal child: ");
		fMessage.append(path);
		fMessage.append("\n");

		fMessage.append("\t\tProduced by IResourceDelta.get");

		switch (expectedKind) {
			case IResourceDelta.ADDED :
				fMessage.append("Added");
				break;
			case IResourceDelta.CHANGED :
				fMessage.append("Changed");
				break;
			case IResourceDelta.REMOVED :
				fMessage.append("Removed");
				break;
		}

		fMessage.append("Children()\n");

		fMessage.append("\t\tIlleagal child's status: ");
		fMessage.append(convertKind(actualKind));
		fMessage.append("\n");
	}

	private void recordMissingActualChange(int kind, int changeFlags) {
		fIsDeltaValid = false;

		fMessage.append("\tMissing actual change\n");
		fMessage.append("\t\tExpected kind: <");
		fMessage.append(convertKind(kind));
		fMessage.append(">\n");
		fMessage.append("\t\tExpected change flags: <");
		fMessage.append(convertChangeFlags(changeFlags));
		fMessage.append(">\n");
	}

	private void recordMissingChild(IPath path, int kind, boolean isMissingFromAffectedChildren) {
		fIsDeltaValid = false;

		fMessage.append("\tMissing child: ");
		fMessage.append(path);
		fMessage.append("\n");

		fMessage.append("\t\tfrom IResourceDelta.getAffectedChildren(");

		if (!isMissingFromAffectedChildren) {
			switch (kind) {
				case IResourceDelta.ADDED :
					fMessage.append("ADDED");
					break;
				case IResourceDelta.CHANGED :
					fMessage.append("CHANGED");
					break;
				case IResourceDelta.REMOVED :
					fMessage.append("REMOVED");
					break;
				default :
					fMessage.append(kind);
			}
		}

		fMessage.append(")\n");
	}

	private void recordMissingExpectedChange(int kind, int changeFlags) {
		fIsDeltaValid = false;

		fMessage.append("\tMissing expected change\n");
		fMessage.append("\t\tActual kind: <");
		fMessage.append(convertKind(kind));
		fMessage.append(">\n");
		fMessage.append("\t\tActual change flags: <");
		fMessage.append(convertChangeFlags(changeFlags));
		fMessage.append(">\n");
	}

	/**
	 * Resets the listener to its initial state.
	 */
	public void reset() {
		fExpectedChanges.clear();
		fIgnoreResources.clear();
		fIsDeltaValid = true;
		fMessage.setLength(0);
		fState = RECEIVING_INPUTS;
	}

	private void resetIfNecessary() {
		if (fState == DELTA_VERIFIED) {
			reset();
		}
	}

	/**
	 * Part of the <code>IResourceChangedListener</code> interface.
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent e) {
		fMessage.append("Resource Changed Delta\n");
		verifyDelta(e.getDelta());
	}

	/**
	 * Compares the given delta with the expected changes.  Recursively
	 * compares child deltas.
	 */
	public void verifyDelta(IResourceDelta delta) {
		internalVerifyDelta(delta);
		fState = DELTA_VERIFIED;
	}

	/**
	 * Add resources whose changes should be ignored
	 */
	public void addIgnore(IResource[] resources) {
		fIgnoreResources.addAll(Arrays.asList(resources));
	}
}
