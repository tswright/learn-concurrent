package org.tswright.learn.concurrent.striped;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A striped thread pool whose tasks are delegated to a specific thread by hashing a given key of type {@code K}.
 *
 * @author tswright
 */
public interface StripedThreadPool<K>
{
	boolean isShutdown();
	boolean isTerminated();
	void shutdown();
	boolean awaitTermination( long timeout, TimeUnit unit ) throws InterruptedException;
	List<Runnable> shutdownNow();
	CompletableFuture<Void> dispatch( K key, Runnable task );
}
