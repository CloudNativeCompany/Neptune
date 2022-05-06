/*
 * Copyright (c) 2022 The Neptune Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neptune.transport.connect;


import org.neptune.transport.connect.ConnectionGroup;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * org.neptune.core.transportLayer - CowConnectionGroupList
 *
 * @author tony-is-coding
 * @date 2021/12/25 16:32
 */
public class CowConnectionGroupList {

    /*
    README:
        1. 考虑 读-写 并发问题
            方案一:  当前读, 性能会比较差; 不适合并发
            方案二:  快照读, 性能好点, 但是需要 [考虑快照数据失效保障问题]
        2. 状态性, 需要考虑负载均衡场景下
            加权能力
            轮询机制
     */
    private transient volatile ConnectionGroup[] array; // 通过 volatile 来保障 读-写并发问题
    final transient ReentrantLock lock = new ReentrantLock();


    public CowConnectionGroupList() {
        setArray(new ConnectionGroup[0]);
    }

    public int size() {
        return getArray().length;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public ConnectionGroup[] snapshot(){
        return getArray();
    }

    public boolean add(ConnectionGroup e) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            ConnectionGroup[] elements = getArray();
            int len = elements.length;
            ConnectionGroup[] newElements = Arrays.copyOf(elements, len + 1);
            newElements[len] = e;
            setArray(newElements);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("all")
    public boolean addIfAbsent(ConnectionGroup e) {
        ConnectionGroup[] snapshot = getArray();
        return indexOf(e, snapshot, 0, snapshot.length) >= 0 ? false :
                addIfAbsent(e, snapshot);
    }

    private boolean addIfAbsent(ConnectionGroup e, ConnectionGroup[] snapshot) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            ConnectionGroup[] current = getArray();
            int len = current.length;
            if (snapshot != current) {
                // Optimize for lost race to another addXXX operation
                int common = Math.min(snapshot.length, len);
                for (int i = 0; i < common; i++)
                    if (current[i] != snapshot[i] && eq(e, current[i]))
                        return false;
                if (indexOf(e, current, common, len) >= 0)
                    return false;
            }
            ConnectionGroup[] newElements = Arrays.copyOf(current, len + 1);
            newElements[len] = e;
            setArray(newElements);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void remove(int index) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            ConnectionGroup[] elements = getArray();
            int len = elements.length;
            int numMoved = len - index - 1;
            if (numMoved == 0)
                setArray(Arrays.copyOf(elements, len - 1));
            else {
                ConnectionGroup[] newElements = new ConnectionGroup[len - 1];
                System.arraycopy(elements, 0, newElements, 0, index);
                System.arraycopy(elements, index + 1, newElements, index,
                        numMoved);
                setArray(newElements);
            }
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("all")
    public boolean remove(ConnectionGroup o) {
        ConnectionGroup[] snapshot = getArray();
        int index = indexOf(o, snapshot, 0, snapshot.length);
        return (index < 0) ? false : remove(o, snapshot, index);
    }


    private boolean remove(ConnectionGroup o, ConnectionGroup[] snapshot, int index) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            ConnectionGroup[] current = getArray();
            int len = current.length;
            if (snapshot != current) findIndex:{
                int prefix = Math.min(index, len);
                for (int i = 0; i < prefix; i++) {
                    if (current[i] != snapshot[i] && eq(o, current[i])) {
                        index = i;
                        break findIndex;
                    }
                }
                if (index >= len)
                    return false;
                if (current[index] == o)
                    break findIndex;
                index = indexOf(o, current, index, len);
                if (index < 0)
                    return false;
            }
            ConnectionGroup[] newElements = new ConnectionGroup[len - 1];
            System.arraycopy(current, 0, newElements, 0, index);
            System.arraycopy(current, index + 1,
                    newElements, index,
                    len - index - 1);
            setArray(newElements);
            return true;
        } finally {
            lock.unlock();
        }
    }

    private static boolean eq(ConnectionGroup o1, ConnectionGroup o2) {
        return Objects.equals(o1, o2);
    }

    private static int indexOf(ConnectionGroup o, ConnectionGroup[] elements,
                               int index, int fence) {
        if (o == null) {
            for (int i = index; i < fence; i++)
                if (elements[i] == null)
                    return i;
        } else {
            for (int i = index; i < fence; i++)
                if (o.equals(elements[i]))
                    return i;
        }
        return -1;
    }


    private ConnectionGroup[] getArray() {
        return array;
    }

    private void setArray(ConnectionGroup[] a) {
        array = a;
    }


    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            setArray(new ConnectionGroup[0]);
        } finally {
            lock.unlock();
        }
    }
}
