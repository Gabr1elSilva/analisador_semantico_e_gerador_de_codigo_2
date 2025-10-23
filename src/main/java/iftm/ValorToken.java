package iftm;

public class ValorToken {

    private String texto;
    private int inteiro;
    private double pontoFlutuante;

    public ValorToken(String texto) {
        this.texto = texto;
    }

    public ValorToken(int inteiro) {
        this.inteiro = inteiro;
    }

    public ValorToken(double pontoFlutuante) {
        this.pontoFlutuante = pontoFlutuante;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public int getInteiro() {
        return inteiro;
    }

    public void setInteiro(int inteiro) {
        this.inteiro = inteiro;
    }

    public double getPontoFlutuante() {
        return pontoFlutuante;
    }

    public void setPontoFlutuante(double pontoFlutuante) {
        this.pontoFlutuante = pontoFlutuante;
    }

    @Override
    public String toString() {

        
        return "ValorToken [texto=" + texto + ", inteiro=" + inteiro + ", pontoFlutuante=" + pontoFlutuante + "]";
    }

}
