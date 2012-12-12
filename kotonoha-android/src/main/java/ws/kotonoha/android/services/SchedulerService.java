/*
 * Copyright 2012 eiennohito
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ws.kotonoha.android.services;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author eiennohito
 * @since 11.02.12
 */
public class SchedulerService {
  private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
  private ThreadPoolExecutor executor;

  public SchedulerService(int minThreads, int maxThreads) {
    executor = new ThreadPoolExecutor(minThreads, maxThreads, 5, MINUTES, queue);
  }

  public Future<?> schedule(Runnable r) {
    return executor.submit(r);
  }

  public <T> Future<T> schedule(Callable<T> callable) {
    return executor.submit(callable);
  }

  public void shutdown() {
    executor.shutdown();
  }
}
