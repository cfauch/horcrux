module com.code.fauch.horcrux {
    requires transitive java.sql;
    exports com.code.fauch.horcrux;
    exports com.code.fauch.horcrux.spi;
    uses com.code.fauch.horcrux.spi.IHorcrux;
    provides com.code.fauch.horcrux.spi.IHorcrux with com.code.fauch.horcrux.BasicHorcrux;
}