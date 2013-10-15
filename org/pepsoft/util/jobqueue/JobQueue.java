/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.util.jobqueue;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A FIFO job queue where each job may only exist at most once in the queue.
 * What constitutes "the same job" is determined by the {@link #equals(java.lang.Object)}
 * and {@link #hashCode()} methods of the job itself, and is therefore the
 * responsibility of the programmer.
 *
 * @author pepijn
 */
public class JobQueue<T extends Job> {
    public synchronized T takeJob() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.remove(0);
    }
    
    /**
     * Adds a job to the front of the queue, but only if it is not on the
     * queue yet.
     * 
     * @param job The job to schedule.
     * @return <code>true</code> if the job was scheduled, <code>false</code> if
     *     it already was on the queue.
     */
    public synchronized boolean scheduleJobIfNotScheduled(T job) {
        if (! queue.contains(job)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Scheduling job " + job);
            }
            queue.add(job);
            notifyAll();
            return true;
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("NOT scheduling job " + job + " due to duplicate on queue");
            }
            return false;
        }
    }
    
    /**
     * Adds a job to the front of the queue. If the job was already on the queue
     * the existing instance is removed.
     * 
     * @param job The job to schedule.
     * @return <code>true</code> if the job already existed on the queue,
     *     <code>false</code> if it did not.
     */
    public synchronized boolean rescheduleJob(T job) {
        if (queue.contains(job)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Scheduling job " + job + ", replacing existing job");
            }
            queue.add(job);
            notifyAll();
            return true;
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Scheduling job " + job);
            }
            queue.add(job);
            notifyAll();
            return false;
        }
    }
    
    /**
     * Block until the queue is empty.
     */
    public synchronized void drain() throws InterruptedException {
        while (! queue.isEmpty()) {
            wait();
        }
    }
    
    private HashList<T> queue = new HashList<T>();
    
    private static final Logger logger = Logger.getLogger(JobQueue.class.getName());
}