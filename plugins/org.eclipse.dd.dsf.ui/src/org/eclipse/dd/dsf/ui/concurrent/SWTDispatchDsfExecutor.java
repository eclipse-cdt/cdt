/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.dd.dsf.ui.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.dd.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.swt.widgets.Display;

public class SWTDispatchDsfExecutor extends DefaultDsfExecutor 
{

	public SWTDispatchDsfExecutor()
	{
		super();
	}
	
	private <V> Callable<V> createSWTDispatchCallable(final Callable<V> callable)
	{
		return new Callable<V>()
		{
			public V call() throws Exception
			{
				final Object[] v = new Object[1];
				final Throwable[] e = new Throwable[1];
				
				Display.getDefault().syncExec(new Runnable()
				{
					public void run()
					{
						try
						{
							v[0] = callable.call();
						}
						catch(Throwable exception)
						{
							e[0] = exception;
						}
					}
				});
				
				if(e[0] instanceof RuntimeException)
					throw (RuntimeException) e[0];
				else if(e[0] instanceof Exception)
					throw (Exception) e[0];
				
				return (V) v[0];
			}
		};
	}
	
	private Runnable createSWTDispatchRunnable(final Runnable runnable)
	{
		return new Runnable()
		{
			public void run()
			{
				final Throwable[] e = new Throwable[1];
				
				Display.getDefault().syncExec(new Runnable()
				{
					public void run()
					{
						try
						{
							runnable.run();
						}
						catch(Throwable exception)
						{
							e[0] = exception;
						}
					}
				});
				
				if(e[0] instanceof RuntimeException)
					throw (RuntimeException) e[0];
			}
		};
	}
	
	@Override
	public <V> ScheduledFuture<V> schedule(final Callable<V> callable, long delay,
			TimeUnit unit) {
		return super.schedule(createSWTDispatchCallable(callable), delay, unit);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay,
			TimeUnit unit) {
		return super.schedule(createSWTDispatchRunnable(command), delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
			long initialDelay, long period, TimeUnit unit) {
		return super.scheduleAtFixedRate(createSWTDispatchRunnable(command), initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
			long initialDelay, long delay, TimeUnit unit) {
		return super.scheduleWithFixedDelay(createSWTDispatchRunnable(command), initialDelay, delay, unit);
	}

	@Override
	public void execute(Runnable command) {
		super.execute(createSWTDispatchRunnable(command));
	}

	@Override
	public <T> Future<T> submit(Callable<T> callable) {
		return super.submit(createSWTDispatchCallable(callable));
	}

	@Override
	public <T> Future<T> submit(Runnable command, T result) {
		return super.submit(createSWTDispatchRunnable(command), result);
	}

	@Override
	public Future<?> submit(Runnable command) {
		return super.submit(createSWTDispatchRunnable(command));
	}

}
