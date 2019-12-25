package com.example.hpnordcoin;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.sql.*;
import java.sql.DriverManager;
import java.util.Base64;
import java.util.Scanner;

public class Ledger {
    //TODO /completed/ sql balance changing!- data added to blockchain!
    //TODO /stuff I know I have to do/ proof of work - GUI - get block to chain to run on boot up and not interfere at all- better signup @ account add
    //TODO /stuff I am thinking about/ google gucie? - hosting?(google sign in ?) - easyApp?
    CryptographyUtil generator = new CryptographyUtil();
    private ArrayList<Block> blockChain;
    private ArrayList<Wallet> Wallets;
    public Ledger() throws NoSuchProviderException, NoSuchAlgorithmException, SQLException {
        Wallets = new ArrayList<>();
        int searchID = 0;
        int id2 =0;
        int transactionAmount = 0;
        byte[] encryptedData = new byte[0];
        int deleteID = 0;
        String myUrl = "jdbc:mysql://localhost:3306/HPNordicCoin?useSSL=false&allowPublicKeyRetrieval=true";
        Connection conn = DriverManager.getConnection(myUrl, "root", "85yppazz");
        Scanner scan = new Scanner(System.in);
        int menuCase = scan.nextInt();
        switch(menuCase) {
            case 0:
                AddAccount(conn);
                break;
            case 1:
                System.out.println("ID1");
                searchID =scan.nextInt();
                System.out.println("Amount");
                transactionAmount = scan.nextInt();
                System.out.println("ID2");
                id2 = scan.nextInt();
                int[] transactionData = {searchID,id2,transactionAmount};
                SendTransaction(conn,searchID,transactionAmount,transactionData,id2, encryptedData);
                ReceiveTransaction(encryptedData, searchID, conn, transactionAmount, id2);
                //makeChain(encryptedData);
                break;
            case 2:
                System.out.println("Enter the ID of the account you would like to delete:");
                deleteID = scan.nextInt();
                deleteAccount(conn,deleteID);
        }

    }
    public void AddAccount( Connection conn ) throws NoSuchProviderException, NoSuchAlgorithmException {
        System.out.println("Account Added");
        KeyPair generateKeyPair = generator.generateKeyPair();
        byte[] publicKey = generateKeyPair.getPublic().getEncoded();
        byte[] privateKey = generateKeyPair.getPrivate().getEncoded();
        Wallets.add(new Wallet(Wallets.size(),publicKey,privateKey,1000));
        int walletId = Wallets.size()-1;
        int count = 0;
        try {

            String query = " INSERT INTO `userTable` (`userID`, `privateKey`, `publicKey`, `balance`)"
                    + " VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery("SELECT COUNT(*) FROM "+"userTable");
            while (res.next()){
                count = res.getInt(1);
            }
            System.out.println(count);
            preparedStmt.setInt(1,count);
            preparedStmt.setBytes (2, Wallets.get(walletId).getPrivateKey());
            preparedStmt.setBytes (3,Wallets.get(walletId).getPublicKey());
            preparedStmt.setInt(4,Wallets.get(walletId).getBalance());
            preparedStmt.execute();

            conn.close();

            preparedStmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
    public void deleteAccount (Connection conn, int deleteID) throws SQLException {
        try {
            System.out.println("Deleting.....");
            String sqlDelete = "DELETE FROM usertable WHERE userID = ?";
            PreparedStatement dStmt = conn.prepareStatement(sqlDelete);
            dStmt.setInt(1,deleteID);
            dStmt.executeUpdate();
            System.out.println("Account " + deleteID + " has been deleted.");
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
    public byte[] SendTransaction( Connection conn, int searchID, int transactionAmount, int[] transactionData, int id2, byte[] encryptedData ) throws SQLException {
        String query = "SELECT * FROM usertable WHERE userID = " + searchID;
        // create the java statement
        Statement st = null;
        try {
            st = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ResultSet rs = st.executeQuery(query);
        rs.first();
        byte[] PublicKey1 = rs.getBytes("publicKey");
        byte[] PrivateKey1 = rs.getBytes("privateKey");
        String data = transactionData[0] + " " + transactionData[1] + " " + transactionData[2] + " ";
        try {
            encryptedData = generator.encrypt(PublicKey1,
                    data.getBytes());
            byte[] decyptedData = generator.decrypt(PrivateKey1,encryptedData);
            System.out.println(new String(encryptedData));
            System.out.println(new String(decyptedData));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return encryptedData;
    }
    public void ReceiveTransaction( byte[] encryptedData, int searchID, Connection conn, int transactionAmount, int id2) throws SQLException {
        String query = "SELECT * FROM usertable WHERE userID = " + searchID;
        String query1 = "SELECT * FROM usertable WHERE userID = " + id2;
        Statement st = null;
        try {
            st = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ResultSet rs = st.executeQuery(query);
        rs.first();
        int newBalance1 = rs.getInt("balance");
        if (newBalance1-transactionAmount >= 0){
            newBalance1 = newBalance1 - transactionAmount;
        }
        else {transactionAmount=0;}
        ResultSet rs1 = st.executeQuery(query1);
        rs1.first();
        int newBalance2 = rs1.getInt("balance")+transactionAmount;
        String query2 = "UPDATE usertable SET balance = ?  WHERE userID = ?";
        PreparedStatement myStmt = conn.prepareStatement(query2);
        myStmt.setInt(1, newBalance1);
        myStmt.setInt(2,searchID);
        myStmt.executeUpdate();
        String query3 =  "UPDATE usertable SET balance= ? WHERE userID = ?";
        PreparedStatement myStmt2 = conn.prepareStatement(query3);
        myStmt2.setInt(1, newBalance2);
        myStmt2.setInt(2, id2);
        myStmt2.executeUpdate();
        System.out.println(transactionAmount + " HpNordCoins have been transferred from account " + searchID + " balance(" + newBalance1 + ") to account " + id2 + " balance(" + newBalance2 + ")");
    }
    public void makeChain(byte[] encyptedData) {
        int count = 0;
        blockChain = new ArrayList<>();
        String[] genesisData = {"This is the first block!"};
        // FIXME -pass in transactionData[] to blockchain
        Block genesisBlock = new Block (0,0,genesisData);
        blockChain.add(genesisBlock);
        int currentMin = LocalDateTime.now().getMinute();
        while (true) {
            if (currentMin != LocalDateTime.now().getMinute()) {
                String[] tempData = {"No activity"};
                if (encyptedData != null) {
                    tempData = new String[]{new String(encyptedData)};
                }
                count++;
                blockChain.add(new Block(count, blockChain.get(blockChain.size() - 1).getBlockHash(), genesisData));

                if (count > 1) {
                    break;
                }
            }
        }
        printChain();
    }
    public void printChain() {
        for (Block b :
                blockChain) {
            System.out.println(b.getBlockHash());
        }
    }
    public static void main( String[] args ) throws NoSuchProviderException, NoSuchAlgorithmException, SQLException {
        new Ledger();
    }
}

