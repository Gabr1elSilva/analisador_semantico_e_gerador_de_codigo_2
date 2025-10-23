package iftm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Sintatico {

    private String rotulo = "";
    private int contRotulo = 1;
    private int offsetVariavel = 0;
    private String nomeArquivoSaida;
    private String caminhoArquivoSaida;
    private BufferedWriter bw;
    private FileWriter fw;
    private static final int TAMANHO_INTEIRO = 4;
    private List<String> variaveis = new ArrayList<>();
    private List<String> sectionData = new ArrayList<>();
    private Simbolo simbolo;
    private String rotuloElse;
    private Token token;
    private Lexico lexico;

    private HashMap<String, Simbolo> tabelaSimbolos;

    public Sintatico(String nomeArquivo) {
        tabelaSimbolos = new HashMap<>();
        lexico = new Lexico(nomeArquivo);
        token = lexico.proximoToken();
        tabelaSimbolos.get("String");

        nomeArquivoSaida = "queronemver.asm";
        caminhoArquivoSaida = Paths.get(nomeArquivoSaida).toAbsolutePath().toString();
        bw = null;
        fw = null;
        try {
            fw = new FileWriter(caminhoArquivoSaida, Charset.forName("UTF-8"));
            bw = new BufferedWriter(fw);
        } catch (Exception e) {
            System.err.println("Erro ao criar arquivo de saída");
        }
    }

    public void analisar() {
        programa();
    }

    // <programa> ::= program id {A01} ; <corpo> • {A45}
    private void programa() {

        if (isPalavraReservada("program")) {

            token = lexico.proximoToken();

            if (token.getClasse() == ClasseToken.Identificador) {

                // {A01}
                Simbolo simbolo = new Simbolo();
                simbolo.setCategoria(CategoriaSimbolo.PROGRAMAPRINCIPAL);
                simbolo.setLexema(token.getValor().getTexto());
                tabelaSimbolos.put(token.getValor().getTexto(), simbolo);
                offsetVariavel = 0;
                escreverCodigo("global main");
                escreverCodigo("extern printf");
                escreverCodigo("extern scanf ");
                escreverCodigo("\nsection .text");
                rotulo = "main";
                escreverCodigo("\n\t; Entrada do programa");
                escreverCodigo("\tpush ebp");
                escreverCodigo("\tmov ebp, esp");

                token = lexico.proximoToken();

                if (token.getClasse() == ClasseToken.PontoEVirgula) {

                    token = lexico.proximoToken();

                    corpo();

                    if (token.getClasse() == ClasseToken.Ponto) {
                        token = lexico.proximoToken();
                        // {A45}
                        escreverCodigo("\tleave");
                        escreverCodigo("\tret");
                        if (!sectionData.isEmpty()) {
                            escreverCodigo("\nsection .data\n");
                            for (String mensagem : sectionData) {
                                escreverCodigo(mensagem);
                            }
                        }
                        try {
                            bw.close();
                            fw.close();
                        } catch (IOException e) {
                            System.err.println("Erro ao fechar arquivo de saída");
                        }

                    } else {
                        erroSintatico("Faltou ponto final (.) no final do programa");
                    }
                } else {
                    erroSintatico("Faltou ponto final (;) depois do nome do programa");
                }

            } else {
                erroSintatico("Faltou o nome do programa");
            }
        } else {
            erroSintatico("Faltou começar o programa com PROGRAM");
        }
    }

    // <corpo> ::= <declara> <rotina> {A44} begin <sentencas> end {A46}
    private void corpo() {
        declara();
        // {A44}
        if (isPalavraReservada("begin")) {
            token = lexico.proximoToken();
            sentencas();
            if (isPalavraReservada("end")) {
                token = lexico.proximoToken();
                // {A46}
            } else {
                erroSintatico("Faltou END no final do corpo");
            }
        } else {
            erroSintatico("Faltou BEGIN no começo do corpo");
        }
    }

    // <declara> ::= var <dvar> <mais_dc> | ε
    private void declara() {
        if (isPalavraReservada("var")) {
            token = lexico.proximoToken();
            dvar();
            mais_dc();
        }

    }

    // <sentencas> ::= <comando> <mais_sentencas>
    private void sentencas() {
        comando();
        mais_sentencas();
    }

    // <comando> ::=
    // read ( <var_read> ) |
    // write ( <exp_write> ) |
    // writeln ( <exp_write> ) {A61} |
    // for id {A57} := <expressao> {A11} to <expressao> {A12} do begin <sentencas>
    // end {A13} |
    // repeat {A14} <sentencas> until ( <expressao_logica> ) {A15} |
    // while {A16} ( <expressao_logica> ) {A17} do begin <sentencas> end {A18} |
    // if ( <expressao_logica> ) {A19} then begin <sentencas> end {A20} <pfalsa>
    // {A21} |
    // id {A49} := <expressao> {A22} |

    private void comando() {
        // read ( <var_read> ) |

        if (isPalavraReservada("read")) {
            token = lexico.proximoToken();
            if (token.getClasse() == ClasseToken.ParentesesEsquerdo) {
                token = lexico.proximoToken();
                var_read();
                if (token.getClasse() == ClasseToken.ParentesesDireito) {
                    token = lexico.proximoToken();

                } else {
                    erroSintatico("Faltou fechar parênteses ')' no READ");
                }

            } else {
                erroSintatico("Faltou abrir parênteses '(' no READ");
            }
            // Write ( <exp_write> ) |
        } else if (isPalavraReservada("write")) {
            token = lexico.proximoToken();
            if (token.getClasse() == ClasseToken.ParentesesEsquerdo) {
                token = lexico.proximoToken();
                exp_write();
                if (token.getClasse() == ClasseToken.ParentesesDireito) {
                    token = lexico.proximoToken();
                } else {
                    erroSintatico("Faltou fechar parênteses ')' no WRITE");
                }
            } else {
                erroSintatico("Faltou abrir parênteses '(' no WRITE");
            }
            // writeln ( <exp_write> ) {A61} |
        } else if (isPalavraReservada("writeln")) {
            token = lexico.proximoToken();
            if (token.getClasse() == ClasseToken.ParentesesEsquerdo) {
                token = lexico.proximoToken();
                exp_write();
                if (token.getClasse() == ClasseToken.ParentesesDireito) {
                    token = lexico.proximoToken();
                    // {A61} //MEXI AQUI
                    String novaLinha = "rotuloStringLN: db '',10,0";
                    if (!sectionData.contains(novaLinha)) {
                        sectionData.add(novaLinha);
                    }
                    escreverCodigo("\tpush rotuloStringLN");
                    escreverCodigo("\tcall printf");
                    escreverCodigo("\tadd esp, 4");

                } else {
                    erroSintatico("Faltou fechar parênteses ')' no WRITELN");
                }
            } else {
                erroSintatico("Faltou abrir parênteses '(' no WRITELN");
            }
        } else if (isPalavraReservada("for")) {
            token = lexico.proximoToken();
            if (token.getClasse() == ClasseToken.Identificador) {
                token = lexico.proximoToken();
                // {A57}
                String variavel = token.getValor().getTexto();
                if (!tabelaSimbolos.containsKey(variavel)) {
                    System.err.println("Variável " + variavel + " não foi declarada");
                    System.exit(-1);
                } else {
                    simbolo = tabelaSimbolos.get(variavel);
                    if (simbolo.getCategoria() != CategoriaSimbolo.VARIAVEL) {
                        System.err.println("O identificador " + variavel + "não é uma variável. A57");
                        System.exit(-1);
                    }

                }

                if (token.getClasse() == ClasseToken.Atribuicao) {
                    token = lexico.proximoToken();
                    expressao();
                    // {A11}
                    escreverCodigo("\tpop dword[ebp - " + simbolo.getEndereco() + "]");
                    String rotuloEntrada = criarRotulo("FOR");
                    String rotuloSaida = criarRotulo("FIMFOR");
                    rotulo = rotuloEntrada;

                    if (isPalavraReservada("to")) {
                        token = lexico.proximoToken();
                        expressao();
                        // {A12}
                        escreverCodigo("\tpush ecx\n"
                                + "\tmov ecx, dword[ebp - " + simbolo.getEndereco() + "]\n"
                                + "\tcmp ecx, dword[esp+4]\n" // +4 por causa do ecx
                                + "\tjg " + rotuloSaida + "\n"
                                + "\tpop ecx");

                        if (isPalavraReservada("do")) {
                            token = lexico.proximoToken();
                            if (isPalavraReservada("begin")) {
                                token = lexico.proximoToken();
                                sentencas();
                                if (isPalavraReservada("end")) {
                                    token = lexico.proximoToken();
                                    // {A13}
                                    escreverCodigo("\tadd dword[ebp - " + simbolo.getEndereco() + "], 1");
                                    escreverCodigo("\tjmp " + rotuloEntrada);
                                    rotulo = rotuloSaida;

                                } else {
                                    erroSintatico("Faltou END no final do FOR");
                                }
                            } else {
                                erroSintatico("Faltou BEGIN no FOR");
                            }
                        } else {
                            erroSintatico("Faltou DO no FOR");
                        }
                    } else {
                        erroSintatico("Faltou TO no FOR");
                    }

                } else {
                    erroSintatico("Faltou Atribuição ':=' no FOR");
                }
            } else {
                erroSintatico("Faltou o Identificador no FOR");
            }

            // repeat {A14} <sentencas> until ( <expressao_logica> ) {A15} |

        } else if (isPalavraReservada("repeat")) {
            token = lexico.proximoToken();
            // {A14}
            String rotRepeat = criarRotulo("Repeat");
            rotulo = rotRepeat;

            sentencas();
            if (isPalavraReservada("until")) {
                token = lexico.proximoToken();
                if (token.getClasse() == ClasseToken.ParentesesEsquerdo) {
                    token = lexico.proximoToken();
                    if (token.getClasse() == ClasseToken.ParentesesDireito) {
                        token = lexico.proximoToken();
                        // {A15}
                        escreverCodigo("\tcmp dword[esp], 0");
                        escreverCodigo("\tje " + rotRepeat);

                    } else {
                        erroSintatico("Faltou fechar parênteses ')' no REPEAT");
                    }
                } else {
                    erroSintatico("Faltou fechar parênteses '(' no REPEAT");
                }

            } else {
                erroSintatico("Faltou UNTIL no REPEAT");
            }

            // while {A16} ( <expressao_logica> ) {A17} do begin <sentencas> end {A18} |

        } else if (isPalavraReservada("while")) {
            token = lexico.proximoToken();
            // {A16}

            String rotuloWhile = criarRotulo("While");
            String rotuloFim = criarRotulo("FimWhile");
            rotulo = rotuloWhile;
            if (token.getClasse() == ClasseToken.ParentesesEsquerdo) {
                token = lexico.proximoToken();
                expressao_logica();
                if (token.getClasse() == ClasseToken.ParentesesDireito) {
                    token = lexico.proximoToken();
                    // {A17}

                    escreverCodigo("\tcmp dword[esp], 0");
                    escreverCodigo("\tje " + rotuloFim);

                    if (isPalavraReservada("do")) {
                        token = lexico.proximoToken();
                        if (isPalavraReservada("begin")) {
                            token = lexico.proximoToken();
                            sentencas();
                            if (isPalavraReservada("end")) {
                                token = lexico.proximoToken();
                                // {A18}
                                escreverCodigo("\tjmp " + rotuloWhile);
                                rotulo = rotuloFim;

                            } else {
                                erroSintatico("Faltou END no final do WHILE");
                            }
                        } else {
                            erroSintatico("Faltou BEGIN no WHILE");
                        }
                    } else {
                        erroSintatico("Faltou DO no WHILE");
                    }

                } else {
                    erroSintatico("Faltou fechar parênteses ')' no WHILE");
                }
            } else {
                erroSintatico("Faltou abrir parênteses '(' no WHILE");
            }

            // if ( <expressao_logica> ) {A19} then begin <sentencas> end {A20} <pfalsa>

        } else if (isPalavraReservada("if")) {
            token = lexico.proximoToken();
            if (token.getClasse() == ClasseToken.ParentesesEsquerdo) {
                token = lexico.proximoToken();
                expressao_logica();
                if (token.getClasse() == ClasseToken.ParentesesDireito) {
                    token = lexico.proximoToken();
                    // {A19}
                    rotuloElse = criarRotulo("Else");
                    String rotuloFim = criarRotulo("FimIf");
                    escreverCodigo("\tcmp dword[esp], 0\n");
                    escreverCodigo("\tje " + rotuloElse);

                    if (isPalavraReservada("then")) {
                        token = lexico.proximoToken();
                        if (isPalavraReservada("begin")) {
                            token = lexico.proximoToken();
                            sentencas();
                            if (isPalavraReservada("end")) {
                                token = lexico.proximoToken();
                                // {A20}
                                escreverCodigo("\tjmp " + rotuloFim);

                                pfalsa();
                                // {A21}
                                rotulo = rotuloFim;

                            } else {
                                erroSintatico("Faltou END no THEN do IF");
                            }
                        } else {
                            erroSintatico("Faltou BEGIN no THEN do IF");
                        }
                    } else {
                        erroSintatico("Faltou THEN no IF");
                    }
                } else {
                    erroSintatico("Faltou fechar parênteses ')' na condição do IF");
                }
            } else {
                erroSintatico("Faltou abrir parênteses '(' na condição do IF");
            }
            // id {A49} := <expressao> {A22}
        } else if (token.getClasse() == ClasseToken.Identificador) {
            // {A49}
            String variavel = token.getValor().getTexto();
            if (!tabelaSimbolos.containsKey(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada");
                System.exit(-1);
            } else {
                simbolo = tabelaSimbolos.get(variavel);
                if (simbolo.getCategoria() != CategoriaSimbolo.VARIAVEL) {
                    System.err.println("O identificador " + variavel + "não é uma variável. A49");
                    System.exit(-1);
                }
            }
            token = lexico.proximoToken();

            if (token.getClasse() == ClasseToken.Atribuicao) {
                token = lexico.proximoToken();
                expressao();
                // {A22}
                simbolo = tabelaSimbolos.get(variavel);
                escreverCodigo("\tpop eax");
                escreverCodigo("\tmov dword[ebp - " + simbolo.getEndereco() + "], eax");

            } else {
                erroSintatico("Faltou a atribuição ':=' em uma Atribuição");
            }
        } else {
            erroSintatico("Faltou um COMANDO");
        }

    }

    // <expressao> ::= <termo> <mais_expressao>
    private void expressao() {
        termo();
        mais_expressao();
    }

    // <mais_expressao> ::= + <termo> {A37} <mais_expressao> |
    // - <termo> {A38} <mais_expressao> | ε
    private void mais_expressao() {
        if (token.getClasse() == ClasseToken.Adicao) {
            token = lexico.proximoToken();
            termo();
            // {A37}
            escreverCodigo("\tpop eax");
            escreverCodigo("\tadd dword[ESP], eax");
            mais_expressao();
        } else if (token.getClasse() == ClasseToken.Adicao) {
            token = lexico.proximoToken();
            termo();
            // {A38}
            escreverCodigo("\tpop eax");
            escreverCodigo("\tsub dword[ESP], eax");
            mais_expressao();
        }
    }

    // <termo> ::= <fator> <mais_termo>
    private void termo() {
        fator();
        mais_termo();
    }

    // <mais_termo> ::= * <fator> {A39} <mais_termo> |
    // / <fator> {A40} <mais_termo> | ε
    private void mais_termo() {
        if (token.getClasse() == ClasseToken.Multiplicacao) {
            token = lexico.proximoToken();
            fator();
            // {A39}
            escreverCodigo("\tpop eax");
            escreverCodigo("\timul eax, dword [ESP]");
            escreverCodigo("\tmov dword [ESP], eax");
            mais_termo();
        } else if (token.getClasse() == ClasseToken.Divisao) {
            token = lexico.proximoToken();
            fator();
            // {A40}
            escreverCodigo("\tpop ecx");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tidiv ecx");
            escreverCodigo("\tpush eax");
            mais_termo();
        }
    }

    // <fator> ::= id {A55} | intnum {A41} | ( <expressao> ) | id {A60} <argumentos>
    // {A42}
    private void fator() {
        if (token.getClasse() == ClasseToken.Identificador) {

            // {A55}
            String variavel = token.getValor().getTexto();
            if (!tabelaSimbolos.containsKey(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada");
                System.exit(-1);
            } else {
                simbolo = tabelaSimbolos.get(variavel);
                if (simbolo.getCategoria() != CategoriaSimbolo.VARIAVEL) {
                    System.err.println("O identificador " + variavel + "não é uma variável. A55");
                    System.exit(-1);
                }
            }
            escreverCodigo("\tpush dword[ebp - " + simbolo.getEndereco() + "]");

            token = lexico.proximoToken();
        } else if (token.getClasse() == ClasseToken.NumeroInteiro) {
            // {A41}
            escreverCodigo("\tpush " + token.getValor().getInteiro());

            token = lexico.proximoToken();
        } else if (token.getClasse() == ClasseToken.ParentesesEsquerdo) {
            token = lexico.proximoToken();
            expressao();
            if (token.getClasse() == ClasseToken.ParentesesEsquerdo) {
                token = lexico.proximoToken();
            } else {
                erroSintatico("Faltou fechar parênteses ')' no FATOR");
            }
        } else {
            erroSintatico("Faltou um FATOR");
        }
    }

    // <expressao_logica> ::= <termo_logico> <mais_expr_logica>
    private void expressao_logica() {
        termo_logico();
        mais_expr_logica();

    }

    // <mais_expr_logica> ::= or <termo_logico> {A26} <mais_expr_logica> | ε
    private void mais_expr_logica() {
        if (isPalavraReservada("or")) {
            token = lexico.proximoToken();
            termo_logico();
            // {A26}
            String rotSaida = criarRotulo("SaidaMEL");
            String rotVerdade = criarRotulo("VerdadeMEL");
            escreverCodigo("\tcmp dword [ESP + 4], 1");
            escreverCodigo("\tje " + rotVerdade);
            escreverCodigo("\tcmp dword [ESP], 1");
            escreverCodigo("\tje " + rotVerdade);
            escreverCodigo("\tmov dword [ESP + 4], 0");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotVerdade;
            escreverCodigo("\tmov dword [ESP + 4], 1");
            rotulo = rotSaida;
            escreverCodigo("\tadd esp, 4");

            mais_expr_logica();
        }
    }

    // <termo_logico> ::= <fator_logico> <mais_termo_logico>
    private void termo_logico() {
        fator_logico();
        mais_termo_logico();
    }

    // <mais_termo_logico> ::= and <fator_logico> {A27} <mais_termo_logico> | ε
    private void mais_termo_logico() {
        if (isPalavraReservada("and")) {
            token = lexico.proximoToken();
            fator_logico();
            // {A27}
            String rotSaida = criarRotulo("SaidaMTL");
            String rotFalso = criarRotulo("FalsoMTL");
            escreverCodigo("\tcmp dword [ESP + 4], 1");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;

            mais_termo_logico();
        }
    }

    // <fator_logico> ::= <relacional> |
    // ( <expressao_logica> ) |
    // not <fator_logico> {A28} |
    // true {A29} |
    // false {A30}
    private void fator_logico() {
        if (token.getClasse() == ClasseToken.ParentesesEsquerdo) {
            token = lexico.proximoToken();
            expressao_logica();
            if (token.getClasse() == ClasseToken.ParentesesDireito) {
                token = lexico.proximoToken();
            } else {
                erroSintatico("Faltou fechar parênteses ')' no fator_logico ");
            }
        } else if (isPalavraReservada("not")) {
            token = lexico.proximoToken();
            fator_logico();
            // {A28}
            String rotFalso = criarRotulo("FalsoFL");
            String rotSaida = criarRotulo("SaidaFL");
            escreverCodigo("\tcmp dword [ESP], 1");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 0");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 1");
            rotulo = rotSaida;

        } else if (isPalavraReservada("true")) {
            token = lexico.proximoToken();
            // {A29}
            escreverCodigo("\tpush 1");

        } else if (isPalavraReservada("false")) {
            token = lexico.proximoToken();
            // {A30}
            escreverCodigo("\tpush 0");

        } else {
            relacional();
        }
    }

    // <relacional> ::= <expressao> = <expressao> {A31} |
    // <expressao> > <expressao> {A32} |
    // <expressao> >= <expressao> {A33} |
    // <expressao> < <expressao> {A34} |
    // <expressao> <= <expressao> {A35} |
    // <expressao> <> <expressao> {A36}
    private void relacional() {
        expressao();
        if (token.getClasse() == ClasseToken.Igual) {
            token = lexico.proximoToken();
            expressao();
            // {A31}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;

        } else if (token.getClasse() == ClasseToken.Maior) {
            token = lexico.proximoToken();
            expressao();
            // {A32}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjle " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;

        } else if (token.getClasse() == ClasseToken.MaiorIgual) {
            token = lexico.proximoToken();
            expressao();
            // {A33}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjl " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;

        } else if (token.getClasse() == ClasseToken.Menor) {
            token = lexico.proximoToken();
            expressao();
            // {A34}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjge " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;

        } else if (token.getClasse() == ClasseToken.MenorIgual) {
            token = lexico.proximoToken();
            expressao();
            // {A35}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjg " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;

        } else if (token.getClasse() == ClasseToken.Diferente) {
            token = lexico.proximoToken();
            expressao();
            // {A36}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tje " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;

        } else {
            erroSintatico("Faltou um operador relacional");
        }
    }

    // <pfalsa> ::= {A25} else begin <sentecas> end | ε
    private void pfalsa() {
        // {A25}
        escreverCodigo(rotuloElse + ":");
        if (isPalavraReservada("else")) {
            token = lexico.proximoToken();
            if (isPalavraReservada("begin")) {
                token = lexico.proximoToken();
                sentencas();
                if (isPalavraReservada("end")) {
                    token = lexico.proximoToken();
                } else {
                    erroSintatico("Faltou END no ELSE");
                }
            } else {
                erroSintatico("Faltou BEGIN no ELSE");
            }
        }
    }

    // <mais_sentencas> ::= ; <cont_sentencas>
    private void mais_sentencas() {
        if (token.getClasse() == ClasseToken.PontoEVirgula) {
            token = lexico.proximoToken();
            cont_sentencas();
        } else {
            erroSintatico("Faltou ponto e vírgula (;) no final de um comando");
        }
    }

    // <cont_sentencas> ::= <sentencas> | ε
    private void cont_sentencas() {
        if (isPalavraReservada("read") || isPalavraReservada("write") || isPalavraReservada("writeln")
                || isPalavraReservada("for")
                || isPalavraReservada("repeat") || isPalavraReservada("while") || isPalavraReservada("if")
                || token.getClasse() == ClasseToken.Identificador) {

            sentencas();
        }
    }

    // <var_read> ::= id {A08} <mais_var_read>
    private void var_read() {
        if (token.getClasse() == ClasseToken.Identificador) {
            // {A08}
            String variavel = token.getValor().getTexto();
            if (!tabelaSimbolos.containsKey(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada");
                System.exit(-1);
            } else {
                Simbolo registro = tabelaSimbolos.get(variavel);
                if (registro.getCategoria() != CategoriaSimbolo.VARIAVEL) {
                    System.err.println("Identificador " + variavel + " não é uma variável");
                    System.exit(-1);
                } else {
                    escreverCodigo("\tmov edx, ebp");
                    escreverCodigo("\tlea eax, [edx - " + registro.getEndereco() + "]");
                    escreverCodigo("\tpush eax");
                    escreverCodigo("\tpush @Integer");
                    escreverCodigo("\tcall scanf");
                    escreverCodigo("\tadd esp, 8");
                    if (!sectionData.contains("@Integer: db '%d',0")) {
                        sectionData.add("@Integer: db '%d',0");
                    }
                }
            }
            token = lexico.proximoToken();
            mais_var_read();

        } else {
            erroSintatico("Falou o identificador da variável a ser lida com READ");
        }
    }

    // <mais_var_read> ::= , <var_read> | ε
    private void mais_var_read() {
        if (token.getClasse() == ClasseToken.Virgula) {
            token = lexico.proximoToken();
            var_read();
        }
    }

    // <exp_write> ::= id {A09} <mais_exp_write> |
    // string {A59} <mais_exp_write> |
    // intnum {A43} <mais_exp_write>
    private void exp_write() {
        if (token.getClasse() == ClasseToken.Identificador) {
            // {A09}
            String variavel = token.getValor().getTexto();
            if (!tabelaSimbolos.containsKey(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada");
                System.exit(-1);
            } else {
                Simbolo registro = tabelaSimbolos.get(variavel);
                if (registro.getCategoria() != CategoriaSimbolo.VARIAVEL) {
                    System.err.println("Identificador " + variavel + " não é uma variável");
                    System.exit(-1);
                } else {
                    escreverCodigo("\tpush dword[ebp - " + registro.getEndereco() + "]");
                    escreverCodigo("\tpush @Integer");
                    escreverCodigo("\tcall printf");
                    escreverCodigo("\tadd esp, 8");
                    if (!sectionData.contains("@Integer: db '%d',0")) {
                        sectionData.add("@Integer: db '%d',0");
                    }
                }
            }
            token = lexico.proximoToken();
            mais_exp_write();
        } else if (token.getClasse() == ClasseToken.String) {
            // {A59}
            String string = token.getValor().getTexto();
            String rotulo = criarRotulo("String");
            sectionData.add(rotulo + ": db ' " + string + " ',0");
            escreverCodigo("\tpush " + rotulo);
            escreverCodigo("\tcall printf");
            escreverCodigo("\tadd esp, 4");

            token = lexico.proximoToken();
            mais_exp_write();

        } else if (token.getClasse() == ClasseToken.NumeroInteiro) {
            // {A43}
            escreverCodigo("\tpush " + token.getValor().getInteiro());
            escreverCodigo("\tpush @Integer");
            escreverCodigo("\tcall printf");
            escreverCodigo("\tadd esp, 8");
            if (!sectionData.contains("@Integer: db '%d',0")) {
                sectionData.add("@Integer: db '%d',0");
            }
            token = lexico.proximoToken();
            mais_exp_write();
        } else {
            erroSintatico("Era esperado um identificador ou uma String ou um número inteiro no WRITE/WRITELN");
        }
    }

    // <mais_exp_write> ::= , <exp_write> | ε
    private void mais_exp_write() {
        if (token.getClasse() == ClasseToken.Virgula) {
            token = lexico.proximoToken();
            exp_write();
        }
    }

    // <dvar> ::= <variaveis> : <tipo_var> {A02}
    private void dvar() {
        variaveis();
        if (token.getClasse() == ClasseToken.DoisPontos) {
            token = lexico.proximoToken();
            tipo_var();
            // {A02}
            int tamanho = 0;
            for (String var : variaveis) {
                tabelaSimbolos.get(var).setTipo(TipoSimbolo.INTEIRO);
                tamanho += TAMANHO_INTEIRO;
            }
            escreverCodigo("\tsub esp, " + tamanho);
            variaveis.clear();

        } else {
            erroSintatico("Faltou dois pontos (:) na declaração da variáveis");
        }
    }

    // <variaveis> ::= id {A03} <mais_var>
    private void variaveis() {
        if (token.getClasse() == ClasseToken.Identificador) {
            // {A03}
            String variavel = token.getValor().getTexto();
            if (tabelaSimbolos.containsKey(variavel)) {
                System.err.println("Variável " + variavel + " já foi declarada anteriormente");
                System.exit(-1);
            } else {
                Simbolo simbolo = new Simbolo();
                simbolo.setLexema(variavel);
                simbolo.setCategoria(CategoriaSimbolo.VARIAVEL);
                simbolo.setEndereco(offsetVariavel);
                tabelaSimbolos.put(variavel, simbolo);
                offsetVariavel += TAMANHO_INTEIRO;
                variaveis.add(variavel);
            }
            token = lexico.proximoToken();
            mais_var();
        } else {
            erroSintatico("Faltou o identificador de uma cariável");
        }
    }

    // <mais_var> ::= , <variaveis> | ε
    private void mais_var() {
        if (token.getClasse() == ClasseToken.Virgula) {
            token = lexico.proximoToken();
            variaveis();
        }
    }

    // <tipo_var> ::= integer
    private void tipo_var() {
        if (isPalavraReservada("integer")) {
            token = lexico.proximoToken();
        } else {
            erroSintatico("Faltou o tipo (integer) na declaração de variáveis ");
        }
    }

    // <mais_dc> ::= ; <cont_dc>
    private void mais_dc() {

        if (token.getClasse() == ClasseToken.PontoEVirgula) {
            token = lexico.proximoToken();
            cont_dc();
        } else {
            erroSintatico("Faltou ponto e vírgula (;) na declaração de variáveis ");
        }

    }

    // <cont_dc> ::= <dvar> <mais_dc> | ε
    private void cont_dc() {
        if (token.getClasse() == ClasseToken.Identificador) {
            dvar();
            mais_dc();
        }

    }

    private void erroSintatico(String erro) {
        System.err.println(token.getLinha() + "," + token.getColuna() + " - Erro Sintático: " + erro);
    }

    private boolean isPalavraReservada(String texto) {

        return token.getClasse() == ClasseToken.PalavraReservada && token.getValor().getTexto().equalsIgnoreCase(texto);

    }

    private void escreverCodigo(String instrucoes) {
        try {
            if (rotulo.isEmpty()) {
                bw.write(instrucoes + "\n");
            } else {
                bw.write(rotulo + ": " + instrucoes + "\n");
                rotulo = "";
            }
        } catch (IOException e) {
            System.err.println("Erro escrevendo no arquivo de saída");
        }
    }

    private String criarRotulo(String texto) {
        String retorno = "rotulo" + texto + contRotulo;
        contRotulo++;
        return retorno;
    }

}
