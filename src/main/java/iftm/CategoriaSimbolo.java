package iftm;


public enum CategoriaSimbolo {
    FUNCAO("Função"),
    VARIAVEL("Variável"),
    PARAMETRO("Parâmetro"),
    PROCEDIMENTO("Procedimento"),
    TIPO("Tipo"),
    PROGRAMAPRINCIPAL("Programa Principal");

    private String descricao;

    private CategoriaSimbolo(String descricao){
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

}
