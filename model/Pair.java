package com.yiilab.inside.model.btc;


import com.yiilab.inside.model.FinancialInstrument;

import javax.persistence.*;

@Entity
@DiscriminatorValue("btc")
public class Pair extends FinancialInstrument{

    public Pair() {}
}
