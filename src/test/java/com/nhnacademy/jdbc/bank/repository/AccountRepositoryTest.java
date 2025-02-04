package com.nhnacademy.jdbc.bank.repository;

import com.nhnacademy.jdbc.bank.domain.Account;
import com.nhnacademy.jdbc.bank.repository.impl.AccountRepositoryImpl;
import com.nhnacademy.jdbc.util.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;

@Slf4j
class AccountRepositoryTest {

    //todo#7 AccountRepositoryTest를 실행하고 모든 테스트 케이스가 통과하도록 AccountRepositoryImpl을 구현합니다.

    Connection connection;
    AccountRepository accountRepository = new AccountRepositoryImpl();

    @BeforeEach
    void setUp() throws SQLException {
        connection = DbUtils.getDataSource().getConnection();
        System.out.println("현재 연결된 DB: " + connection.getCatalog());

        connection.setAutoCommit(false);

        Account account1 = new Account(8000,"nhn아카데미-8000",10_0000);
        Account account2 = new Account(9000,"nhn아카데미-9000",10_0000);

        accountRepository.save(connection, account1);
        accountRepository.save(connection, account2);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.rollback();

        // 실제로 connection close되지 않고 pool에 반납
        connection.close();
    }

    @Test
    void findByAccountNumber() {
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(connection,8000l);

        Assertions.assertAll(
                ()->Assertions.assertTrue(accountOptional.isPresent()),
                ()->Assertions.assertEquals(8000l,accountOptional.get().getAccountNumber())
        );
    }

    @Test
    @DisplayName("Account 등록")
    void save() {
        Account account = new Account(7000l,"nhn아카데미-7000",10_00000);
        int result = accountRepository.save(connection, account);
        Assertions.assertEquals(1,result);
        log.debug("result:{}", result);
    }

    @Test
    @DisplayName("account_number 중복채크")
    void save_duplicated_pk(){
        Account account = new Account(8000l,"nhn아카데미-8000",10_00000);
        Throwable throwable = Assertions.assertThrows(RuntimeException.class, ()->{
            accountRepository.save(connection,account);
        });
        Assertions.assertTrue(throwable.getMessage().contains(SQLIntegrityConstraintViolationException.class.getName()));
        log.debug("message:{}", throwable.getMessage());
    }

    @Test
    @DisplayName("계좌가 존재")
    void countByAccountNumber() {
        int count = accountRepository.countByAccountNumber(connection,8000l);
        Assertions.assertEquals(1,count);
    }

    @Test
    @DisplayName("계좌가 존재하지 않음")
    void countByAccountNumber_not_exist() {
        int count = accountRepository.countByAccountNumber(connection, Long.MAX_VALUE);
        Assertions.assertEquals(0,count);
    }

    @Test
    @DisplayName("계좌-입금")
    void deposit() {
        long accountNumber=8000l;
        int result = accountRepository.deposit(connection,accountNumber,1_0000);
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(connection,accountNumber);

        Assertions.assertAll(
                ()->Assertions.assertEquals(1, result),
                ()->Assertions.assertTrue(accountOptional.isPresent()),
                ()->Assertions.assertEquals(accountNumber, accountOptional.get().getAccountNumber()),
                ()->Assertions.assertEquals(11_0000, accountOptional.get().getBalance())
        );
    }

    @Test
    @DisplayName("계좌-출금")
    void withdraw() {
        long accountNumber=8000l;
        int result = accountRepository.withdraw(connection,accountNumber,1_0000);
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(connection,accountNumber);

        Assertions.assertAll(
                ()->Assertions.assertEquals(1, result),
                ()->Assertions.assertTrue(accountOptional.isPresent()),
                ()->Assertions.assertEquals(accountNumber, accountOptional.get().getAccountNumber()),
                ()->Assertions.assertEquals(9_0000l, accountOptional.get().getBalance())
        );
    }

    @Test
    @DisplayName("계좌-삭제")
    void deleteByAccountNumber(){
        long accountNumber=8000l;
        int result = accountRepository.deleteByAccountNumber(connection,accountNumber);
        Assertions.assertEquals(result,1);
    }

}