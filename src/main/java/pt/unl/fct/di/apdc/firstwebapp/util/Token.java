package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.Date;
import java.util.UUID;

public class Token {

    public String tokenID;



    // construtor nao devia ter argumentos
//isto e estupido porque na consola do browser o cliente podia alterar a expiration time
    public Token() {

    }

    //o melhor seria o token ser assinado criptograficamente pelo servidor, para garantir que o token
    //nao foi alterado por ninguem
    public Token(String tokenID) {
        this.tokenID = tokenID;
    }
}