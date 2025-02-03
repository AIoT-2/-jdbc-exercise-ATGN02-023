package com.nhnacademy.jdbc.bank.repository.impl;

import com.nhnacademy.jdbc.bank.domain.Account;
import com.nhnacademy.jdbc.bank.repository.AccountRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AccountRepositoryImpl implements AccountRepository {

    public Optional<Account> findByAccountNumber(Connection connection, long accountNumber){
        //todo#1 계좌-조회
        String sql = "select account_number, name, balance from jdbc_accounts where account_number = ? ";
        ResultSet rs = null;
        try(PreparedStatement psmt = connection.prepareStatement(sql)){
            psmt.setLong(1, accountNumber);
            rs = psmt.executeQuery();
            if(rs.next()){
                return Optional.of(new Account(rs.getLong("account_number"),rs.getString("name"),rs.getLong("balance")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public int save(Connection connection, Account account) {
        //todo#2 계좌-등록, executeUpdate() 결과를 반환 합니다.
        String sql = "insert into jdbc_account (account_number, name, balance) values(?,?,?)";

        try (PreparedStatement psmt = connection.prepareStatement(sql)){
            psmt.setLong(1,account.getAccountNumber());
            psmt.setString(2, account.getName());
            psmt.setLong(3,account.getBalance());
            return psmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public int countByAccountNumber(Connection connection, long accountNumber){
        int count=0;
        //todo#3 select count(*)를 이용해서 계좌의 개수를 count해서 반환
        String sql = "Select count(*) as cnt from jdbc_account where account_number=?";
        ResultSet rs = null;

        try (PreparedStatement psmt = connection.prepareStatement(sql)){
            psmt.setLong(1, accountNumber);
            rs = psmt.executeQuery();
            if(rs.next()){
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return count;
    }

    @Override
    public int deposit(Connection connection, long accountNumber, long amount){
        //todo#4 입금, executeUpdate() 결과를 반환 합니다.
        String sql = "update jdbc_account set balance=balance+? where account_number=? ";
        try(PreparedStatement psmt = connection.prepareStatement(sql)){
            psmt.setLong(1, amount);
            psmt.setLong(2, accountNumber);
            int result = psmt.executeUpdate();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int withdraw(Connection connection, long accountNumber, long amount){
        //todo#5 출금, executeUpdate() 결과를 반환 합니다.
        String sql = "update jdbc_account set balance=balance-? where account_number=?";
        try(PreparedStatement psmt = connection.prepareStatement(sql)){
            psmt.setLong(1, amount);
            psmt.setLong(2,accountNumber);
            int result = psmt.executeUpdate();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int deleteByAccountNumber(Connection connection, long accountNumber) {
        //todo#6 계좌 삭제, executeUpdate() 결과를 반환 합니다.
        String sql = "delete from jdbc_accounts where account_number = ?";
        try(PreparedStatement psmt = connection.prepareStatement(sql)){
            psmt.setLong(1, accountNumber);
            int result = psmt.executeUpdate();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
