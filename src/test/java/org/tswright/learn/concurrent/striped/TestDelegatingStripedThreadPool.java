package org.tswright.learn.concurrent.striped;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.RandomStringUtils.random;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author tswright
 */
public class TestDelegatingStripedThreadPool
{
	private Set<String> orderIds;
	private FixedStripedThreadPool<String> dispatcher;
	private CyclicBarrier startingLine;
	private CountDownLatch finishLine;

	@BeforeTest
	public void setupStuff()
	{
		orderIds = IntStream.rangeClosed( 1, 1000 )
			.mapToObj( this::intToOrderId )
			.collect( toSet() );
		dispatcher = new FixedStripedThreadPool<>( 8 );
		startingLine = new CyclicBarrier( orderIds.size() );
		finishLine = new CountDownLatch( orderIds.size() );
	}

	@Test
	public void feedConcurrentOrdersToThreadPool()
	{
		asynchronouslyFillOrders();
		try
		{
			finishLine.await( 10, TimeUnit.MINUTES );
			dispatcher.shutdown();
			dispatcher.awaitTermination( 15, TimeUnit.SECONDS );
		}
		catch ( final InterruptedException e )
		{
			e.printStackTrace();
		}
	}

	private void asynchronouslyFillOrders()
	{
		orderIds.forEach( this::createOrderProducer );
	}

	private void createOrderProducer( final String orderId )
	{
		new OrderProducer( startingLine, finishLine, dispatcher, orderId ).start();
	}

	private String intToOrderId( final int unused )
	{
		return random( 13, true, true ).toUpperCase();
	}
}
