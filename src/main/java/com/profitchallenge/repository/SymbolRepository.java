package com.profitchallenge.repository;

import com.profitchallenge.domain.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SymbolRepository extends JpaRepository<Symbol,String> {
}
