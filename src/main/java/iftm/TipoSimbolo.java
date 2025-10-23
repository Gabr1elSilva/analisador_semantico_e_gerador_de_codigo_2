package iftm;

public enum TipoSimbolo {
    
    INTEIRO("Inteiro"),
    REAL("Real"),
    STRING("String"),
    BOOLEAN("Boolean");

    private String descricao;

    private TipoSimbolo(String descricao){
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

}
