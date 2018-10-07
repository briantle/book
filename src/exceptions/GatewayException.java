package exceptions;

public class GatewayException extends Exception{

	public GatewayException(Exception e) {
		super(e);
	}
	public GatewayException(String sE) {
		super(sE);
	}
}
