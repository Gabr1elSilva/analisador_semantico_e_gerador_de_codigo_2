package iftm;

public class Main {
    public static void main(String[] args) {
        Sintatico sintatico = new Sintatico("teste.pas");
        sintatico.analisar();
        
        
        
        
        
        /*Lexico lexico = new Lexico("teste.pas");

        Token token;

        

        do{
            token = lexico.proximoToken();
            System.out.println(token);
            
        }while (token.getClasse() != ClasseToken.EOF) ;*/
    }
}