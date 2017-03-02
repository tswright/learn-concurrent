package org.tswright.learn.concurrent.striped;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.ThreadLocalRandom.current;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tswright
 */
class OrderProducer extends Thread
{
	private final CyclicBarrier startingLine;
	private final CountDownLatch finishLine;
	private final StripedThreadPool<String> executor;
	private final String orderId;
	private final Logger logger;

	OrderProducer( final CyclicBarrier startingLine, final CountDownLatch finishLine, final StripedThreadPool<String> executor, final String orderId )
	{
		this.startingLine = requireNonNull( startingLine );
		this.finishLine = requireNonNull( finishLine );
		this.executor = requireNonNull( executor );
		this.orderId = requireNonNull( orderId );
		logger = LoggerFactory.getLogger( getClass().getSimpleName() );
		setName( getClass().getSimpleName() + " - " + orderId );
	}

	@Override
	public void run()
	{
		try
		{
			startingLine.await();
			simulateOrder();
			finishLine.countDown();
		}
		catch ( InterruptedException | BrokenBarrierException e )
		{
			e.printStackTrace();
		}
	}

	private void simulateOrder()
	{
		sleepRandom();
		executor.dispatch( orderId, () -> logger.info( format( "%s --> orderId=%s, status=Sent", currentThread().getName(), orderId ) ) );
		sleepRandom();
		executor.dispatch( orderId, () -> logger.info( format( "%s --> orderId=%s, status=Acknowledged", currentThread().getName(), orderId ) ) );
		sleepRandom();
		executor.dispatch( orderId, () -> logger.info( format( "%s --> orderId=%s, status=Pending", currentThread().getName(), orderId ) ) );

		final int partials = current().nextInt() % 8;
		switch ( partials )
		{
			case 0:
				sleepRandom();
				executor.dispatch( orderId, () -> logger.info( format( "%s --> orderId=%s, status=Executed", currentThread().getName(), orderId ) ) );
				break;

			default:
				IntStream.range( 1, partials + 1 ).forEach( index -> fillPartial( orderId, index )  );
				sleepRandom();
				executor.dispatch( orderId, () -> logger.info( format( "%s --> orderId=%s, status=Complete", currentThread().getName(), orderId ) ) );
				break;
		}
	}

	private void fillPartial( final String orderId, final int index )
	{
		sleepRandom();
		executor.dispatch( orderId, () -> logger.info( format( "%s --> orderId=%s, status=Partial (%d)", currentThread().getName(), orderId, index ) ) );
	}

	private void sleepRandom()
	{
		try
		{
			Thread.sleep( current().nextInt( 0, 2000 ) );
		}
		catch ( final InterruptedException e )
		{
			logger.error( "I feel so tired; I just couldn't sleep!", e );
		}
	}
}
