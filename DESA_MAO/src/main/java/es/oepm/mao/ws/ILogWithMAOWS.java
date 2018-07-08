package es.oepm.mao.ws;

import java.io.Serializable;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import es.oepm.mao.ws.parameters.LogWithMAORequest;
import es.oepm.mao.ws.parameters.LogWithMAOResponse;

@WebService(targetNamespace = "http://logwithmao.ws.mao.oepm.es/")
@SOAPBinding
public interface ILogWithMAOWS extends Serializable {
	
	@WebMethod
	public @WebResult( name="response" ) LogWithMAOResponse 
		logWithMAO ( @WebParam( name = "request" ) LogWithMAORequest request );

}