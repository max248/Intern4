package com.example.intern4;

import com.example.intern4.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u where u.email = ?1")
    User findByEmail(String email);

//    @Query("SELECT u FROM User u where u.id = ?1")
//    User findById(Long id);

    @Modifying
    @Query("update User u set u.lastLoginDate = ?2 where u.id = ?1")
    @Transactional
    void updateLastLoginDateById(Long id, String date);

    @Modifying
    @Query("update User u set u.enabled = ?2 where u.id = ?1")
    @Transactional
    void updateStatusById(Long id, boolean flag);
}
