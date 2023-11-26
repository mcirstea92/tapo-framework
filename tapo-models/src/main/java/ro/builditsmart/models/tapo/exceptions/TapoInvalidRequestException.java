package ro.builditsmart.models.tapo.exceptions;

public class TapoInvalidRequestException extends TapoException {

    public TapoInvalidRequestException(int errorCode, String message) {
        super(message);
    }

}
