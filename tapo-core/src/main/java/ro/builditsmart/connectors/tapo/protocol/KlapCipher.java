package ro.builditsmart.connectors.tapo.protocol;

import lombok.ToString;
import ro.builditsmart.models.tapo.exceptions.TapoKlapException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static ro.builditsmart.connectors.tapo.util.TapoCrypto.sha256Encode;
import static ro.builditsmart.connectors.tapo.util.TapoUtils.concatBytes;
import static ro.builditsmart.connectors.tapo.util.TapoUtils.truncateByteArray;

@ToString
public class KlapCipher {

    private byte[] key;
    private byte[] iv;
    private byte[] sig;
    private int seq;
    private byte[] segBytes;
    private byte[] ivSeq;

    protected static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    protected static final String CIPHER_ALGORITHM = "AES";
    protected static final String CIPHER_CHARSET = "UTF-8";

    public KlapCipher(byte[] localSeed, byte[] remoteSeed, byte[] userHash) {
        ivDerive(localSeed, remoteSeed, userHash);
        keyDerive(localSeed, remoteSeed, userHash);
        sigDerive(localSeed, remoteSeed, userHash);
    }

    public byte[] encrypt(String message) throws Exception {
        seq++;

        if (Arrays.equals(sig, new byte[28])) {
            throw new TapoKlapException("Sig is all zeros");
        }
        Cipher encodeCipher = getCipher(Cipher.ENCRYPT_MODE, seq);
        byte[] msg = message.getBytes(CIPHER_CHARSET);
        byte[] cipherText = encodeCipher.doFinal(msg);
        byte[] signature = sha256Encode(concatBytes(sig, BigInteger.valueOf(seq).toByteArray(), cipherText));
        return concatBytes(signature, cipherText);
    }

    public String decrypt(byte[] message) {
        try {
            Cipher decodeCipher = getCipher(Cipher.DECRYPT_MODE, seq);
            byte[] bytesToDecode = truncateByteArray(message, 32, message.length - 32);
            byte[] doFinal = decodeCipher.doFinal(bytesToDecode);
            return new String(doFinal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void keyDerive(byte[] localSeed, byte[] remoteSeed, byte[] userHash) {
        byte[] prefix = new byte[]{(byte) 'l', (byte) 's', (byte) 'k'};
        byte[] payload = concatBytes(prefix, localSeed, remoteSeed, userHash);
        byte[] hash = sha256Encode(payload);

        key = truncateByteArray(hash, 0, 16);
    }

    protected void ivDerive(byte[] localSeed, byte[] remoteSeed, byte[] userHash) {
        byte[] prefix = new byte[]{(byte) 'i', (byte) 'v'};
        byte[] payload = concatBytes(prefix, localSeed, remoteSeed, userHash);
        byte[] hash = sha256Encode(payload);
        int hashLength = hash.length;
        // copy last 4 bytes
        byte[] seqBytes = truncateByteArray(hash, hashLength - 4, 4);
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            ByteBuffer.wrap(seqBytes).order(ByteOrder.BIG_ENDIAN);
        }

        seq = new BigInteger(seqBytes).intValue();
        iv = truncateByteArray(hash, 0, 12);
    }

    private void sigDerive(byte[] localSeed, byte[] remoteSeed, byte[] userHash) {
        byte[] prefix = new byte[]{(byte) 'l', (byte) 'd', (byte) 'k'};
        byte[] payload = concatBytes(prefix, localSeed, remoteSeed, userHash);
        byte[] hash = sha256Encode(payload);
        sig = truncateByteArray(hash, 0, 28);
        // return Arrays.copyOfRange(hash, 0, 28);
    }

    private byte[] getIv(int ivSeq) {
        byte[] seq = BigInteger.valueOf(ivSeq).toByteArray();
        return concatBytes(iv, seq);
    }

    private Cipher getCipher(int opMode, int ivSeq) {
        try {
            byte[] ivBuffer = getIv(ivSeq);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, CIPHER_ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBuffer);

            Cipher myCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            myCipher.init(opMode, secretKeySpec, ivParameterSpec);

            return myCipher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected byte[] getSegBytes() {
        byte[] seqBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            seqBytes[i] = (byte) ((seq >> (i * 8)) & 0xFF);
        }
        return seqBytes;
    }

    protected byte[] getIvSeq() {
        segBytes = getSegBytes();
        byte[] payload = concatBytes(iv, segBytes);

        if (payload.length != 16) {
            throw new RuntimeException("Iv and Seq bytes is incorrect length.");
        } else {
            ivSeq = payload;
            return payload;
        }
    }

    public int getSeq() {
        return this.seq;
    }

}
