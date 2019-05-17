package com.zul.tests.obdzulbeta;

public class ItemPID {


    String def;
    String value;
    String units;

    public ItemPID(String DEF, String VAL, String UNITS) {
        def = DEF;
        value = VAL;
        units = UNITS;
    }

    public String getDef() {return def; }
    public String getValue() {return value; }
    public String getUnites() {return units; }

    public void setValue(String x) { value = x;}

}