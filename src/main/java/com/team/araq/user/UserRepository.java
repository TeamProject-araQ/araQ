package com.team.araq.user;

import com.team.araq.idealType.IdealType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByusername(String username);
    Page<SiteUser> findAll(Specification<SiteUser> spec, Pageable pageable);
    List<SiteUser> findByLogin(boolean login);

    Optional<SiteUser> findByEmail(String email);

    Optional<SiteUser> findByToken(String token);

    Optional<SiteUser> findByNameAndPhoneNum(String name, String phoneNum);

    List<SiteUser> findByGenderNot(String gender);

    @Query(value = "SELECT * FROM site_user WHERE gender != :gender ORDER BY preference DESC, RAND() LIMIT 3", nativeQuery = true)
    List<SiteUser> findByGenderNotRandom(@Param("gender") String gender);

    @Query(value = "SELECT * FROM site_user WHERE " +
            "gender <> :gender AND " +
            "age BETWEEN :minAge AND :maxAge AND " +
            "height BETWEEN :minHeight AND :maxHeight AND " +
            "(:drinking = '상관 없음' OR drinking = :drinking) AND " +
            "(:education = '상관 없음' OR education = :education) AND " +
            "(:smoking = '상관 없음' OR smoking = :smoking) AND " +
            "(:religion = '상관 없음' OR religion = :religion) ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<SiteUser> findMatchingUsersByIdealTypeRand(@Param("gender") String gender,
                                                @Param("minAge") int minAge,
                                                @Param("maxAge") int maxAge,
                                                @Param("minHeight") int minHeight,
                                                @Param("maxHeight") int maxHeight,
                                                @Param("drinking") String drinking,
                                                @Param("education") String education,
                                                @Param("smoking") String smoking,
                                                @Param("religion") String religion);

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

    @Query(value = "SELECT * FROM site_user WHERE gender != :gender AND address LIKE %:address% ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<SiteUser> findByAddressLikeAndGender(@Param("gender") String gender, @Param("address") String address);


    List<SiteUser> findByLocation(String location);

    @Query(value = "SELECT * FROM site_user WHERE gender != :gender AND preference = :status ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<SiteUser> findByGenderNotAndPreferenceRandomly(@Param("gender") String gender, @Param("status") boolean status);

    List<SiteUser> findByRoleIn(Collection<UserRole> role);

    long countByGender(String gender);

    Optional<SiteUser> findByPhoneNum(String phoneNum);
}
