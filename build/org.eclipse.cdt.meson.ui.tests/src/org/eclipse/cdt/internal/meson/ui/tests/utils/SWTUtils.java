/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui.tests.utils;

import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.UIJob;
import org.junit.Assert;
import org.junit.ComparisonFailure;

/**
 * Utility class for SWT
 */
public class SWTUtils {

	/**
	 * Calls <strong>synchronously</strong> the given {@link Supplier} in the
	 * default Display and returns the result
	 *
	 * @param supplier
	 *            the Supplier to call
	 * @return the supplier's result
	 */
	public static <V> V syncExec(final Supplier<V> supplier) {
		final Queue<V> result = new ArrayBlockingQueue<>(1);
		Display.getDefault().syncExec(() -> result.add(supplier.get()));
		return result.poll();
	}

	/**
	 * Executes <strong>synchronously</strong> the given {@link Runnable} in the
	 * default Display
	 *
	 * @param runnable
	 *            the {@link Runnable} to execute
	 */
	public static void syncExec(final Runnable runnable) {
		Display.getDefault().syncExec(runnable);
	}

	/**
	 * Executes <strong>synchronously</strong> the given {@link Runnable} in the
	 * default Display. The given {@link Runnable} is ran into a rapping
	 * {@link Runnable} that will catch the {@link ComparisonFailure} that may
	 * be raised during an assertion.
	 *
	 * @param runnable
	 *            the {@link Runnable} to execute
	 * @throws ComparisonFailure
	 *             if an assertion failed.
	 * @throws SWTException
	 *             if an {@link SWTException} occurred
	 */
	public static void syncAssert(final Runnable runnable) throws SWTException, ComparisonFailure {
		final Queue<ComparisonFailure> failure = new ArrayBlockingQueue<>(1);
		final Queue<SWTException> swtException = new ArrayBlockingQueue<>(1);
		Display.getDefault().syncExec(() -> {
			try {
				runnable.run();
			} catch (ComparisonFailure e1) {
				failure.add(e1);
			} catch (SWTException e2) {
				swtException.add(e2);
			}
		});
		if (!failure.isEmpty()) {
			throw failure.poll();
		}
		if (!swtException.isEmpty()) {
			throw swtException.poll();
		}
	}

	/**
	 * Executes the given {@link Runnable} <strong>asynchronously</strong> in
	 * the default {@link Display} and waits until all jobs are done before
	 * completing.
	 *
	 * @param runnable
	 * @throws InterruptedException
	 */
	public static void asyncExec(final Runnable runnable) {
		asyncExec(runnable, true);
	}

	/**
	 * Executes the given {@link Runnable} <strong>asynchronously</strong> in
	 * the default {@link Display} and waits until all jobs are done before
	 * completing.
	 *
	 * @param runnable
	 *            the {@link Runnable} to execute
	 * @param waitForJobsToComplete
	 *            boolean flag to indicate if the method should wait for all
	 *            jobs to complete before finishing
	 * @throws InterruptedException
	 */
	public static void asyncExec(final Runnable runnable, final boolean waitForJobsToComplete) {
		final Queue<ComparisonFailure> failure = new ArrayBlockingQueue<>(1);
		final Queue<SWTException> swtException = new ArrayBlockingQueue<>(1);
		Display.getDefault().asyncExec(() -> {
			try {
				runnable.run();
			} catch (ComparisonFailure e1) {
				failure.add(e1);
			} catch (SWTException e2) {
				swtException.add(e2);
			}
		});
		if (waitForJobsToComplete) {
			waitForJobsToComplete();
		}
		if (!failure.isEmpty()) {
			throw failure.poll();
		}
		if (!swtException.isEmpty()) {
			throw swtException.poll();
		}
	}

	/**
	 * Waits for all {@link Job} to complete.
	 *
	 * @throws InterruptedException
	 */
	public static void waitForJobsToComplete() {
		wait(1, TimeUnit.SECONDS);
		while (!Job.getJobManager().isIdle()) {
			wait(1, TimeUnit.SECONDS);
		}
	}

