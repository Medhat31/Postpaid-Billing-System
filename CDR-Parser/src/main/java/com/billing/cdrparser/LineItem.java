package com.billing.cdrparser;

import com.opencsv.bean.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a single Call Detail Record (CDR) line item parsed from a CSV file.
 * This class holds information such as contract details, call source and destination,
 * service type, duration, timestamp, and rating flags.
 *
 * @author seifabsalam
 * @version 1.0
 * @since 1.0
 */
public class LineItem {

    /**
     * Unique identifier of the associated billing contract.
     */
    @CsvBindByName(column = "contract_id")
    private int contractId;

    /**
     * Calling party telephone number (A-number).
     */
    @CsvBindByName(column = "dial_a")
    private String dialA;

    /**
     * Called party telephone number (B-number).
     */
    @CsvBindByName(column = "dial_b")
    private String dialB;

    /**
     * Type of service used (e.g. voice call, SMS).
     */
    @CsvBindByName(column = "service_type")
    private short serviceType;

    /**
     * Call duration in minutes or fractional seconds.
     */
    @CsvBindByName(column = "duration")
    private BigDecimal duration;

    /**
     * Date and time when the call or transaction commenced.
     */
    @CsvBindByName(column = "start_time")
    @CsvDate("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * Additional network charges in piasters.
     */
    @CsvBindByName(column = "external_fee_piasters")
    private int externalFeePiasters;

    /**
     * Flag indicating if rating processing has already occurred.
     */
    @CsvBindByName(column = "is_rated")
    private boolean rated;

    /**
     * Default constructor for creating an uninitialized LineItem.
     * Required by OpenCSV bean mapping.
     */
    public LineItem() {
    }

    /**
     * Retrieves the contract ID associated with this CDR.
     *
     * @return the contract ID
     */
    public int getContractId() {
        return contractId;
    }

    /**
     * Sets the contract ID associated with this CDR.
     *
     * @param contractId the contract ID to set
     */
    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    /**
     * Retrieves the calling party's phone number (Dial A).
     *
     * @return the calling phone number
     */
    public String getDialA() {
        return dialA;
    }

    /**
     * Sets the calling party's phone number (Dial A).
     *
     * @param dialA the calling phone number to set
     */
    public void setDialA(String dialA) {
        this.dialA = dialA;
    }

    /**
     * Retrieves the called party's phone number (Dial B).
     *
     * @return the called phone number
     */
    public String getDialB() {
        return dialB;
    }

    /**
     * Sets the called party's phone number (Dial B).
     *
     * @param dialB the called phone number to set
     */
    public void setDialB(String dialB) {
        this.dialB = dialB;
    }

    /**
     * Retrieves the service type code representing the call type.
     *
     * @return the service type code
     */
    public short getServiceType() {
        return serviceType;
    }

    /**
     * Sets the service type code representing the call type.
     *
     * @param serviceType the service type code to set
     */
    public void setServiceType(short serviceType) {
        this.serviceType = serviceType;
    }

    /**
     * Retrieves the duration of the call.
     *
     * @return the call duration
     */
    public BigDecimal getDuration() {
        return duration;
    }

    /**
     * Sets the duration of the call.
     *
     * @param duration the call duration to set
     */
    public void setDuration(BigDecimal duration) {
        this.duration = duration;
    }

    /**
     * Retrieves the start time of the call.
     *
     * @return the call start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time of the call.
     *
     * @param startTime the call start time to set
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Retrieves the external transaction fee in piasters.
     *
     * @return the external fee in piasters
     */
    public int getExternalFeePiasters() {
        return externalFeePiasters;
    }

    /**
     * Sets the external transaction fee in piasters.
     *
     * @param externalFeePiasters the external fee to set
     */
    public void setExternalFeePiasters(int externalFeePiasters) {
        this.externalFeePiasters = externalFeePiasters;
    }

    /**
     * Checks whether this call record has already been rated.
     *
     * @return true if the record is rated, false otherwise
     */
    public boolean isRated() {
        return rated;
    }

    /**
     * Sets whether this call record has already been rated.
     *
     * @param rated the rating status to set
     */
    public void setRated(boolean rated) {
        this.rated = rated;
    }
}
