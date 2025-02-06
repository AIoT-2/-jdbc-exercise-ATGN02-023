package com.nhnacademy.jdbc.student.repository.impl;

import com.nhnacademy.jdbc.common.Page;
import com.nhnacademy.jdbc.student.domain.Student;
import com.nhnacademy.jdbc.student.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class StudentRepositoryImpl implements StudentRepository {

    @Override
    public int save(Connection connection, Student student){
        String sql = "insert into jdbc_students(id,name,gender,age) values(?,?,?,?)";

        try(
            PreparedStatement statement = connection.prepareStatement(sql);
        ){
            statement.setString(1, student.getId());
            statement.setString(2, student.getName());
            statement.setString(3, student.getGender().toString());
            statement.setInt(4,student.getAge());

            int result = statement.executeUpdate();
            log.debug("save:{}",result);
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<Student> findById(Connection connection,String id){
        String sql = "select * from jdbc_students where id=?";
        log.debug("findById:{}",sql);

        ResultSet rs = null;
        try(
            PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setString(1,id);
            rs = statement.executeQuery();
            if(rs.next()){
                Student student =  new Student(rs.getString("id"),
                        rs.getString("name"),
                        Student.GENDER.valueOf(rs.getString("gender")),
                        rs.getInt("age"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                return Optional.of(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public int update(Connection connection,Student student){
        String sql = "update jdbc_students set name=?, gender=?, age=? where id=?";
        log.debug("update:{}",sql);

        try(
            PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            int index=0;
            statement.setString(++index, student.getName());
            statement.setString(++index, student.getGender().toString());
            statement.setInt(++index, student.getAge());
            statement.setString(++index, student.getId());

            int result = statement.executeUpdate();
            log.debug("result:{}",result);
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int deleteById(Connection connection,String id){
        String sql = "delete from jdbc_students where id=?";

        try(
            PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setString(1, id);
            int result = statement.executeUpdate();
            log.debug("result:{}",result);
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int deleteAll(Connection connection) {
        String sql = "delete from jdbc_students";

        try(
                PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            int result = statement.executeUpdate();
            log.debug("result:{}",result);
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //전체 학생 수 조회.
    @Override
    public long totalCount(Connection connection) {
        //todo#4 totalCount 구현
        long count = 0L;
        try (PreparedStatement statement = connection.prepareStatement("select count(*) from jdbc_students");
             // 학생 테이블에서 전체 학생 수를 조회.
               ResultSet resultSet = statement.executeQuery();) {
            if (resultSet.next()){
                return resultSet.getLong(1); //조회된 첫 번쨰 칼럼의 값을 long 타입으로 가져옴.
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    // 페이징 처리된 학생 목록 조회.
    @Override
    public Page<Student> findAll(Connection connection, int page, int pageSize) {
        //todo#5 페이징 처리 구현
        int offset = (page -1) * pageSize; //페이지 오프셋 계산.
        // 오프셋이란?: 데이터의 시작 지점.
        // 여러 페이지에 걸쳐 데이터를 나누어 보여줄 때, 현재 페이지에서 데이터를 몇번째부터
        //가져올지를 지정하는 값.
        // 페이지 크기가 10일때, 1페이지는 0번 인덱스부터 9번 인덱스까지 데이터 가져오고,
        // 2페이지는 10번 인덱스부터 19번 인덱스까지 가져옴.
        // 이렇게 offset은 각 페이지가 데이터를 가져올 시작지점을 나타내는 값.
        //ex) 1 페이지(10개 데이터) offset = 0; 2페이지(10개 데이터):offset=10

        try (PreparedStatement statement = connection.prepareStatement(
                "select * from jdbc_students order by id desc limit ?, ?");){
            statement.setInt(1, offset);
            statement.setInt(2, pageSize);

            ArrayList<Student> studentList = new ArrayList<>();

            try (ResultSet resultSet = statement.executeQuery();) {
                while (resultSet.next()) {
                    Student student = new Student(resultSet.getString("id"), resultSet.getString("name"),
                            Student.GENDER.valueOf(resultSet.getString("gender")), resultSet.getInt("age"),
                            resultSet.getTimestamp("created_at").toLocalDateTime());

                    studentList.add(student);
                }

                if (studentList.isEmpty()) {
                    return new Page<>(new ArrayList<>(), 0);
                } else {
                    return new Page<>(studentList, pageSize);
                }

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}