package ro.builditsmart.models.tapo.exceptions;

public class TapoException extends Exception {

    public static final int SuccessErrorCode = 0;

    public static final int RequestMethodNotSupportedErrorCode = -10000;
    public static final int InvalidPublicKeyLengthErrorCode = -1010;
    public static final int InvalidRequestOrCredentialsErrorCode = -1501;
    public static final int IncorrectRequestErrorCode = -1002;
    public static final int JsonFormatErrorCode = -1003;
    public static final int ParameterDoesntExistErrorCode = -20104;
    public static final int CloudTokenExpiredOrInvalidErrorCode = -20675;
    public static final int DeviceTokenExpiredOrInvalidErrorCode = 9999;
    public static final int TokenExpiredErrorCode = -20651;
    public static final int SecurePassthroughDeprecated = 1003;
    public static final int InvalidParameterReceived = -1008;
    public static final int TooManyCountdownRules = -1901;

    public static void throwFromErrorCode(int errorCode) throws Exception {
        switch (errorCode) {
            case SuccessErrorCode:
                return;

            case RequestMethodNotSupportedErrorCode:
                throw new TapoInvalidRequestException(errorCode, "Request method not supported");
            case InvalidPublicKeyLengthErrorCode:
                throw new TapoInvalidRequestException(errorCode, "Invalid public key length");
            case InvalidRequestOrCredentialsErrorCode:
                throw new TapoInvalidRequestException(errorCode, "Invalid request or credentials");
            case ParameterDoesntExistErrorCode:
                throw new TapoInvalidRequestException(errorCode, "Parameter doesn't exist");
            case InvalidParameterReceived:
                throw new TapoInvalidRequestException(errorCode, "Invalid request parameter");
            case IncorrectRequestErrorCode:
                throw new TapoInvalidRequestException(errorCode, "Incorrect request");
            case TooManyCountdownRules:
                throw new TapoInvalidRequestException(errorCode, "An active countdown rule is already set");
            case JsonFormatErrorCode:
                throw new TapoJsonException("JSON format error");
            case CloudTokenExpiredOrInvalidErrorCode:
                throw new TapoCloudTokenExpiredOrInvalidException("Cloud token expired or invalid");
            case DeviceTokenExpiredOrInvalidErrorCode:
                throw new TapoDeviceTokenExpiredOrInvalidException("Device token expired or invalid");
            case TokenExpiredErrorCode:
                throw new TapoTokenExpiredException("Token expired");
            case SecurePassthroughDeprecated:
                throw new TapoSecurePassThroughProtocolDeprecatedException("Secure passThrough protocol deprecated in firmware >= \"1.1.0 Build 230721 Rel.224802\" for KLAP.");
            default:
                throw new TapoException("Unexpected Error Code: " + errorCode);
        }
    }

    public TapoException(String message) {
        super(message);
    }

}