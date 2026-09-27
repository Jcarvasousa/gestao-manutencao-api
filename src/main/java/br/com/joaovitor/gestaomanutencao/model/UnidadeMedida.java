package br.com.joaovitor.gestaomanutencao.model;

public enum UnidadeMedida {
    UN("Un"),
    G("g"),
    KG("Kg"),
    T("T"),
    ML("mL"),
    L("L"),
    M3("m³"),
    MM("mm"),
    CM("cm"),
    M("m"),
    M2("m²"),
    CAIXA("Caixa"),
    SACO("Saco"),
    PACOTE("Pacote");

    private final String label;

    UnidadeMedida(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
