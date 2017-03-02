package org.tswright.learn.concurrent.striped;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A striped thread pool that uses a {@link ConcurrentHashMap} for striping, and {@link CompletableFuture} as a queueing mechanism.
 *
 * @author tswright
 */
public class FixedStripedThreadPool<K> implements StripedThreadPool<K>
{
	private final ConcurrentMap<K, CompletableFuture<Void>> queueMap;
	private final ExecutorService delegate;

	public FixedStripedThreadPool( final int threadCount )
	{
		delegate = newFixedThreadPool( threadCount );
		queueMap = new ConcurrentHashMap<>( threadCount, 0.80f, threadCount );
	}

	@Override
	public boolean isShutdown()
	{
		return delegate.isShutdown();
	}

	@Override
	public boolean isTerminated()
	{
		return delegate.isTerminated();
	}

	@Override
	public void shutdown()
	{
		delegate.shutdown();
	}

	@Override
	public boolean awaitTermination( final long timeout, final TimeUnit unit ) throws InterruptedException
	{
		return delegate.awaitTermination( timeout, unit );
	}

	@Override
	public List<Runnable> shutdownNow()
	{
		return delegate.shutdownNow();
	}

	@Override
	public CompletableFuture<Void> dispatch( final K key, final Runnable task )
	{
		requireNonNull( key, "parameter 'key' must be non-null" );
		requireNonNull( task, "parameter 'task' must be non-null" );
		if ( isShutdown() )
		{
			throw new RejectedExecutionException( "the thread pool has been shut down" );
		}
		return queueMap.compute( key, ( k, value ) -> createOrQueue( value, task ) );
	}

	private CompletableFuture<Void> createOrQueue( final CompletableFuture<Void> value, final Runnable task )
	{
		return ( ( null == value ) ? runAsync( task, delegate ) : value.thenRunAsync( task, delegate ) );
	}
}
