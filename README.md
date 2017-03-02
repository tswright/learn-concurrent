# learn-concurrent
Included in this code is the `StripedThreadPool` interface and its implementation, `FixedStripedThreadPool`.  The intent is to provide
concurrency, but also to ensure that all tasks processed using the same key are processed in order.  Note that all tasks using the same
key are **not** guaranteed to use the same thread, just to be processed in the order in which they were submitted to the thread pool.
