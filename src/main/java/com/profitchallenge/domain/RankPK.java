package com.profitchallenge.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class RankPK implements Serializable {

    private int ranking;
    private String rankDate;
}
