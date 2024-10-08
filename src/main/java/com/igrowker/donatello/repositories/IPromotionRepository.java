package com.igrowker.donatello.repositories;

import com.igrowker.donatello.models.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPromotionRepository extends JpaRepository<Promotion, Integer> {
    List<Promotion> findAllByIdUser(Integer idUser);
}