	/**
	 * @param viewBot
	 *            the {@link SWTBotView} containing the {@link Tree} to traverse
	 * @param paths
	 *            the node path in the {@link SWTBotTree} associated with the
	 *            given {@link SWTBotView}
	 * @return the first {@link SWTBotTreeItem} matching the given node names
	 */
	public static SWTBotTreeItem getTreeItem(final SWTBotView viewBot, final String... paths) {
		final SWTBotTree tree = viewBot.bot().tree();
		return getTreeItem(tree.getAllItems(), paths);
	}

	/**
	 *
	 * @param parentTreeItem
	 *            the parent tree item from which to start
	 * @param paths
	 *            the relative path to the item to return
	 * @return the {@link SWTBotTreeItem} that matches the given path from the
	 *         given parent tree item
	 */
	public static SWTBotTreeItem getTreeItem(final SWTBotTreeItem parentTreeItem, final String... paths) {
		if (paths.length == 1) {
			return getTreeItem(parentTreeItem, paths[0]);
		}
		final String[] remainingPaths = new String[paths.length - 1];
		System.arraycopy(paths, 1, remainingPaths, 0, paths.length - 1);
		return getTreeItem(getTreeItem(parentTreeItem, paths[0]), remainingPaths);
	}

	/**
	 * Returns the first child node in the given parent tree item whose text
	 * matches (ie, begins with) the given path argument.
	 *
	 * @param parentTree
	 *            the parent tree item
	 * @param path
	 *            the text of the node that should match
	 * @return the first matching node or <code>null</code> if none could be
	 *         found
	 */
	public static SWTBotTreeItem getTreeItem(final SWTBotTree parentTree, final String path) {
		for (SWTBotTreeItem child : parentTree.getAllItems()) {
			if (child.getText().startsWith(path)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Returns the first child node in the given parent tree item whose text
	 * matches (ie, begins with) the given path argument.
	 *
	 * @param parentTreeItem
	 *            the parent tree item
	 * @param path
	 *            the text of the node that should match
	 * @return the first matching node or <code>null</code> if none could be
	 *         found
	 */
	public static SWTBotTreeItem getTreeItem(final SWTBotTreeItem parentTreeItem, final String path) {
		for (SWTBotTreeItem child : parentTreeItem.getItems()) {
			if (child.getText().startsWith(path)) {
				return child;
			}
		}
		return null;
	}

	private static SWTBotTreeItem getTreeItem(final SWTBotTreeItem[] treeItems, final String[] paths) {
		final SWTBotTreeItem swtBotTreeItem = Stream.of(treeItems).filter(item -> item.getText().startsWith(paths[0]))
				.findFirst().orElseThrow(() -> new RuntimeException("Only available items: "
						+ Stream.of(treeItems).map(item -> item.getText()).collect(Collectors.joining(", "))));
		if (paths.length > 1) {
			syncExec(() -> swtBotTreeItem.expand());
			final String[] remainingPath = new String[paths.length - 1];
			System.arraycopy(paths, 1, remainingPath, 0, remainingPath.length);
			return getTreeItem(swtBotTreeItem.getItems(), remainingPath);
		}
		return swtBotTreeItem;
	}

	public static SWTBotTableItem getListItem(final SWTBotTable table, final String name) {
		return Stream.iterate(0, i -> i + 1).limit(table.rowCount()).map(rowNumber -> table.getTableItem(rowNumber))
				.filter(rowItem -> {
					return Stream.iterate(0, j -> j + 1).limit(table.columnCount())
							.map(colNum -> rowItem.getText(colNum)).anyMatch(colValue -> colValue.contains(name));
				}).findFirst().orElse(null);
	}

	/**
	 * Waits for the given duration
	 *
	 * @param duration
	 *            the duration
	 * @param unit
	 *            the duration unit
	 */
	public static void wait(final int duration, final TimeUnit unit) {
		try {
			Thread.sleep(unit.toMillis(duration));
		} catch (InterruptedException e) {
			fail("Failed to wait for a " + unit.toMillis(duration) + "ms", e);
		}
	}

	/**
	 * Selects <strong> all child items</strong> in the given
	 * <code>parentTreeItem</code> whose labels match the given
	 * <code>items</code>.
	 *
	 * @param parentTreeItem
	 *            the parent tree item
	 * @param matchItems
	 *            the items to select
	 * @return
	 */
	public static SWTBotTreeItem select(final SWTBotTreeItem parentTreeItem, final String... matchItems) {
		final List<String> fullyQualifiedItems = Stream.of(parentTreeItem.getItems()).filter(
				treeItem -> Stream.of(matchItems).anyMatch(matchItem -> treeItem.getText().startsWith(matchItem)))
				.map(item -> item.getText()).collect(Collectors.toList());
		return parentTreeItem.select(fullyQualifiedItems.toArray(new String[0]));
	}

	/**
	 * Selects <strong> all child items</strong> in the given
	 * <code>parentTreeItem</code> whose labels match the given
	 * <code>items</code>.
	 *
	 * @param parentTree
	 *            the parent tree
	 * @param matchItems
	 *            the items to select
	 * @return
	 */
	public static SWTBotTree select(final SWTBotTree parentTree, final String... matchItems) {
		final List<String> fullyQualifiedItems = Stream.of(parentTree.getAllItems()).filter(
				treeItem -> Stream.of(matchItems).anyMatch(matchItem -> treeItem.getText().startsWith(matchItem)))
				.map(item -> item.getText()).collect(Collectors.toList());
		return parentTree.select(fullyQualifiedItems.toArray(new String[0]));
	}

	/**
	 * Selects the given <code>treeItem</code> whose labels match the given
	 * <code>items</code>.
	 *
	 * @param treeItem
	 *            the parent tree item
	 * @param matchItems
	 *            the items to select
	 */
	public static void select(SWTBotTreeItem treeItem) {
		treeItem.select();
	}

	/**
	 * @param tree
	 *            the root {@link SWTBotTree}
	 * @param path
	 *            the path for the menu
	 * @return the child {@link SWTBotMenu} named with the first item in the
	 *         given <code>path</code> from the given {@link SWTBotTree}
	 */
	public static SWTBotMenu getContextMenu(final SWTBotTree tree, String... path) {
		final SWTBotMenu contextMenu = tree.contextMenu(path[0]);
		if (contextMenu == null) {
			Assert.fail("Failed to find context menu '" + path[0] + "'.");
		}
		if (path.length == 1) {
			return contextMenu;
		}
		final String[] remainingPath = new String[path.length - 1];
		System.arraycopy(path, 1, remainingPath, 0, remainingPath.length);
		return getSubMenu(contextMenu, remainingPath);
	}

	/**
	 * Hides the menu for the given <code>tree</code>
	 *
	 * @param tree
	 *            the tree whose {@link Menu} should be hidden
	 */
	public static void hideMenu(final SWTBotTree tree) {
		try {
			final Menu menu = UIThreadRunnable.syncExec((Result<Menu>) () -> tree.widget.getMenu());
			UIThreadRunnable.syncExec(new VoidResult() {

				@Override
				public void run() {
					hide(menu);
				}

				private void hide(final Menu menu) {
					menu.notifyListeners(SWT.Hide, new Event());
					if (menu.getParentMenu() != null) {
						hide(menu.getParentMenu());
					}
				}
			});
		} catch (WidgetNotFoundException e) {
			// ignore if widget is not found, that's probably because there's no
			// tree in the
			// Docker Explorer view for the test that just ran.
		}
	}

	/**
	 * @param menu
	 *            the parent menu
	 * @param path
	 *            the path for the menu
	 * @return the child {@link SWTBotMenu} named with the first item in the
	 *         given <code>path</code> from the given {@link SWTBotMenu}
	 */
	public static SWTBotMenu getSubMenu(final SWTBotMenu menu, String... path) {
		final SWTBotMenu subMenu = menu.menu(path[0]);
		if (subMenu == null) {
			Assert.fail("Failed to find submenu '" + path[0] + "'.");
		}
		if (path.length == 1) {
			return subMenu;
		}
		final String[] remainingPath = new String[path.length - 1];
		System.arraycopy(path, 1, remainingPath, 0, remainingPath.length);
		return getSubMenu(subMenu, remainingPath);
	}

	public static SWTBotTreeItem expand(final SWTBotTree tree, final String... paths) {
		final SWTBotTreeItem rootItem = getTreeItem(tree, paths[0]);
		expandTreeItem(rootItem);
		if (paths.length > 1) {
			final String[] remainingPath = new String[paths.length - 1];
			System.arraycopy(paths, 1, remainingPath, 0, remainingPath.length);
			return expand(rootItem, remainingPath);
		}
		return rootItem;
	}

	public static SWTBotTreeItem expand(final SWTBotTreeItem treeItem, final String... paths) {
		final SWTBotTreeItem childItem = getTreeItem(treeItem, paths[0]);
		expandTreeItem(childItem);
		if (paths.length > 1) {
			final String[] remainingPath = new String[paths.length - 1];
			System.arraycopy(paths, 1, remainingPath, 0, remainingPath.length);
			return expand(childItem, remainingPath);
		}
		return getTreeItem(treeItem, paths[0]);
	}

	private static SWTBotTreeItem expandTreeItem(final SWTBotTreeItem treeItem) {
		final UIJob expandJob = new UIJob("expanding tree") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				treeItem.expand();
				return Status.OK_STATUS;
			}
		};
		expandJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				final int maxAttempts = 30;
				int currentAttempt = 0;
				while (currentAttempt < maxAttempts && treeItem.getItems().length == 1
						&& treeItem.getItems()[0].getText().isEmpty()) {
					SWTUtils.wait(1, TimeUnit.SECONDS);
					currentAttempt++;
				}

			}
		});
		expandJob.schedule();
		SWTUtils.wait(1, TimeUnit.SECONDS);
		return treeItem;
	}

	public static SWTBotView getSWTBotView(final SWTWorkbenchBot bot, final String viewId) {
		return bot.views().stream().filter(v -> v.getViewReference().getId().equals(viewId)).findFirst().orElse(null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getView(final SWTWorkbenchBot bot, final String viewId) {
		return (T) getView(bot, viewId, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getView(final SWTWorkbenchBot bot, final String viewId, final boolean restore) {
		final SWTBotView viewBot = bot.viewById(viewId);
		viewBot.setFocus();
		return (T) viewBot.getReference().getView(restore);
	}

	/**
	 * @return <code>true</code> if the Console view is visible in the active
	 *         page, <code>false</code> otherwise.
	 * @throws InterruptedException
	 */
	public static boolean isConsoleViewVisible(final SWTWorkbenchBot bot) {
		return bot.views().stream()
				.anyMatch(v -> v.getViewReference().getId().equals(IConsoleConstants.ID_CONSOLE_VIEW));
	}

	public static SWTBotToolbarButton getConsoleToolbarButtonWithTooltipText(final SWTWorkbenchBot bot,
			final String tooltipText) {
		return bot.viewById(IConsoleConstants.ID_CONSOLE_VIEW).getToolbarButtons().stream()
				.filter(button -> button.getToolTipText().equals(tooltipText)).findFirst().get();
	}

	public static void closeView(final SWTWorkbenchBot bot, final String viewId) {
		bot.views().stream().filter(v -> v.getReference().getId().equals(viewId)).forEach(v -> v.close());
	}

	/**
	 * Creates a new {@link SWTBotMenu} from the context. This avoids some
	 * unexpected "Widget is disposed" errors.
	 *
	 * @param bot
	 *            the bot
	 * @param menuName
	 *            the name of the menu to find
	 * @return the context menu
	 * @see <a href=
	 *      "https://www.eclipse.org/forums/index.php?t=msg&th=11863&start=0&">Eclipse
	 *      forum</a>
	 */
	public static SWTBotMenu getContextMenu(final AbstractSWTBot<? extends Control> bot, final String menuName) {
		return new SWTBotMenu(ContextMenuHelper.contextMenu(bot, menuName));
	}

}
