package org.neptune.transport.connection;

import org.neptune.common.UnresolvedAddress;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * @desc TODO
 * 考虑批量connect?
 * @author tony
 * @createDate 2024/4/20 6:58 下午
 */
public class ConnectionGroup {

    public static final int CONNECT_NUM = 1;

    private final UnresolvedAddress address;
    private transient final CopyOnWriteArrayList<Connection> connections = new CopyOnWriteArrayList<>(); // 通过 volatile 来保障 读-写并发问题
    final transient ReentrantLock lock = new ReentrantLock();

    public ConnectionGroup(UnresolvedAddress address){
        this.address = address;
    }
    public  void addConnect(Connection connection){
        lock.lock();
        connections.add(connection);
        lock.unlock();
    }

    public void addConnect(Supplier<Connection> connectionSupplier){
        if(!isFull()){
            synchronized (connections){
                if(!isFull()){
                    Connection connection = connectionSupplier.get();
                    connections.add(connection);
                }
            }
        }
    }

    public List<Connection> connections(){
        return connections;
    }


    public UnresolvedAddress address(){
        return address;
    }

    public boolean isAvailable(){
        return !connections.isEmpty();
    }

    public boolean isFull(){
        return connections.size() == CONNECT_NUM;
    }

    public Connection next(){
        if(!isAvailable()){
            throw new RuntimeException("no connection availabele");
        }
        return connections.get(0);
    };
}
