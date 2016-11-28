/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form.tests

import com.google.inject.Inject
import com.google.inject.Injector
import java.util.ArrayList
import java.util.Arrays
import java.util.List
import java.util.stream.Collectors
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Table
import org.eclipse.swt.widgets.Tree
import org.eclipse.swt.widgets.TreeItem
import org.eclipse.ui.forms.widgets.FormToolkit
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(LinkerScriptInjectorProvider)
abstract class ViewerTestBase {

	@Inject Injector injector
	static Display display
	static boolean disposeDisplay
	/* no-op, just need to wake up, not do anything */
	static Runnable wakeDisplayRunnable = []

	static FormToolkit toolkit

	Shell shell

	boolean stopHumanSpeed = false
	Runnable humanSpeed = [|
		if (shell.isDisposed() || stopHumanSpeed) {
			return
		}
		Thread.sleep(200)
		display.timerExec(1, humanSpeed)
	]

	def abstract void createControl(Composite parent);

	def getToolkit() {
		return toolkit
	}

	@BeforeClass
	def static void createDisplay() {
		display = Display.current
		if (display == null) {
			display = new Display()
			disposeDisplay = true
		}
		toolkit = new FormToolkit(display);
	}

	@Before
	def void createAndOpenShell() {
		shell = new Shell(display);

		createControl(shell);

		shell.setLayout(new FillLayout());
		shell.setSize(500, 500);
		shell.open();
		readAndDispatch()

	// uncomment next line to run tests at "human speed"
	// display.asyncExec(humanSpeed);
	}

	@After
	def void disposeShell() {
		readAndDispatch()
		shell.close()
		shell.dispose()
		readAndDispatch()
	}

	@AfterClass
	def static void disposeDisplay() {
		toolkit.dispose
		if (disposeDisplay) {
			display.dispose
		}
	}

	/**
	 * Call this to stop at this point and be able to interact with the UI
	 */
	def void readAndDispatchForever() {
		readAndDispatch(-1);
	}

	def void readAndDispatch() {
		readAndDispatch(0);
	}

	/**
	 *
	 * @param time
	 *            < 0 means forever
	 */
	def void readAndDispatch(long time) {
		if (time < 0) {
			stopHumanSpeed = true;
		}
		val endtime = System.currentTimeMillis() + time;
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				val timeLeft = endtime - System.currentTimeMillis()
				if (timeLeft <= 0 && time >= 0) {
					return;
				}
				// when run independently of full eclipse the display.sleep can
				// appear to hang because there are 0 events left. so force sleep
				// to be no more than the amount of time left.
				// we always use the same instance of runnable so that
				// only one runnable ever exists, we only need one to make
				// sure the display eventually wakes up
				display.timerExec(timeLeft as int, wakeDisplayRunnable)
				display.sleep()
			}
		}
	}

	def newModel(String contents) {
		val model = new LinkerScriptModel()
		injector.injectMembers(model)
		model.contents = contents
		return model
	}

	def ROW(String... cols) {
		return new Row(cols)
	}

	static class Row {
		List<String> columns = new ArrayList<String>()

		new(String... cols) {
			columns.addAll(Arrays.asList(cols))
		}

		override toString() {
			return columns.toString
		}

		override equals(Object obj) {
			if (obj == null || !(obj instanceof Row)) {
				return false
			}
			return columns.equals((obj as Row).columns)
		}

		override hashCode() {
			return columns.hashCode
		}

	}

	def void getItemLooksLike(List<Row> rows, TreeItem item, int columnCount) {
		val row = new Row()
		if (columnCount > 0) {
			for (var i = 0; i < columnCount; i++) {
				row.columns.add(item.getText(i))
			}
		} else {
			row.columns.add(item.getText())
		}
		rows.add(row)
		for (child : item.items) {
			getItemLooksLike(rows, child, columnCount)
		}
	}

	def List<Row> getTableLooksLike() {
		val rows = new ArrayList<Row>();
		val table = getTable()
		if (table != null) {
			for (item : table.items) {
				val row = new Row()
				if (table.columnCount > 0) {
					for (var i = 0; i < table.columnCount; i++) {
						row.columns.add(item.getText(i))
					}
				} else {
					row.columns.add(item.getText())
				}
				rows.add(row)
			}
		} else {
			val tree = getTree()
			if (tree != null) {
				for (item : tree.items) {
					getItemLooksLike(rows, item, tree.columnCount);
				}
			} else {
				fail("Needs to be a tree or a table")
			}
		}
		return rows

	}

	def Table getTable() {
		// override method id there is a table to test against
		return null;
	}

	def Tree getTree() {
		// override method id there is a tree to test against
		return null;
	}

	def void assertLooksLike(Row... rows) {
		if (rows.length == 0) {
			assertThat(tableLooksLike, is(empty()))
		} else {
			assertThat(tableLooksLike, contains(rows))
		}
	}

	/**
	 * Convenience method for when only one column is in table
	 */
	def void assertLooksLike(String str0, String... strs) {
		val l = new ArrayList(Arrays.asList(strs))
		l.add(0, str0);
		val Row[] rows = l.stream().map([r|ROW(r)]).collect(Collectors.toList());
		assertThat(tableLooksLike, contains(rows))
	}

}
