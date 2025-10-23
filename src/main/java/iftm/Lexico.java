package iftm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lexico {

    private char caractere;
    private BufferedReader br;
    private int linha = 1;
    private int coluna = 0;
    private List<String> palavrasReservadas = new ArrayList<>();

    public Lexico(String nomeArquivoACompilar) {
        String caminhoArquivo = Paths.get(nomeArquivoACompilar).toAbsolutePath().toString();
        try {
            br = new BufferedReader(new FileReader(caminhoArquivo, StandardCharsets.UTF_8));
            caractere = proximoCaractere();

            palavrasReservadas.addAll(Arrays.asList(
                    "program", "begin", "end", "var", "integer", "procedure", "function",
                    "read", "write", "writeln", "for", "do", "repeat", "while",
                    "until", "if", "then", "else", "or", "and", "not", "true", "false", "to"));
        } catch (IOException e) {
            System.err.println("Não foi possível abrir o arquivo: " + nomeArquivoACompilar);
            e.printStackTrace();
        }
    }

    public Token proximoToken() {
        String lexema;
        Token token;
        while ((int) caractere != 65535) { // 65535 é o EOF (end of file)
            lexema = "";
            if (Character.isLetter(caractere)) {
                token = new Token(linha, coluna, ClasseToken.Identificador);
                while (Character.isLetter(caractere) || Character.isDigit(caractere)) {
                    lexema += caractere;
                    caractere = proximoCaractere();
                }
                token.setValor(new ValorToken(lexema));
                if (palavrasReservadas.contains(lexema.toLowerCase())) {
                    token.setClasse(ClasseToken.PalavraReservada);
                }
                return token;
            } else if (Character.isDigit(caractere)) {
                token = new Token(linha, coluna, ClasseToken.NumeroInteiro);
                while (Character.isDigit(caractere)) {
                    lexema += caractere;
                    caractere = proximoCaractere();
                }
                if (caractere == '.') {
                    lexema += caractere;
                    caractere = proximoCaractere();
                    if (Character.isDigit(caractere)) {
                        while (Character.isDigit(caractere)) {
                            lexema += caractere;
                            caractere = proximoCaractere();
                        }
                        token.setClasse(ClasseToken.NumeroPontoFlutuante);
                        token.setValor(new ValorToken(Double.parseDouble(lexema)));
                        return token;
                    } else {
                        System.out.println(linha + "," + coluna + "- Erro léxico: caractere inválido" + caractere);
                        System.exit(-1);
                    }
                }
                token.setValor(new ValorToken(Integer.parseInt(lexema)));
                return token;
            } else if (caractere == '+') {
                token = new Token(linha, coluna, ClasseToken.Adicao);
                lexema += caractere;
                caractere = proximoCaractere();
                return token;
            } else if (caractere == '-') {
                token = new Token(linha, coluna, ClasseToken.Subtracao);
                lexema += caractere;
                caractere = proximoCaractere();
                return token;
            } else if (caractere == '*') {
                token = new Token(linha, coluna, ClasseToken.Multiplicacao);
                lexema += caractere;
                caractere = proximoCaractere();
                return token;
            } else if (caractere == '/') {
                token = new Token(linha, coluna, ClasseToken.Divisao);
                lexema += caractere;
                caractere = proximoCaractere();
                if (caractere == '/') {
                    do {
                        caractere = proximoCaractere();
                    } while (caractere != '\n' && (int) caractere != 65535);

                } else {
                    return token;
                }
            } else if (caractere == '>') {
                token = new Token(linha, coluna, ClasseToken.Maior);
                lexema += caractere;
                caractere = proximoCaractere();
                if (caractere == '=') {
                    lexema += caractere;
                    caractere = proximoCaractere();
                    token.setClasse(ClasseToken.MaiorIgual);
                }
                return token;
            } else if (caractere == '<') {
                token = new Token(linha, coluna, ClasseToken.Menor);
                lexema += caractere;
                caractere = proximoCaractere();
                if (caractere == '=') {
                    lexema += caractere;
                    caractere = proximoCaractere();
                    token.setClasse(ClasseToken.MenorIgual);
                } else if (caractere == '>') {
                    lexema += caractere;
                    caractere = proximoCaractere();
                    token.setClasse(ClasseToken.Diferente);
                }
                return token;
            } else if (caractere == ':') {
                token = new Token(linha, coluna, ClasseToken.DoisPontos);
                lexema += caractere;
                caractere = proximoCaractere();
                if (caractere == '=') {
                    lexema += caractere;
                    caractere = proximoCaractere();
                    token.setClasse(ClasseToken.Atribuicao);
                }
                return token;
            } else if (caractere == ',') {
                token = new Token(linha, coluna, ClasseToken.Virgula);
                lexema += caractere;
                caractere = proximoCaractere();
                return token;
            } else if (caractere == ';') {
                token = new Token(linha, coluna, ClasseToken.PontoEVirgula);
                lexema += caractere;
                caractere = proximoCaractere();
                return token;
            } else if (caractere == '(') {
                token = new Token(linha, coluna, ClasseToken.ParentesesEsquerdo);
                lexema += caractere;
                caractere = proximoCaractere();
                return token;
            } else if (caractere == ')') {
                token = new Token(linha, coluna, ClasseToken.ParentesesDireito);
                lexema += caractere;
                caractere = proximoCaractere();
                return token;
            } else if (caractere == '[') {
                token = new Token(linha, coluna, ClasseToken.ColchetesEsquerdo);
                lexema += caractere;
                caractere = proximoCaractere();
                return token;
            } else if (caractere == ']') {
                token = new Token(linha, coluna, ClasseToken.ColchetesDireito);
                lexema += caractere;
                caractere = proximoCaractere();
                return token;
            } else if (caractere == '.') {
                token = new Token(linha, coluna, ClasseToken.Ponto);
                lexema += caractere;
                caractere = proximoCaractere();
                return token;
            } else if (caractere == '=') {
                token = new Token(linha, coluna, ClasseToken.Igual);
                lexema += caractere;
                caractere = proximoCaractere();
                return token;
            } else if (Character.isWhitespace(caractere)) {
                caractere = proximoCaractere();
            } else if (caractere == '{') {
                while (caractere != '}' && (int) caractere != 65535) {
                    caractere = proximoCaractere();
                }

                if (caractere == '}') {
                    caractere = proximoCaractere();
                }
            } else if (caractere == '\'') {
                token = new Token(linha, coluna, ClasseToken.String);
                caractere = proximoCaractere();
                int linhaOld = linha;
                int colunaOld = coluna;
                
                while (caractere != '\'' && caractere != '\n' && (int) caractere != 65535) {
                    lexema += caractere;
                    linhaOld = linha;
                    colunaOld = coluna;
                    caractere = proximoCaractere();

                }

                if (caractere == '\'') {
                    token.setValor(new ValorToken(lexema));
                    caractere = proximoCaractere();
                    return token;
                } else if (caractere == '\n'){
                    linha = linhaOld;
                    coluna = colunaOld;
                    System.out.println(linha + "," + coluna + "- Erro léxico: String não terminada");
                    System.exit(-1);
                } else if((int) caractere == 65535) {
                    System.out.println(linha + "," + coluna + "- Erro léxico: String não terminada");
                    System.exit(-1);
                }
            } else {
                System.out.println(linha + "," + coluna + "- Erro léxico: caractere inválido" + caractere);
                System.exit(-1);
            }
        }
        return new Token(linha, coluna, ClasseToken.EOF);

    }

    public char proximoCaractere() {
        try {
            char c = (char) br.read();
            if (c == '\n') {
                linha++;
                coluna = 0;
            } else {
                coluna++;
            }
            return c;
        } catch (IOException e) {
            System.err.println("Não foi possível ler do arquivo");
            e.printStackTrace();
            return '\0';
        }
    }

}