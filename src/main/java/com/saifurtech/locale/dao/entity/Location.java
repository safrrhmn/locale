package com.saifurtech.locale.dao.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "Location")
@DynamoDBDocument
public class Location {

    @DynamoDBAutoGeneratedKey
    @JsonIgnore
    private String id;

    @JsonProperty("postal_code")
    @DynamoDBHashKey(attributeName = "postalCode")
    @DynamoDBAttribute
    private String postalCode;

    @JsonProperty("country_code")
    @DynamoDBAttribute
    @DynamoDBRangeKey(attributeName = "countryCode")
    private String countryCode;

    @JsonProperty("latitude")
    @DynamoDBAttribute
    private String latitude;

    @JsonProperty("longitude")
    @DynamoDBAttribute
    private String longitude;

    @JsonProperty("city")
    @DynamoDBAttribute
    private String city;

    @JsonProperty("state")
    @DynamoDBAttribute
    private String state;

    @JsonProperty("city_en")
    @DynamoDBAttribute
    private String cityEnglish;

    @JsonProperty("state_en")
    @DynamoDBAttribute
    private String stateEnglish;

    @JsonProperty("state_code")
    @DynamoDBAttribute
    private String stateCode;

    @JsonProperty("province")
    @DynamoDBAttribute
    private String province;

    @JsonProperty("province_code")
    @DynamoDBAttribute
    private String provinceCode;

    @JsonIgnore
    @DynamoDBAttribute
    private String allPostalCodes;
}
