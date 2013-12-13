/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.tests;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.qt.core.QtNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;

public class BaseQtTestCase extends BaseTestCase {

	// TODO There is a problem with the unit test framework where it sometimes will not wait
	//      long enough for the index to be updated.  For now mask this problem by stopping
	//      that test and continuing with the rest.
	@Deprecated
	protected boolean isIndexOk(String indexName, Object obj) {
		if (obj != null)
			return true;

		System.err.println(getClass().getSimpleName() + '.' + getName() + ": could not find " + indexName + " in the index, continuing with other tests");
		return false;
	}

	protected IProject fProject;
	protected IFile fFile;
	protected ICProject fCProject;
	protected IIndex fIndex;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		String projectName = "__" + getClass().getSimpleName() + "__";

		fCProject = CProjectHelper.createCCProject(projectName, "bin", IPDOMManager.ID_FAST_INDEXER);
		fProject = fCProject.getProject();
		QtNature.addNature(fProject, new NullProgressMonitor());
		fIndex = CCorePlugin.getIndexManager().getIndex(fCProject);

		indexQObject_h();
	}

	@Override
	protected void tearDown() throws Exception {
		if (fCProject != null)
			CProjectHelper.delete(fCProject);

		fIndex = null;
		fCProject = null;
		fProject = null;
		super.tearDown();
	}

	/**
	 * This creates a mock Qt header file, which avoids putting the real Qt headers into the
	 * include path of this unit test's fake project.
	 */
	// #define QT_VERSION 0x040805
	// #define Q_PROPERTY(defn)
	// #define Q_OBJECT
	// #define Q_GADGET
	// #define Q_CLASSINFO(k,v)
	// #define Q_SIGNAL
	// #define Q_SLOT
	// #define Q_INVOKABLE
	// #define Q_DECLARE_FLAGS(t,e)
	// #define Q_ENUMS(e)
	// #define Q_FLAGS(e)
	// #define slots
	// #define signals protected
	// #define Q_SLOTS
	// #define Q_SIGNALS protected
	// const char *qFlagLocation(const char *method);
	// #define SLOT(a)   qFlagLocation("1"#a)
	// #define SIGNAL(a) qFlagLocation("2"#a)
	// #define QML_DECLARE_TYPEINFO( T, F ) template <> struct QDeclarativeTypeInfo<T> { enum { H = F }; };
	// enum { QML_HAS_ATTACHED_PROPERTIES = 0x01 };
	// class QObject
	// {
	// Q_OBJECT
	// Q_SIGNAL void destroyed( QObject * );
	// public:
	//     static bool connect( QObject *, const char *, QObject *, const char * );
	// };
	// class QString { public: QString( const char * ch ); };
	// template<typename T>                         int qmlRegisterType(const char *uri, int versionMajor, int versionMinor, const char *qmlName);
	// template<typename T, int metaObjectRevision> int qmlRegisterType(const char *uri, int versionMajor, int versionMinor, const char *qmlName);
	// template<typename T> int qmlRegisterUncreatableType(const char *uri, int versionMajor, int versionMinor, const char *qmlName, const QString& reason);
	public void indexQObject_h() throws Exception {
		loadComment("junit-QObject.hh");
	}

	private String[] getContentsForTest(int blocks) throws Exception {
    	String callingMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
    	CharSequence[] help= TestSourceReader.getContentsForTest(
    			QtTestPlugin.getDefault().getBundle(), "src", getClass(), callingMethod, blocks);
    	String[] result= new String[help.length];
    	int i= 0;
    	for (CharSequence buf : help) {
			result[i++]= buf.toString();
		}
    	return result;
    }

	/**
	 * The implementation of TestSourceReader (called from BaseTestCase) imposes some restrictions
	 * on the caller of #loadComment.
	 * <ol>
	 * <li>loadComment must be called from a public method</li>
	 * <li>loadComment must be called from a method that does not accept parameters</li>
	 * </ol>
	 */
    protected void loadComment(String filename) throws Exception {
		String[] contents= getContentsForTest(1);

		// get the timestamp of the last change to the index
		IIndexManager indexManager = CCorePlugin.getIndexManager();
		long timestamp = indexManager.getIndex(fCProject).getLastWriteAccess();

		// add the new content
		fFile = TestSourceReader.createFile(fProject, filename, contents[0]);

		// wait for the index to change
		Thread.yield();
		for(long stopAt = System.currentTimeMillis() + 3000;
			System.currentTimeMillis() < stopAt && timestamp == indexManager.getIndex(fCProject).getLastWriteAccess();
			Thread.sleep(100)) {
			/* intentionally empty*/
		}

		assertNotSame(timestamp, indexManager.getIndex(fCProject).getLastWriteAccess());
    }

    /**
     * A utility method for pausing the JUNIT code.  This is helpful when investigating the
     * CDT indexer, which runs in a different job.  The idea is to use it only while debugging,
     * and to change the value of the pause variable in the loop in order to continue.
     */
	protected static void pause() throws Exception {
		String oldName = Thread.currentThread().getName();
		Thread.currentThread().setName("*** JUNIT PAUSED ***");
		try
		{
			// pause = false
			boolean pause = true;
			do
			{
				Thread.sleep(10000);
			} while( pause );

		} finally {
			Thread.currentThread().setName(oldName);
		}
	}
}
