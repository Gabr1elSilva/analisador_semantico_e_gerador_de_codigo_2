package iftm;

public class Token {

    private ClasseToken classe;
    private ValorToken valor;
    private int linha;
    private int coluna;

    public Token(int linha, int coluna, ClasseToken classe) {
        this.linha = linha;
        this.coluna = coluna;
        this.classe = classe;
    }

    public ClasseToken getClasse() {
        return classe;
    }

    public void setClasse(ClasseToken classe) {
        this.classe = classe;
    }

    public ValorToken getValor() {
        return valor;
    }

    public void setValor(ValorToken valor) {
        this.valor = valor;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    public int getColuna() {
        return coluna;
    }

    public void setColuna(int coluna) {
        this.coluna = coluna;
    }

    @Override
    public String toString() {

        String v = "";

        if (classe == ClasseToken.Identificador
                || classe == ClasseToken.PalavraReservada
                || classe == ClasseToken.String) {
            v += valor.getTexto();
        } else if (classe == ClasseToken.NumeroInteiro) {
            v += valor.getInteiro();
        } else if (classe == ClasseToken.NumeroPontoFlutuante) {
            v += valor.getPontoFlutuante();
        }
        return "Token ["+"linha=" + linha + ", coluna=" + coluna +", classe=" + classe + ", valor=" + v +  "]";
    }

}
