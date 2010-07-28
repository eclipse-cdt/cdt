/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Gvozdev
 *******************************************************************************/

package org.eclipse.cdt.errorparsers.xlc.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParserNamed;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/*
 * Helper tester class to be able to test XlcErrorParser which extends AbstractErrorParser.
 */

public class XlcErrorParserTester {
	public static final String XLC_ERROR_PARSER_ID = "org.eclipse.cdt.errorparsers.xlc.XlcErrorParser";

	static private int counter=0;
	IProject fTempProject = ResourcesPlugin.getWorkspace().getRoot().getProject("XlcErrorParserTester.temp." + counter++);

	XlcErrorParserTester() {
		try {
			fTempProject.create(null);
		} catch (CoreException e) {
			e.printStackTrace();
			Assert.fail("Exception creating temporary project "+fTempProject.getName()+": "+e);
		}
	}

	private class MarkerData {
		private String fileName;
		private int lineNumber;
		private int severity;
		private String message;
	}
	
	private List<MarkerData> markerDataList = new ArrayList<MarkerData>();

	/*
	 * Dummy class implementing IMarkerGenerator lets get through testing
	 * without NPE.
	 */
	private class MockMarkerGenerator implements IMarkerGenerator {

		public void addMarker(IResource file, int lineNumber, String errorDesc,
				int severity, String errorVar) {
			// dummy
		}

		public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
			// dummy
		}

	}

	/*
	 * Class MockErrorParserManager replaces ErrorParserManager
	 * with the purpose to be able to inquire how the line was parsed.
	 * fileName, lineNumber, message and severity are populated
	 * to be accessed from the test cases.
	 * Relying on internal implementation of ErrorPattern.RecordError()
	 * to provide necessary data via generateExternalMarker() call
	 */
	private class MockErrorParserManager extends ErrorParserManager {

		private MockErrorParserManager() {
			super(fTempProject, new MockMarkerGenerator());
		}

		/*
		 * A stub function just to return non-null IFile.
		 * Necessary to trick ErrorPattern.RecordError() to generate markers.
		 */
		@Override
		public IFile findFileName(String fileName) {
			if (fileName!=null && fileName.trim().length()>0)
				return fTempProject.getFile(fileName);
			return null;
		}

		/**
		 * Called by ErrorPattern.RecordError() for external problem markers
		 */
		@Override
		public void generateExternalMarker(IResource rc, int lineNumb, String desc, int sev, String varName, IPath externalPath) {
			// if rc is this project it means that file was not found
			MarkerData markerData = new MarkerData();
			if (rc!=null && rc!=fTempProject) {
				markerData.fileName = rc.getName();
			} else {
				markerData.fileName="";
			}
			markerData.lineNumber = lineNumb;
			markerData.message = desc;
			markerData.severity = sev;
			
			markerDataList.add(markerData);
		}
	}

	/**
	 * Main method called by individual error parser tests.
	 * @param line one xlC error message
	 * @return
	 */
	boolean parseLine(String line) {
		IErrorParserNamed errorParser = ErrorParserManager.getErrorParserCopy(XLC_ERROR_PARSER_ID);
		Assert.assertNotNull(errorParser);

		MockErrorParserManager epManager = new MockErrorParserManager();
		return errorParser.processLine(line, epManager);
	}

	int getNumberOfMarkers() {
		return markerDataList.size();
	}

	String getFileName(int i) {
		return markerDataList.get(i).fileName;
	}

	int getLineNumber(int i) {
		return markerDataList.get(i).lineNumber;
	}

	int getSeverity(int i) {
		return markerDataList.get(i).severity;
	}

	String getMessage(int i) {
		return markerDataList.get(i).message;
	}
}
