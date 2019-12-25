package com.example.hpnordcoin;

public class Wallet {

    byte[] publicKey;
    byte[] privateKey;
    private int balance = 0;
    private int userId = 0;

    public Wallet(int userId, byte[] publicKey, byte[] privateKey, int balance ) {
        this.userId = userId;
        this.balance = balance;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }


    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public int getBalance() {
        return balance;
    }
}
