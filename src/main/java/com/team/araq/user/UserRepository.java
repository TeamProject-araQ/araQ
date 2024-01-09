package com.team.araq.user;

import com.team.araq.idealType.IdealType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByusername(String username);
    List<SiteUser> findByAddressLikeAndGender(String address, String gender);
    Page<SiteUser> findAll(Specification<SiteUser> spec, Pageable pageable);
    List<SiteUser> findByLogin(boolean login);

    Optional<SiteUser> findByEmail(String email);

    Optional<SiteUser> findByToken(String token);

    Optional<SiteUser> findByNameAndPhoneNum(String name, String phoneNum);

    List<SiteUser> findByGenderNot(String gender);

    @Query("SELECT u FROM SiteUser u WHERE " +
            "u.gender <> :gender AND " +
            "(CASE WHEN u.age BETWEEN :#{#idealType.minAge} AND :#{#idealType.maxAge} THEN 1 ELSE 0 END) + " +
            "(CASE WHEN u.height BETWEEN :#{#idealType.minHeight} AND :#{#idealType.maxHeight} THEN 1 ELSE 0 END) + " +
            "(CASE WHEN :#{#idealType.drinking} IS NULL OR u.drinking = :#{#idealType.drinking} THEN 1 ELSE 0 END) + " +
            "(CASE WHEN :#{#idealType.education} IS NULL OR u.education = :#{#idealType.education} THEN 1 ELSE 0 END) + " +
            "(CASE WHEN :#{#idealType.smoking} IS NULL OR u.smoking = :#{#idealType.smoking} THEN 1 ELSE 0 END) + " +
            "(CASE WHEN :#{#idealType.religion} IS NULL OR u.religion = :#{#idealType.religion} THEN 1 ELSE 0 END) >= 3")
    List<SiteUser> findMatchingUsersByIdealType(@Param("idealType") IdealType idealType, @Param("gender") String gender);

    @Query(value = "SELECT * FROM site_user WHERE gender != :gender AND religion = :religion ORDER BY preference DESC, RAND() LIMIT 10", nativeQuery = true)
    List<SiteUser> findByGenderNotAndReligion(@Param("gender") String gender, @Param("religion") String religion);

    @Query(value = "SELECT * FROM site_user WHERE gender != :gender AND drinking = :drinking ORDER BY preference DESC, RAND() LIMIT 10", nativeQuery = true)
    List<SiteUser> findByGenderNotAndDrinking(@Param("gender") String gender, @Param("drinking") String drinking);

    @Query(value = "SELECT * FROM site_user WHERE gender != :gender AND Smoking = :Smoking ORDER BY preference DESC, RAND() LIMIT 10", nativeQuery = true)
    List<SiteUser> findByGenderNotAndSmoking(@Param("gender") String gender, @Param("Smoking") String Smoking);

    @Query(value = "SELECT * FROM site_user WHERE gender != :gender AND hobby LIKE %:hobby% ORDER BY preference DESC, RAND() LIMIT 10", nativeQuery = true)
    List<SiteUser> findByGenderNotAndHobbyContaining(@Param("gender") String gender, @Param("hobby") String hobby);

    @Query(value = "SELECT * FROM site_user WHERE gender != :gender AND mbti = :mbti ORDER BY preference DESC, RAND() LIMIT 10", nativeQuery = true)
    List<SiteUser> findByGenderNotAndMbti(@Param("gender") String gender, @Param("mbti") String mbti);

    List<SiteUser> findByLocation(String location);

    List<SiteUser> findByGetPreferenceTimeBefore(LocalDateTime time);

    @Query(value = "SELECT * FROM site_user WHERE gender != :gender AND preference = :status ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<SiteUser> findByGenderNotAndPreferenceRandomly(@Param("gender") String gender, @Param("status") boolean status);

}
