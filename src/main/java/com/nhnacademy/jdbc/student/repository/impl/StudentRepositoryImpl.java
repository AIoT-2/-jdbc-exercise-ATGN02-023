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

    @Override
    public long totalCount(Connection connection) {
        //todo#4 totalCount 구현
        long count = 0L;
        try (PreparedStatement statement = connection.prepareStatement("select count(*) from jdbc_students");
               ResultSet resultSet = statement.executeQuery();) {
            if (resultSet.next()){
                return resultSet.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    @Override
    public Page<Student> findAll(Connection connection, int page, int pageSize) {
        //todo#5 페이징 처리 구현
        int offset = (page -1) * pageSize;

        try (PreparedStatement statement = connection.prepareStatement(
                "select * from jdbc_students order by id desclimit ?, ?");){
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