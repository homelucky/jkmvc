package net.jkcode.jkmvc.bit;

import java.util.AbstractCollection;
import java.util.BitSet;
import java.util.Iterator;

/**
 * BitSet相关集合
 * @author shijianhang
 * @date 2019-06-27 11:57 AM
 */
public abstract class IBitCollection<E> extends AbstractCollection<E> implements IBitElementOperator<E> {

    protected BitSet bits;

    public IBitCollection(BitSet bits) {
        this.bits = bits;
    }

    /**
     * 获得BitSet中设置为 true 的位数
     * @return
     */
    @Override
    public int size() {
        return bits.cardinality();
    }

    /**
     * 获得迭代器
     * @return
     */
    @Override
    public Iterator<E> iterator() {
        return new BitElementIterator(bits,this);
    }
};