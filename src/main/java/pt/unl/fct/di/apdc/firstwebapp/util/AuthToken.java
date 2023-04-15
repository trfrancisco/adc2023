package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.Date;
import java.util.UUID;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000 * 60 * 60 * 2; // 2h

	public String username;

	public long role;
	public String tokenID;

	public Date creationData;

	public Date expirationData;
	
// construtor nao devia ter argumentos
//isto e estupido porque na consola do browser o cliente podia alterar a expiration time
	
	//o melhor seria o token ser assinado criptograficamente pelo servidor, para garantir que o token
	//nao foi alterado por ninguem
	public AuthToken(String username,long role ) {
		this.username = username;
		this.role=role;
		this.tokenID = UUID.randomUUID().toString();
		Date now = new Date();
		Date twoHoursLater = new Date(now.getTime() + EXPIRATION_TIME);
		this.creationData = now;
		this.expirationData = twoHoursLater;
	}
}