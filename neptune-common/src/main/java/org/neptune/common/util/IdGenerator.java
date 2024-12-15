package org.neptune.common.util;

public class IdGenerator {

    final static LongSequence sequence = new LongSequence(System.currentTimeMillis(), 2);
    public static long newId(){
        return sequence.next();
    }
}
