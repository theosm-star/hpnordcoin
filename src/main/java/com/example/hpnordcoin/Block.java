package com.example.hpnordcoin;

import java.util.Arrays;

public class Block {
    private int previousHash;
    private String[] data;
    private int blockHash;
    private int id;
    public Block( int id, int previousHash, String[] data ) {
        this.previousHash = previousHash;
        this.data = data;
        this.id=id;
        Object[] contents = {Arrays.hashCode(data), previousHash};
        this.blockHash = Arrays.hashCode(contents);
    }

    public int getPreviousHash() {
        return previousHash;
    }

    public String[] getData() {
        return data;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public int getBlockHash() {
        return blockHash;
    }
}

