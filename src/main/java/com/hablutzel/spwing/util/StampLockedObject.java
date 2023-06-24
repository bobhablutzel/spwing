/*
 * Copyright © 2023, Hablutzel Consulting, LLC.
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

package com.hablutzel.spwing.util;

import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class StampLockedObject<T> {

    private T t = null;
    private final StampedLock lock = new StampedLock();


    public void withRead( Consumer<T> reader ) {
        long stamp = lock.readLock();
        try {
            reader.accept(t);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public <R> R withRead(Function<T,R> readerWithResult ) {
        long stamp = lock.readLock();
        try {
            return readerWithResult.apply(t);
        } finally {
            lock.unlockRead(stamp);
        }

    }

    public void withOptimisticRead( Consumer<T> reader ) {
        long stamp = lock.tryOptimisticRead();
        reader.accept(t);
        if(!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                reader.accept(t);
            } finally {
                lock.unlock(stamp);
            }
        }
    }

    public <R> R withOptimisticRead( Function<T,R> readerWithResult ) {
        long stamp = lock.tryOptimisticRead();
        R result = readerWithResult.apply(t);
        if(!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                return readerWithResult.apply(t);
            } finally {
                lock.unlock(stamp);
            }
        }
        return result;
    }


    public void put( Supplier<T> supplier ) {
        long stamp = lock.writeLock();
        try {
            t = supplier.get();
        } finally {
            lock.unlockWrite(stamp);
        }
    }


    public void replace( Function<T, T> map ) {
        long stamp = lock.writeLock();
        try {
            t = map.apply(t);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

}
