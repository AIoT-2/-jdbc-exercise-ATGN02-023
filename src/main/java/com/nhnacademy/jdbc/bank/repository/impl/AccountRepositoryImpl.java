package com.nhnacademy.jdbc.bank.repository.impl;

import com.nhnacademy.jdbc.bank.domain.Account;
import com.nhnacademy.jdbc.bank.repository.AccountRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;


//AccountRepository 인터페이스를 구현하여 은행계좌와 관련된 CRUD 작업을 수행하는 JDBC 기반 저장소.
// 각 메서드는 Connection 객체를 매개변수로 받아 , 데이터베이스와 연결된 상태에서 SQL 쿼리 실행.
public class AccountRepositoryImpl implements AccountRepository {

    public Optional<Account> findByAccountNumber(Connection connection, long accountNumber){
        //todo#1 계좌-조회
        String sql = "select account_number, name, balance from jdbc_account where account_number = ? ";
        ResultSet rs = null; //ResultSet을 통해 결과를 가져온 후, 계좌 객체를 생성하여 Optional<Account>로 반환.
        try(PreparedStatement psmt = connection.prepareStatement(sql)){ // sql 쿼리를 실행할 준비를 하는 PreparedStatement 객체 생성
            psmt.setLong(1, accountNumber); //sql 구문의 첫번째 ?에 accountNumber를 넣음.
            rs = psmt.executeQuery(); // 삽입 성공 여부를 반환
            if(rs.next()){ //rs가 존재하면
                return Optional.of(new Account(
                        rs.getLong("account_number"), //account_number 컬럼 가져오기
                        rs.getString("name"), //name 컬럼 가져오기
                        rs.getLong("balance") //balance 컬럼 가져오기
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty(); //계좌가 존재하지 않으면 Optional.empty() 반환.
    }



    @Override
    public int save(Connection connection, Account account) {

        //todo#2 계좌-등록, executeUpdate() 결과를 반환 합니다.
        String sql = "insert into jdbc_account (account_number, name, balance) values(?,?,?)";

        try (PreparedStatement psmt = connection.prepareStatement(sql)){ // sql 쿼리를 실행할 준비를 하는 PreparedStatement 객체 생성
            psmt.setLong(1,account.getAccountNumber()); // sql의 첫번째 ?에 account_number 넣기
            psmt.setString(2, account.getName()); //name
            psmt.setLong(3,account.getBalance()); //balance
            return psmt.executeUpdate(); //삽입 성공 여부를 반환.
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
            // sql 쿼리를 실행할 준비를 하는 PreparedStatement 객체 생성
            psmt.setLong(1, accountNumber); //첫번째 ?에 accountNumber 값을 넣음.
            rs = psmt.executeQuery(); //executeQuery(): SELECT문 실행 후 결과 ResultSet 반환
            if(rs.next()){ //데이터가 있으면 rs.next()로 조회 가능.
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
        String sql = "delete from jdbc_account where account_number = ?";
        try(PreparedStatement psmt = connection.prepareStatement(sql)){
            psmt.setLong(1, accountNumber);
            int result = psmt.executeUpdate();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
