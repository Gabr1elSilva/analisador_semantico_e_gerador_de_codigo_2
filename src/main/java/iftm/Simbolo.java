package iftm;

public class Simbolo {
    private String lexema;
    private CategoriaSimbolo categoria;
    private TipoSimbolo tipo;
    private int endereco;
    public String getLexema() {
        return lexema;
    }
    public void setLexema(String lexema) {
        this.lexema = lexema;
    }
    public CategoriaSimbolo getCategoria() {
        return categoria;
    }
    public void setCategoria(CategoriaSimbolo categoria) {
        this.categoria = categoria;
    }
    public TipoSimbolo getTipo() {
        return tipo;
    }
    public void setTipo(TipoSimbolo tipo) {
        this.tipo = tipo;
    }
    public int getEndereco() {
        return endereco;
    }
    public void setEndereco(int endereco) {
        this.endereco = endereco;
    }
    @Override
    public String toString() {
        return "Simbolo [lexema=" + lexema + ", categoria=" + categoria + ", tipo=" + tipo + ", endereco=" + endereco
                + ", getLexema()=" + getLexema() + ", getCategoria()=" + getCategoria() + ", getTipo()=" + getTipo()
                + ", getEndereco()=" + getEndereco() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
                + ", toString()=" + super.toString() + "]";
    }

}
