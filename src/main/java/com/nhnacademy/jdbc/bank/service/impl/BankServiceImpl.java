package com.nhnacademy.jdbc.bank.service.impl;

import com.nhnacademy.jdbc.bank.domain.Account;
import com.nhnacademy.jdbc.bank.exception.AccountAreadyExistException;
import com.nhnacademy.jdbc.bank.exception.AccountNotFoundException;
import com.nhnacademy.jdbc.bank.exception.BalanceNotEnoughException;
import com.nhnacademy.jdbc.bank.repository.AccountRepository;
import com.nhnacademy.jdbc.bank.repository.impl.AccountRepositoryImpl;
import com.nhnacademy.jdbc.bank.service.BankService;

import java.sql.Connection;
import java.util.Optional;

// 은행 서비스에 관련된 기능을 제공하는 클래스.
public class BankServiceImpl implements BankService {

    private final AccountRepository accountRepository;
    // 각 메서드는 AccountRepository를 사용하여 실제 데이터베이스에 접근.
    // 필요한 예외 처리를 수행합니다.

    //accountRepository를 필드로 선언하고, 생성자에서 해당 객체를 주입받습니다.
    public BankServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    // AccountRepository는 실제 데이터베이스와 상호작용하는
    // findByAccountNumber, save, deposit, withdraw, countByAccountNumber, deleteByAccountNumber 등의 메서드를 제공하는 레포지토리.

    @Override
    public Account getAccount(Connection connection, long accountNumber){
        //todo#11 계좌-조회
        // 주어진 계좌번호에 해당하는 계좌 정보를 조회합니다.

        //Optional은 값이 있을 수도, 없을 수도 있다는 것을 명시. -> 값이 존재하지 않을 가능성을 보여줌.
        // Optional을 사용하면 null 값에 대해 직접적으로 체크하지 않고도 안전하게 작업을 처리 가능.
        //if(value != null)와 같은 체크 대신해서 Optional 메서드 사용하여 작업 가능.
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(connection, accountNumber);
        if(accountOptional.isEmpty()){ //isEpty()메서드를 사용하여 값이 비어있는지 체크하고,
            throw new AccountNotFoundException(accountNumber); // 비어있을 경우 예외를 던지는 방식으로 처리.
        }
        return accountOptional.get();
    }

    @Override
    public void createAccount(Connection connection, Account account){
        //todo#12 계좌-등록
        if(isExistAccount(connection, account.getAccountNumber())){
            throw new AccountAreadyExistException(account.getAccountNumber());
        } //계좌가 이미 존재하면 예외를 던집니다.

        int result = accountRepository.save(connection, account);
        //계좌가 존재하지 않으면 새로운 계좌를 데이터베이스에 저장.
        if(result<1){
            throw new RuntimeException("Do not Save Account");
        } // 저장이 실패하면 오류
    }

    @Override
    public boolean depositAccount(Connection connection, long accountNumber, long amount){
        //todo#13 예금, 계좌가 존재하는지 체크 -> 예금실행 -> 성공 true, 실패 false;

        if(!isExistAccount(connection, accountNumber)){
            throw new AccountNotFoundException(accountNumber);
        } //계좌가 존재하지 않으면 예외던짐.

        int result = accountRepository.deposit(connection, accountNumber, amount);
        // 계좌가 존재하면 예금 처리.
        return result>0; // 입금이 성공적으로 이루어졌는지 확인하고 그 결과에 따라 true/false 반환하는 역할
        // result 값이 1 이상이면, true. 0 이하이면 false.
    }


    @Override
    public boolean withdrawAccount(Connection connection, long accountNumber, long amount){
        //todo#14 출금, 계좌가 존재하는지 체크 ->  출금가능여부 체크 -> 출금실행, 성공 true, 실폐 false 반환
        if(!isExistAccount(connection, accountNumber)){
            throw new AccountNotFoundException(accountNumber);
        }

        Optional<Account> accountOptional = accountRepository.findByAccountNumber(connection, accountNumber);
        Account account = accountOptional.get();

        if(!account.isWithdraw(amount)){ // 출금 가능한지 체크.
            throw new BalanceNotEnoughException(accountNumber);
        }
        int result = accountRepository.withdraw(connection, accountNumber, amount);
        return result>0;
    }

    @Override
    public void transferAmount(Connection connection, long accountNumberFrom, long accountNumberTo, long amount){
        //todo#15 계좌 이체 accountNumberFrom -> accountNumberTo 으로 amount만큼 이체

        //계좌체크
        if(!isExistAccount(connection, accountNumberFrom)){
            throw new AccountNotFoundException(accountNumberFrom);
        }
        if(!isExistAccount(connection, accountNumberTo)){
            throw new AccountNotFoundException(accountNumberTo);
        }

        Optional<Account> accountFromOptional = accountRepository.findByAccountNumber(connection, accountNumberFrom);
        if(accountFromOptional.isEmpty()){
            throw new AccountNotFoundException(accountNumberFrom);
        }
        Optional<Account> accountToOptional = accountRepository.findByAccountNumber(connection, accountNumberTo);
        if(accountToOptional.isEmpty()){
            throw new AccountNotFoundException(accountNumberTo);
        }

        Account accountFrom = accountFromOptional.get();

        if(!accountFrom.isWithdraw(amount)){ //출금 가능한지 체크.
            throw new BalanceNotEnoughException(accountNumberFrom); //잔액 부족 시 예외.
        }

        int result1 = accountRepository.withdraw(connection, accountNumberFrom, amount);

        if(result1<1){
            throw new RuntimeException("fail - withdraw : " + accountNumberFrom );
        }

        int result2 = accountRepository.deposit(connection, accountNumberTo, amount);
        if(result2<1){
            throw new RuntimeException("fail - deposit: " + accountNumberTo );
        }

    }

    @Override
    public boolean isExistAccount(Connection connection, long accountNumber){
        //todo#16 Account가 존재하면 true , 존재하지 않다면 false
        int count = accountRepository.countByAccountNumber(connection, accountNumber);
        // 계좌가 존재하는지 확인.
        return count >0; //계좌가 존재하면 true, 아니면 false.
    }

    @Override
    public void dropAccount(Connection connection, long accountNumber) {
        //todo#17 account 삭제
        if(!isExistAccount(connection, accountNumber)){
            throw new AccountNotFoundException(accountNumber);
        }
        int result = accountRepository.deleteByAccountNumber(connection, accountNumber);
        if(result<1){
            throw new RuntimeException("fail - drop : " + accountNumber );
        }
    }

}