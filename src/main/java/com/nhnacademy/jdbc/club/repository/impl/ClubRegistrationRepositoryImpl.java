package com.nhnacademy.jdbc.club.repository.impl;

import com.nhnacademy.jdbc.club.domain.ClubStudent;
import com.nhnacademy.jdbc.club.repository.ClubRegistrationRepository;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


// JDBC(Java Database Connectivity)를 사용하여 동아리(club)와 학생(student)의 등록 및 조회를 처리하는
// ClunRegistrationRepositoryImpl 클래스입니다.
@Slf4j
public class ClubRegistrationRepositoryImpl implements ClubRegistrationRepository {
//ClubRegistrationRepository 인터페이스를 구현한 클래스입니다.
// 데이터베이스 테이블 jdbc_club_registration, jdbc_students, jdbc_club을 사용하여 학생과 동아리 간의 관계를 관리.
// Connection 객체를 이용하여 SQL 실행.


    @Override
    public int save(Connection connection, String studentId, String clubId) { // 학생이 클럽에 가입
        //todo#11 - 핵생 -> 클럽 등록, executeUpdate() 결과를 반환
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into jdbc_club_registrations set student_id=?, club_id=?");
        ){
            statement.setString(1, studentId);
            statement.setString(2, clubId);

            return statement.executeUpdate(); //INSERT 실행 결과(변경된 행의 개수) 반환. 성공시 1 이상의 값 반환.
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 학생이 클럽에서 탈퇴.
     *
     * @param connection
     * @param studentId
     * @param clubId
     * @return
     */
    @Override
    public int deleteByStudentIdAndClubId(Connection connection, String studentId, String clubId) {
        //todo#12 - 핵생 -> 클럽 탈퇴, executeUpdate() 결과를 반환\
        try (PreparedStatement statement = connection
                .prepareStatement("delete from jdbc_club_registrations where student_id=? and club_id=?");){
            statement.setString(1, studentId);
            statement.setString(2, clubId);

            return statement.executeUpdate();
        } catch (SQLException e) {}
            throw new RuntimeException();
    }

    /**
     * 특정한 학생이 가입한 클럽 조회. studentId를 입력받아 그 학생이 속한 클럽 목록을 반환.
     * @param connection
     * @param studentId
     * @return
     */
    @Override
    public List<ClubStudent> findClubStudentsByStudentId(Connection connection, String studentId) {
        //todo#13 - 핵생 -> 클럽 등록, executeUpdate() 결과를 반환
        try (PreparedStatement statement = connection.
                prepareStatement("select a.id as student_id, a.name as student_name, c.club_id, c.club_name from jdbc_students a inner join jdbc_club registrations b on a.id=b.student_id inner join jdbc_club c on b.club_id=c.club_id where a.id=?"
        )){
            statement.setString(1, studentId);

            try (ResultSet resultSet = statement.executeQuery()) { //execute 쿼리 실행 후 resultSet에 결과 저장.
                List<ClubStudent> clubStudentList = new ArrayList<>();

                while (resultSet.next()) { // 조회된 행을 하나씩 가져옴.
                    clubStudentList.add(new ClubStudent(resultSet.getString("student_id"),
                            resultSet.getString("student_name"),
                            resultSet.getString("club_id"),
                            resultSet.getString("club_name")));
                }
                return clubStudentList;
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
        }
    }

    // 모든 학생-클럽 정보 조회.
    // right join을 사용하여 클럽 기준으로 등록된 학생을 포함하는 데이터를 조회합니다.
    @Override
    public List<ClubStudent> findClubStudents(Connection connection) {
        //todo#21 - join
        String sql = "select a.id as student_id, a.name as student_name, c.club_id, c.club_name from jdbc_students a right join jdbc_club_registrations b on a.id=b.student_id right join jdbc_club c on b.club_id=c.club_id order by c.club_id asc,a.id asc";
        try {
            return getClubStudentList(connection, sql);
        } catch (Exception e) {
            throw e;
        }
    }

    // left join을 사용하여 학생을 기준으로 소속된 클럽 정보를 조회.
    @Override
    public List<ClubStudent> findClubStudents_left_join(Connection connection) {
        //todo#22 - left join
        String sql = "select   a.id as student_id,  a.name as student_name,  c.club_id,  c.club_name from jdbc_students a  left join jdbc_club_registrations b on a.id=b.student_id left join jdbc_club c on b.club_id=c.club_id order by a.id asc, b.club_id asc";
        try {
            return getClubStudentList(connection, sql);
        } catch (Exception e) {
            throw e;
        }
    }


    // right join을 사용하여 클럽을 기준으로 학생 정보를 조회.
    @Override
    public List<ClubStudent> findClubStudents_right_join(Connection connection) {
        //todo#23 - right join

        String sql = "select a.id as student_id, a.name as student_name, c.club_id, c.club_name from jdbc_students a right join jdbc_club_registrations b on a.id=b.student_id right join jdbc_club c on b.club_id=c.club_id order by c.club_id asc,a.id asc";
        try {
            return getClubStudentList(connection, sql);
        } catch (Exception e) {
            throw e;
        }
    }

    //mysql은 Full join을 지원하지 않으므로 left join union right join으로 구현.
    @Override
    public List<ClubStudent> findClubStudents_full_join(Connection connection) {
        //todo#24 - full join = left join union right join
        StringBuilder sb = new StringBuilder();
        //left join
        sb.append("select   a.id as student_id,  a.name as student_name,  c.club_id,  c.club_name from jdbc_students a  left join jdbc_club_registrations b on a.id=b.student_id left join jdbc_club c on b.club_id=c.club_id");
        sb.append(System.lineSeparator());
        sb.append("union");
        sb.append(System.lineSeparator());
        // right join
        sb.append("select a.id as student_id, a.name as student_name, c.club_id, c.club_name from jdbc_students a right join jdbc_club_registrations b on a.id=b.student_id right join jdbc_club c on b.club_id=c.club_id");
        try {
            return getClubStudentList(connection, sb.toString());
        } catch (Exception e) {
            throw e;
        }
    }

    // 클럽에 소속되지 않은 학생만 조회합니다.
    @Override
    public List<ClubStudent> findClubStudents_left_excluding_join(Connection connection) {
        //todo#25 - left excluding join
        String sql =  "select   a.id as student_id,  a.name as student_name,  c.club_id,  c.club_name from jdbc_students a  left join jdbc_club_registrations b on a.id=b.student_id left join jdbc_club c on b.club_id=c.club_id where c.club_id is null order by a.id asc";
        try {
            return getClubStudentList(connection, sql);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<ClubStudent> findClubStudents_right_excluding_join(Connection connection) {
        //todo#26 - right excluding join
        String sql = "select   a.id as student_id,  a.name as student_name,  c.club_id,  c.club_name from jdbc_students a  right join jdbc_club_registrations b on a.id=b.student_id right join jdbc_club c on b.club_id=c.club_id where a.id is null order by b.club_id asc ";
        try {
            return getClubStudentList(connection, sql);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<ClubStudent> findClubStudents_outher_excluding_join(Connection connection) {
        //todo#27 - outher_excluding_join = left excluding join union right excluding join
        // join
        StringBuilder sb = new StringBuilder();
        // left join
        sb.append(
                "select   a.id as student_id,  a.name as student_name,  c.club_id,  c.club_name from jdbc_students a  left join jdbc_club_registrations b on a.id=b.student_id left join jdbc_club c on b.club_id=c.club_id where c.club_id is null");
        sb.append(System.lineSeparator());
        sb.append("union");
        sb.append(System.lineSeparator());
        // right join
        sb.append(
                "select   a.id as student_id,  a.name as student_name,  c.club_id,  c.club_name from jdbc_students a  right join jdbc_club_registrations b on a.id=b.student_id right join jdbc_club c on b.club_id=c.club_id where a.id is null");

        try {
            return getClubStudentList(connection, sb.toString());
        } catch (Exception e) {
            throw e;
        }
    }

    private List<ClubStudent> getClubStudentList(Connection connection, String sql) {
        ResultSet rs = null;

        try (PreparedStatement psmt = connection.prepareStatement(sql)) {
            rs = psmt.executeQuery();

            List<ClubStudent> clubStudentlist = new ArrayList<>();
            while (rs.next()) {
                clubStudentlist.add(
                        new ClubStudent(
                                rs.getString("student_id"),
                                rs.getString("student_name"),
                                rs.getString("club_id"),
                                rs.getString("club_name")));
            }
            return clubStudentlist;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (Objects.nonNull(rs)) {
                    rs.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}