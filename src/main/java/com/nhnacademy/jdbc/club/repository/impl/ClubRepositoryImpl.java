package com.nhnacademy.jdbc.club.repository.impl;

import com.mysql.cj.x.protobuf.MysqlxPrepare;
import com.nhnacademy.jdbc.club.domain.Club;
import com.nhnacademy.jdbc.club.repository.ClubRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ClubRepositoryImpl implements ClubRepository {

    @Override
    public Optional<Club> findByClubId(Connection connection, String clubId) {
        //todo#3 club 조회
        String sql = "select club_id, club_name, club_created_at from jdbc_club where club_id = ?";
        ResultSet rs = null;
        try (PreparedStatement psmt = connection.prepareStatement(sql)) {
            psmt.setString(1, clubId);
            rs = psmt.executeQuery();
            if (rs.next()) {
                return Optional.of( //데이터가 존재하면 Optional.of(Club)
                        new Club(rs.getString("club_id"),
                                rs.getString("club_name"),
                                rs.getTimestamp("club_created_at").toLocalDateTime()));
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
        return Optional.empty(); //club 존재하지 않을 시 반환.
    }

    @Override
    public int save(Connection connection, Club club) {
        //todo#4 club 생성, executeUpdate() 결과를 반환
        String sql = "insert into jdbc_club (club_id, club_name) VALUES (?,?)";

        try (PreparedStatement psmt = connection.prepareStatement(sql)) {
            psmt.setString(1, club.getClubId());
            psmt.setString(2, club.getClubName());
            return psmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public int update(Connection connection, Club club) {
        //todo#5 club 수정, clubName을 수정합니다. executeUpdate()결과를 반환

        String sql = "update jdbc_club set club_name = ? where club_id = ?";

        try (PreparedStatement psmt = connection.prepareStatement(sql)) {
            psmt.setString(1, club.getClubName());
            psmt.setString(2, club.getClubId());
            return psmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int deleteByClubId(Connection connection, String clubId) {
        //todo#6 club 삭제, executeUpdate()결과 반환
        String sql = "delete from jdbc_club where club_id = ?";

        try (PreparedStatement psmt = connection
                .prepareStatement(sql);) {
            psmt.setString(1, clubId);
            return psmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }}

        @Override
        public int countByClubId (Connection connection, String clubId){
            int count = 0;
            //todo#7 clubId에 해당하는 club의 count를 반환
            try (PreparedStatement statement = connection
                    .prepareStatement("select count(*) from jdbc_club where club_id=?");) {
                statement.setString(1, clubId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return count;
        }
    }
