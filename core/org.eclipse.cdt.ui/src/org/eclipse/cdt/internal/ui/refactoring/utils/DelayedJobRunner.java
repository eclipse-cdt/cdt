package org.eclipse.cdt.internal.ui.refactoring.utils;


/**
 * Runs a job delayed by a given time when <code>runJob</code> gets called. <br>
 * Only the time of the last <code>runJob</code> call is considered. So if  <code>runJob</code>
 * gets called before the time expires the first call will be ignored and the job will be run
 * after the time of the second call + the delay time.<br>
 * <code>DelayedJobRunner</code> isn't thread save (not neede till now).<br>
 * Oh and one more thing. Do call the stop method when the runner isn't needed any more. ;-)
 * 
 * @author Lukas Felber
 *
 */
public class DelayedJobRunner {
	private Runnable job;
	private long lastUpdateEventTime;
	private long delayTimeInMillis;
	private boolean shouldStop;
	private boolean shouldUpdate;
	private static final long sleepTimeInMillis = 50;

	public DelayedJobRunner(Runnable job, long delayTimeInMillis) {
		this.job = job;
		this.delayTimeInMillis = delayTimeInMillis;
		shouldUpdate = true;
	}
	
	public void start() {
		shouldStop = false;
		new Thread(
			new Runnable() {
				public void run() {
					startUpdateLoop();
				}
			}	
		).start();
	}
	
	public void runJob() {
		shouldUpdate = true;
		lastUpdateEventTime = System.currentTimeMillis();
	}

	private void startUpdateLoop() {
		try {
			while (!shouldStop) {
				if (shouldUpdate && isDelayTimeOver()) {
					lastUpdateEventTime = System.currentTimeMillis();
					shouldUpdate = false;
					job.run();
				}
				Thread.sleep(sleepTimeInMillis);
			}
		} catch (Exception e) {
			//do nothing expect die.
		}
	}
	
	private boolean isDelayTimeOver() {
		long currentTime = System.currentTimeMillis();
		return lastUpdateEventTime + delayTimeInMillis < currentTime;
	}

	public void stop() {
		this.shouldStop = true;
	}
}
