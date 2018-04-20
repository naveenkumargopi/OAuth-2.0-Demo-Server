package com.pramati.ts.oauth.server.thumbsignin.sdk;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Class TransactionInfo.
 *
 * @author nagarajan
 * @version 1.0
 * @since <pre>17/8/17 12:16 PM</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionInfo {

    private String transactionId;
    private String status;
    private String qrImage;
    private String rawData;
    private String deepLinkUrl;
    private long expireInSeconds;
    private String message;

    /**
     * Gets qr image.
     *
     * @return the qr image
     */
    public String getQrImage() {
        return qrImage;
    }

    /**
     * Sets qr image.
     *
     * @param qrImage the qr image
     */
    public void setQrImage(String qrImage) {
        this.qrImage = qrImage;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets transaction id.
     *
     * @return the transaction id
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Sets transaction id.
     *
     * @param transactionId the transaction id
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Gets expire in seconds.
     *
     * @return the expire in seconds
     */
    public long getExpireInSeconds() {
        return expireInSeconds;
    }

    /**
     * Sets expire in seconds.
     *
     * @param expireInSeconds the expire in seconds
     */
    public void setExpireInSeconds(long expireInSeconds) {
        this.expireInSeconds = expireInSeconds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public String getDeepLinkUrl() {
        return deepLinkUrl;
    }

    public void setDeepLinkUrl(String deepLinkUrl) {
        this.deepLinkUrl = deepLinkUrl;
    }
}
