package org.eclipse.cdt.internal.qt.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

import org.eclipse.cdt.internal.qt.core.QtPlugin;
import org.eclipse.cdt.internal.qt.core.build.IConsoleService;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class QtConsoleService implements IConsoleService, IResourceChangeListener {

	private MessageConsole console;
	private MessageConsoleStream out;
	private MessageConsoleStream err;

	private void initConsole() {
		console = new MessageConsole("Qt Builds", null);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
		out = console.newMessageStream();
		err = console.newMessageStream();

		// set the colors
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				// TODO use preferences so user can change the colors
				out.setColor(display.getSystemColor(SWT.COLOR_BLACK));
				err.setColor(display.getSystemColor(SWT.COLOR_RED));
			}
		});
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.PRE_BUILD);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		switch (event.getType()) {
		case IResourceChangeEvent.PRE_BUILD:
			if (event.getBuildKind() != IncrementalProjectBuilder.AUTO_BUILD) {
				// TODO this really should be done from the core and only when
				// our projects are being built
				console.clearConsole();
			}
			break;
		}
	}

	@Override
	public void monitor(final Process process) throws IOException {
		if (console == null) {
			initConsole();
		}

		console.activate();

		final CountDownLatch latch = new CountDownLatch(2);

		// Output stream reader
		new Thread("Qt Build Console Output") {
			public void run() {
				try (BufferedReader processOut = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					for (String line = processOut.readLine(); line != null; line = processOut.readLine()) {
						out.write(line);
						out.write('\n');
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			}
		}.start();

		// Error stream reader
		new Thread("Qt Build Console Error") {
			public void run() {
				try (BufferedReader processErr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
					for (String line = processErr.readLine(); line != null; line = processErr.readLine()) {
						err.write(line);
						out.write('\n');
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			}
		}.start();

		try {
			latch.await();
			process.waitFor();
		} catch (InterruptedException e) {
			QtPlugin.log(e);
		}
	}

	@Override
	public void writeOutput(String msg) throws IOException {
		if (out == null) {
			initConsole();
		}
		out.write(msg);
	}

	@Override
	public void writeError(String msg) throws IOException {
		if (err == null) {
			initConsole();
		}
		err.write(msg);
	}

}
