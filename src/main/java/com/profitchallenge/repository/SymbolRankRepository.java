package com.profitchallenge.repository;

import com.profitchallenge.domain.RankPK;
import com.profitchallenge.domain.SymbolRank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SymbolRankRepository extends JpaRepository<SymbolRank, RankPK> {
    List<SymbolRank> findByRankPKRankDate(String rankDate);
}
